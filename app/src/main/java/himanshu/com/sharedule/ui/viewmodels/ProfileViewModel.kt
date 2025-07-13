package himanshu.com.sharedule.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import himanshu.com.sharedule.services.DeviceStatusService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DeviceState(
    val isSilentMode: Boolean = false,
    val isDndMode: Boolean = false,
    val isInternetConnected: Boolean = false,
    val lastConnected: Long = 0L,
    val lastUpdated: Long = 0L,
    val deviceId: String = "unknown"
)

class ProfileViewModel : ViewModel() {
    
    private val _profileData = MutableStateFlow<ProfileData?>(null)
    val profileData: StateFlow<ProfileData?> = _profileData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfileData(context: Context, user: FirebaseUser) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val deviceState = getCurrentDeviceState(context)
                val profileData = ProfileData(
                    user = user,
                    accountInfo = getAccountInfo(user),
                    deviceState = deviceState
                )
                
                _profileData.value = profileData
                
                // Set up real-time monitoring
                setupRealTimeMonitoring(context)
            } catch (e: Exception) {
                _error.value = "Failed to load profile data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun setupRealTimeMonitoring(context: Context) {
        // DeviceStatusService is already started in MainActivity
        // We can update device state periodically or on demand
        viewModelScope.launch {
            // Update device state every 5 seconds
            while (true) {
                kotlinx.coroutines.delay(5000)
                _profileData.value?.let { currentProfile ->
                    val updatedDeviceState = getCurrentDeviceState(context)
                    val updatedProfile = currentProfile.copy(deviceState = updatedDeviceState)
                    _profileData.value = updatedProfile
                }
            }
        }
    }
    
    private fun getAccountInfo(user: FirebaseUser): AccountInfo {
        return AccountInfo(
            displayName = user.displayName ?: "Unknown",
            email = user.email ?: "No email",
            phoneNumber = user.phoneNumber ?: "No phone number",
            isEmailVerified = user.isEmailVerified,
            accountCreated = formatDate(user.metadata?.creationTimestamp ?: 0),
            lastSignIn = formatDate(user.metadata?.lastSignInTimestamp ?: 0),
            photoUrl = user.photoUrl?.toString(),
            uid = user.uid
        )
    }
    
    private fun getCurrentDeviceState(context: Context): DeviceState {
        return DeviceState(
            isSilentMode = DeviceStatusService.isSilentMode(context),
            isDndMode = DeviceStatusService.isDndMode(context),
            isInternetConnected = DeviceStatusService.isInternetConnected(context),
            lastConnected = DeviceStatusService.lastTimeConnected(),
            lastUpdated = System.currentTimeMillis(),
            deviceId = getDeviceId(context)
        )
    }
    
    // Device state methods are now handled by DeviceStatusService
    
    private fun getDeviceId(context: Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
    
    private fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Unknown"
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return formatter.format(date)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        // The device state service will be cleaned up when the context is available
        // We can't stop monitoring here since we don't have context
    }
}

data class ProfileData(
    val user: FirebaseUser,
    val accountInfo: AccountInfo,
    val deviceState: DeviceState
)

data class AccountInfo(
    val displayName: String,
    val email: String,
    val phoneNumber: String,
    val isEmailVerified: Boolean,
    val accountCreated: String,
    val lastSignIn: String,
    val photoUrl: String?,
    val uid: String
)

 