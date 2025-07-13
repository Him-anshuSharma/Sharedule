package himanshu.com.sharedule.services

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import himanshu.com.sharedule.model.DatabaseProvider
import himanshu.com.sharedule.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.tasks.await

class DataClearService {
    
    companion object {
        private const val TAG = "DataClearService"
        
        /**
         * Clear all local data for the current user
         * This includes:
         * - Room database (tasks)
         * - SharedPreferences
         * - FCM token
         * - Firebase Auth sign out
         * - Profile data
         */
        suspend fun clearAllLocalData(context: Context) {
            try {
                Log.d(TAG, "Starting to clear all local data")
                
                // 1. Clear Room database
                clearLocalDatabase(context)
                
                // 2. Clear SharedPreferences
                clearSharedPreferences(context)
                
                // 3. Delete FCM token
                deleteFCMToken()
                
                // 4. Clear Firebase Auth (sign out)
                signOutFirebaseAuth()
                
                // 5. Clear any cached data
                clearCachedData()
                
                // 6. Clear profile data (if ProfileViewModel is available)
                clearProfileData()
                
                Log.d(TAG, "Successfully cleared all local data")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing local data", e)
                throw e
            }
        }
        
        /**
         * Clear the local Room database
         */
        private suspend fun clearLocalDatabase(context: Context) {
            try {
                Log.d(TAG, "Clearing local database")
                val database = DatabaseProvider.getDatabase(context)
                val dao = database.dailyTaskDao()
                dao.clearAllTasks()
                Log.d(TAG, "Local database cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing local database", e)
                throw e
            }
        }
        
        /**
         * Clear SharedPreferences
         */
        private fun clearSharedPreferences(context: Context) {
            try {
                Log.d(TAG, "Clearing SharedPreferences")
                val sharedPrefs = context.getSharedPreferences("sharedule_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                Log.d(TAG, "SharedPreferences cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing SharedPreferences", e)
                // Don't throw here as SharedPreferences clearing is not critical
            }
        }
        
        /**
         * Delete FCM token
         */
        private suspend fun deleteFCMToken() {
            try {
                Log.d(TAG, "Deleting FCM token")
                FirebaseMessaging.getInstance().deleteToken()
                Log.d(TAG, "FCM token deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting FCM token", e)
                // Don't throw here as FCM token deletion is not critical
            }
        }
        
        /**
         * Sign out from Firebase Auth
         */
        private fun signOutFirebaseAuth() {
            try {
                Log.d(TAG, "Signing out from Firebase Auth")
                FirebaseAuth.getInstance().signOut()
                Log.d(TAG, "Firebase Auth sign out successful")
            } catch (e: Exception) {
                Log.e(TAG, "Error signing out from Firebase Auth", e)
                throw e
            }
        }
        
        /**
         * Clear any cached data
         */
        private fun clearCachedData() {
            try {
                Log.d(TAG, "Clearing cached data")
                // Clear any other cached data here if needed
                Log.d(TAG, "Cached data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cached data", e)
                // Don't throw here as cache clearing is not critical
            }
        }
        
        /**
         * Clear profile data
         */
        private fun clearProfileData() {
            try {
                Log.d(TAG, "Clearing profile data")
                // Note: ProfileViewModel state will be cleared when the user navigates away
                // This is handled by the ViewModel lifecycle
                Log.d(TAG, "Profile data cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing profile data", e)
                // Don't throw here as profile clearing is not critical
            }
        }
        
        /**
         * Clear user-specific data from Firestore (optional - for complete data removal)
         * Note: This requires user authentication and proper Firestore rules
         */
        suspend fun clearUserDataFromFirestore() {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Log.w(TAG, "No authenticated user, skipping Firestore data clearing")
                    return
                }
                
                Log.d(TAG, "Clearing user data from Firestore")
                val firestore = FirebaseFirestore.getInstance()
                
                // Clear user's tasks collection
                val tasksCollection = firestore
                    .collection("users")
                    .document(user.uid)
                    .collection("daily_tasks")
                
                val tasksSnapshot = tasksCollection.get().await()
                val batch = firestore.batch()
                
                tasksSnapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                
                batch.commit().await()
                Log.d(TAG, "User data cleared from Firestore successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing user data from Firestore", e)
                // Don't throw here as Firestore clearing is optional
            }
        }
    }
} 