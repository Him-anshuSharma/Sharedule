package himanshu.com.sharedule.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import himanshu.com.sharedule.database.entity.DailyTask
import himanshu.com.sharedule.database.entity.Recurrence
import himanshu.com.sharedule.repository.DailyTaskRepository
import himanshu.com.sharedule.repository.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlinx.coroutines.flow.first

class DailyTaskViewModel(context: Context) : ViewModel() {
    private val repository = DailyTaskRepository(context)

    companion object {
        private const val TAG = "DailyTaskViewModel"
    }

    // StateFlows for UI
    val todayTasks = repository.getTasksForToday().stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5000),
        emptyList()
    )
    val completedToday = repository.getCompletedTasksForToday().stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5000),
        emptyList()
    )
    val pendingToday = repository.getPendingTasksForToday().stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5000),
        emptyList()
    )
    val allTasks = repository.getAllTasks().stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5000),
        emptyList()
    )

    // Sync state
    val syncState = repository.syncState.stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5000),
        SyncState.Idle
    )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val prefs: SharedPreferences = context.getSharedPreferences("recurrence_prefs", Context.MODE_PRIVATE)
    private val LAST_PROCESSED_KEY = "last_processed_date"

    init {
        // Debug: Log when ViewModel is created
        Log.d(TAG, "DailyTaskViewModel initialized")

        // Debug: Observe todayTasks flow
        viewModelScope.launch {
            todayTasks.collect { tasks ->
                Log.d(TAG, "Today tasks updated: ${tasks.size} tasks")
                tasks.forEach { task ->
                    Log.d(TAG, "Task: ${task.title}, Date: ${task.date}, Done: ${task.isDone}")
                }
            }
        }

        // Observe sync state
        viewModelScope.launch {
            syncState.collect { state ->
                Log.d(TAG, "Sync state changed: $state")
                when (state) {
                    is SyncState.Error -> _error.value = state.message
                    else -> _error.value = null
                }
            }
        }

        viewModelScope.launch {
            ensureRecurringTasksUpToDate()
        }
    }

    fun addTask(task: DailyTask) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding task: ${task.title} with date: ${task.date}")
                repository.insertTask(task)
                Log.d(TAG, "Task added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task", e)
                _error.value = e.message
            }
        }
    }

    fun updateTask(task: DailyTask) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating task: ${task.title}")
                repository.updateTask(task)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task", e)
                _error.value = e.message
            }
        }
    }

    fun deleteTask(task: DailyTask) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting task: ${task.title}")
                repository.deleteTask(task)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task", e)
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Clear all view model state (useful for logout)
     */
    fun clearAllState() {
        _error.value = null
        // Note: The StateFlow values will be reset when the repository data is cleared
    }

    // Sync methods
    fun syncToFirebase() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sync to Firebase")
                repository.syncToFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing to Firebase", e)
                _error.value = e.message
            }
        }
    }

    fun syncFromFirebase() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting sync from Firebase")
                repository.syncFromFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing from Firebase", e)
                _error.value = e.message
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Clearing all data")
                repository.clearAllData()
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
                _error.value = e.message
            }
        }
    }

    // Debug method to add sample tasks
    fun addSampleTasks() {
        viewModelScope.launch {
            try {
                val today = getTodayMidnightMillis()
                Log.d(TAG, "Adding sample tasks for today: $today")

                val sampleTasks = listOf(
                    DailyTask(
                        title = "Complete project presentation",
                        description = "Finish the slides for tomorrow's meeting",
                        date = today,
                        isDone = false
                    ),
                    DailyTask(
                        title = "Buy groceries",
                        description = "Milk, bread, eggs, and vegetables",
                        date = today,
                        isDone = true
                    ),
                    DailyTask(
                        title = "Call mom",
                        description = "Check in and see how she's doing",
                        date = today,
                        isDone = false
                    ),
                    DailyTask(
                        title = "Exercise",
                        description = "30 minutes of cardio",
                        date = today,
                        isDone = false
                    )
                )

                sampleTasks.forEach { task ->
                    repository.insertTask(task)
                    Log.d(TAG, "Added sample task: ${task.title}")
                }

                Log.d(TAG, "All sample tasks added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample tasks", e)
                _error.value = e.message
            }
        }
    }

    private fun getTodayMidnightMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    suspend fun ensureRecurringTasksUpToDate() {
        val today = getTodayMidnightMillis()
        val lastProcessed = prefs.getLong(LAST_PROCESSED_KEY, today)
        Log.d(TAG, "Last processed date: $lastProcessed")
        val allTasks = repository.getTasksByDate(lastProcessed)
        val recurringTasks = allTasks.filter{ it.recurrence != null }
        var date = lastProcessed + 24 * 60 * 60 * 1000L
        while (date <= today) {
            for (task in recurringTasks) {
                val recurrence = task.recurrence ?: continue
                val shouldCreate = when (recurrence.type) {
                    "DAILY" -> true
                    "WEEKLY" -> {
                        val cal = Calendar.getInstance().apply { timeInMillis = date }
                        recurrence.daysOfWeek?.contains(cal.get(Calendar.DAY_OF_WEEK)) ?: false
                    }
                    "CUSTOM" -> {
                        val lastTaskDate = allTasks
                            .filter { t -> t.title == task.title && t.recurrence == task.recurrence }
                            .maxOfOrNull { t -> t.date } ?: task.date
                        val intervalMillis = (recurrence.interval ?: 1) * 24 * 60 * 60 * 1000L
                        ((date - lastTaskDate) % intervalMillis == 0L) && (date > lastTaskDate)
                    }
                    else -> false
                }
                val exists = allTasks.any { t -> t.title == task.title && t.date == date && t.recurrence == task.recurrence }
                if (shouldCreate && !exists) {
                    val newTask = task.copy(
                        localId = 0L,
                        firebaseId = null,
                        date = date,
                        isDone = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.insertTask(newTask)
                }
            }
            date += 24 * 60 * 60 * 1000L
        }
        prefs.edit().putLong(LAST_PROCESSED_KEY, today).apply()
    }

    // Call ensureRecurringTasksUpToDate() after recurrence is set/changed
    fun onRecurrenceChanged() {
        viewModelScope.launch {
            ensureRecurringTasksUpToDate()
        }
    }
}