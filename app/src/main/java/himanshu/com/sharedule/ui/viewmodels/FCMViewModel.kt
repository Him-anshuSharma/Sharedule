package himanshu.com.sharedule.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import himanshu.com.sharedule.services.FCMTokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FCMViewModel : ViewModel() {

    companion object {
        private const val TAG = "FCMViewModel"
    }

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAutoInitEnabled = MutableStateFlow(true)
    val isAutoInitEnabled: StateFlow<Boolean> = _isAutoInitEnabled.asStateFlow()

    // NEW: State for notifications received
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        loadFCMToken()
        checkAutoInitStatus()
    }

    /**
     * Load the current FCM token
     */
    fun loadFCMToken() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val token = FCMTokenManager.Companion.getToken()
                _fcmToken.value = token

                Log.d(TAG, "FCM token loaded: ${token?.take(10)}...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load FCM token", e)
                _error.value = "Failed to load FCM token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete the current FCM token
     */
    fun deleteToken() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                FCMTokenManager.Companion.deleteToken()
                _fcmToken.value = null

                Log.d(TAG, "FCM token deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete FCM token", e)
                _error.value = "Failed to delete FCM token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Subscribe to a topic
     */
    fun subscribeToTopic(topic: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                FCMTokenManager.Companion.subscribeToTopic(topic)
                Log.d(TAG, "Subscribed to topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to subscribe to topic: $topic", e)
                _error.value = "Failed to subscribe to topic: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Unsubscribe from a topic
     */
    fun unsubscribeFromTopic(topic: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                FCMTokenManager.Companion.unsubscribeFromTopic(topic)
                Log.d(TAG, "Unsubscribed from topic: $topic")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unsubscribe from topic: $topic", e)
                _error.value = "Failed to unsubscribe from topic: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Check auto-init status
     */
    private fun checkAutoInitStatus() {
        _isAutoInitEnabled.value = FCMTokenManager.Companion.isAutoInitEnabled()
    }

    /**
     * Enable or disable auto-init
     */
    fun setAutoInitEnabled(enabled: Boolean) {
        FCMTokenManager.Companion.setAutoInitEnabled(enabled)
        _isAutoInitEnabled.value = enabled
        Log.d(TAG, "Auto-init ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }

    // --- NEW METHODS FOR NOTIFICATIONS ---

    /**
     * Add a received notification to the list.
     * Call this from your FirebaseMessagingService when a message is received.
     */
    fun addNotification(message: String) {
        _notifications.value = _notifications.value + message
        Log.d(TAG, "Notification received: $message")
    }

    /**
     * Clear all received notifications.
     * Optionally expose this to UI if you want a "clear all" button.
     */
    fun clearNotifications() {
        _notifications.value = emptyList()
        Log.d(TAG, "Notifications cleared")
    }
}