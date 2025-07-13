package himanshu.com.sharedule.model

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class DailyTaskRepository(context: Context) {
    private val db = DatabaseProvider.getDatabase(context)
    private val dao = db.dailyTaskDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "DailyTaskRepository"
        private const val SYNC_RETRY_DELAY = 5000L // 5 seconds
        private const val MAX_SYNC_RETRIES = 3
    }
    
    // Sync state tracking
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    // Pending sync operations
    private val pendingSyncs = ConcurrentHashMap<String, SyncOperation>()
    
    // Get user-specific collection
    private fun getUserTasksCollection() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "anonymous")
        .collection("daily_tasks")

    // Get today's date in UTC millis (midnight)
    private fun todayUtcMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getTasksForToday(): Flow<List<DailyTask>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = cal.timeInMillis
        
        Log.d(TAG, "Getting tasks for today: startOfDay=$startOfDay, endOfDay=$endOfDay")
        
        // Trigger background sync if needed
        CoroutineScope(Dispatchers.IO).launch {
            syncFromFirebaseIfNeeded()
        }
        
        return dao.getTasksForDay(startOfDay, endOfDay)
    }
    
    fun getAllTasks(): Flow<List<DailyTask>> {
        Log.d(TAG, "Getting all tasks")
        
        // Trigger background sync if needed
        CoroutineScope(Dispatchers.IO).launch {
            syncFromFirebaseIfNeeded()
        }
        
        return dao.getAllTasks()
    }
    
    fun getCompletedTasksForToday(): Flow<List<DailyTask>> {
        val today = todayUtcMillis()
        Log.d(TAG, "Getting completed tasks for today: $today")
        return dao.getCompletedTasksForDate(today)
    }
    
    fun getPendingTasksForToday(): Flow<List<DailyTask>> {
        val today = todayUtcMillis()
        Log.d(TAG, "Getting pending tasks for today: $today")
        return dao.getPendingTasksForDate(today)
    }

    suspend fun insertTask(task: DailyTask) {
        try {
            Log.d(TAG, "Inserting task: ${task.title} with date: ${task.date}")
            
            // Insert to local database first
            val localId = dao.insertTask(task)
            val taskWithLocalId = task.copy(localId = localId)
            
            Log.d(TAG, "Task inserted with local ID: $localId")
            
            // Queue for Firebase sync
            queueForFirebaseSync(SyncOperation.Insert(taskWithLocalId))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting task", e)
            throw e
        }
    }

    suspend fun updateTask(task: DailyTask) {
        try {
            Log.d(TAG, "Updating task: ${task.title}")
            
            // Update local database first
            dao.updateTask(task)
            
            // Queue for Firebase sync
            queueForFirebaseSync(SyncOperation.Update(task))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task", e)
            throw e
        }
    }

    suspend fun deleteTask(task: DailyTask) {
        try {
            Log.d(TAG, "Deleting task: ${task.title}")
            
            // Delete from local database first
            dao.deleteTask(task)
            
            // Queue for Firebase sync
            queueForFirebaseSync(SyncOperation.Delete(task))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
            throw e
        }
    }
    
    // Manual sync methods
    suspend fun syncToFirebase() {
        _syncState.value = SyncState.SyncingToFirebase
        try {
            val allTasks = dao.getAllTasks().first()
            Log.d(TAG, "Syncing ${allTasks.size} tasks to Firebase")
            
            allTasks.forEach { task ->
                syncTaskToFirebase(task)
            }
            
            _syncState.value = SyncState.Synced
            Log.d(TAG, "Sync to Firebase completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firebase", e)
            _syncState.value = SyncState.Error("Failed to sync to Firebase: ${e.message}")
        }
    }
    
    suspend fun syncFromFirebase() {
        _syncState.value = SyncState.SyncingFromFirebase
        try {
            val user = auth.currentUser
            if (user == null) {
                Log.w(TAG, "No authenticated user, skipping Firebase sync")
                _syncState.value = SyncState.Synced
                return
            }
            
            val snapshot = getUserTasksCollection()
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            Log.d(TAG, "Retrieved ${snapshot.size()} tasks from Firebase")
            
            val firebaseTasks = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(DailyTask::class.java)?.copy(firebaseId = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Firebase document ${doc.id}", e)
                    null
                }
            }
            
            // Merge with local data
            mergeTasksFromFirebase(firebaseTasks)
            
            _syncState.value = SyncState.Synced
            Log.d(TAG, "Sync from Firebase completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firebase", e)
            _syncState.value = SyncState.Error("Failed to sync from Firebase: ${e.message}")
        }
    }
    
    private suspend fun syncFromFirebaseIfNeeded() {
        // Only sync if we haven't synced recently or if there are pending operations
        if (pendingSyncs.isNotEmpty()) {
            Log.d(TAG, "Found ${pendingSyncs.size} pending syncs, triggering sync")
            syncFromFirebase()
        }
    }
    
    private fun queueForFirebaseSync(operation: SyncOperation) {
        val operationId = "${operation.type}_${System.currentTimeMillis()}"
        pendingSyncs[operationId] = operation
        
        Log.d(TAG, "Queued sync operation: $operationId")
        
        // Trigger background sync
        CoroutineScope(Dispatchers.IO).launch {
            processPendingSyncs()
        }
    }
    
    private suspend fun processPendingSyncs() {
        if (pendingSyncs.isEmpty()) return
        
        _syncState.value = SyncState.SyncingToFirebase
        
        val operations = pendingSyncs.values.toList()
        pendingSyncs.clear()
        
        operations.forEach { operation ->
            try {
                when (operation) {
                    is SyncOperation.Insert -> syncTaskToFirebase(operation.task)
                    is SyncOperation.Update -> syncTaskToFirebase(operation.task)
                    is SyncOperation.Delete -> deleteTaskFromFirebase(operation.task)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing sync operation: $operation", e)
                // Re-queue failed operations
                pendingSyncs["${operation.type}_${System.currentTimeMillis()}"] = operation
            }
        }
        
        _syncState.value = SyncState.Synced
    }
    
    private suspend fun syncTaskToFirebase(task: DailyTask) {
        try {
            val user = auth.currentUser
            if (user == null) {
                Log.w(TAG, "No authenticated user, skipping Firebase sync")
                return
            }
            
            val docId = task.firebaseId ?: firestore.collection("temp").document().id
            val taskToSync = task.copy(
                firebaseId = docId,
                updatedAt = System.currentTimeMillis()
            )
            
            getUserTasksCollection()
                .document(docId)
                .set(taskToSync)
                .await()
            
            // Update local task with Firebase ID if it was new
            if (task.firebaseId == null) {
                dao.updateTask(taskToSync)
            }
            
            Log.d(TAG, "Successfully synced task to Firebase: ${task.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing task to Firebase: ${task.title}", e)
            throw e
        }
    }
    
    private suspend fun deleteTaskFromFirebase(task: DailyTask) {
        try {
            val user = auth.currentUser
            if (user == null) {
                Log.w(TAG, "No authenticated user, skipping Firebase delete")
                return
            }
            
            task.firebaseId?.let { docId ->
                getUserTasksCollection()
                    .document(docId)
                    .delete()
                    .await()
                
                Log.d(TAG, "Successfully deleted task from Firebase: ${task.title}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task from Firebase: ${task.title}", e)
            throw e
        }
    }
    
    private suspend fun mergeTasksFromFirebase(firebaseTasks: List<DailyTask>) {
        val localTasks = dao.getAllTasks().first()
        
        // Create maps for efficient lookup
        val localTaskMap = localTasks.associateBy { it.localId }
        val firebaseTaskMap = firebaseTasks.associateBy { it.firebaseId }
        
        // Tasks to insert (new from Firebase)
        val tasksToInsert = firebaseTasks.filter { firebaseTask ->
            localTasks.none { localTask -> 
                localTask.firebaseId == firebaseTask.firebaseId || 
                localTask.localId == firebaseTask.localId 
            }
        }
        
        // Tasks to update (conflicts or newer versions)
        val tasksToUpdate = firebaseTasks.filter { firebaseTask ->
            localTasks.any { localTask ->
                (localTask.firebaseId == firebaseTask.firebaseId || 
                 localTask.localId == firebaseTask.localId) &&
                localTask.updatedAt < firebaseTask.updatedAt
            }
        }
        
        // Insert new tasks
        tasksToInsert.forEach { task ->
            dao.insertTask(task)
            Log.d(TAG, "Inserted new task from Firebase: ${task.title}")
        }
        
        // Update existing tasks
        tasksToUpdate.forEach { firebaseTask ->
            val localTask = localTasks.find { 
                it.firebaseId == firebaseTask.firebaseId || 
                it.localId == firebaseTask.localId 
            }
            localTask?.let {
                dao.updateTask(firebaseTask.copy(localId = it.localId))
                Log.d(TAG, "Updated task from Firebase: ${firebaseTask.title}")
            }
        }
        
        Log.d(TAG, "Merged ${tasksToInsert.size} new tasks and ${tasksToUpdate.size} updated tasks")
    }
    
    // Clear all data (for testing or reset)
    suspend fun clearAllData() {
        try {
            dao.clearAllTasks()
            pendingSyncs.clear()
            _syncState.value = SyncState.Idle
            Log.d(TAG, "Cleared all local data")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data", e)
            throw e
        }
    }
}

// Sync state enum
sealed class SyncState {
    object Idle : SyncState()
    object SyncingToFirebase : SyncState()
    object SyncingFromFirebase : SyncState()
    object Synced : SyncState()
    data class Error(val message: String) : SyncState()
}

// Sync operation sealed class
sealed class SyncOperation {
    abstract val type: String
    
    data class Insert(val task: DailyTask) : SyncOperation() {
        override val type = "INSERT"
    }
    
    data class Update(val task: DailyTask) : SyncOperation() {
        override val type = "UPDATE"
    }
    
    data class Delete(val task: DailyTask) : SyncOperation() {
        override val type = "DELETE"
    }
} 