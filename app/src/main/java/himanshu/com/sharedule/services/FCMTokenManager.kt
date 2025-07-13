package himanshu.com.sharedule.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FCMTokenManager {
    
    companion object {
        private const val TAG = "FCMTokenManager"
        
        /**
         * Get the current FCM token
         */
        suspend fun getToken(): String? {
            return try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
                null
            }
        }
        
        /**
         * Delete the current FCM token
         */
        suspend fun deleteToken() {
            try {
                FirebaseMessaging.getInstance().deleteToken()
                Log.d(TAG, "FCM token deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete FCM token", e)
            }
        }
        
        /**
         * Subscribe to a topic
         */
        suspend fun subscribeToTopic(topic: String) {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
                Log.d(TAG, "Subscribed to topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to topic: $topic", e)
            }
        }
        
        /**
         * Unsubscribe from a topic
         */
        suspend fun unsubscribeFromTopic(topic: String) {
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
                Log.d(TAG, "Unsubscribed from topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
            }
        }
        
        /**
         * Check if auto-init is enabled
         */
        fun isAutoInitEnabled(): Boolean {
            return FirebaseMessaging.getInstance().isAutoInitEnabled
        }
        
        /**
         * Enable or disable auto-init
         */
        fun setAutoInitEnabled(enabled: Boolean) {
            FirebaseMessaging.getInstance().isAutoInitEnabled = enabled
            Log.d(TAG, "Auto-init ${if (enabled) "enabled" else "disabled"}")
        }
    }
} 