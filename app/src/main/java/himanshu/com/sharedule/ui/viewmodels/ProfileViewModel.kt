package himanshu.com.sharedule.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    fun loadProfileData(context: Context) {
        viewModelScope.launch {
            try {
                println("ProfileViewModel: Starting to load profile data")
                _isLoading.value = true
                _error.value = null
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                println("ProfileViewModel: Current user: ${currentUser?.email}")
                
                if (currentUser == null) {
                    _error.value = "No user logged in"
                    _isLoading.value = false
                    println("ProfileViewModel: No user logged in")
                    return@launch
                }
                
                val deviceState = getCurrentDeviceState(context)
                println("ProfileViewModel: Device state created")
                
                val profileData = ProfileData(
                    user = currentUser,
                    accountInfo = getAccountInfo(currentUser),
                    deviceState = deviceState
                )
                
                println("ProfileViewModel: Profile data created, setting value")
                _profileData.value = profileData
                
                // Set up real-time monitoring
                setupRealTimeMonitoring(context)
                println("ProfileViewModel: Profile data loaded successfully")
            } catch (e: Exception) {
                println("ProfileViewModel: Error loading profile data: ${e.message}")
                _error.value = "Failed to load profile data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refreshProfile() {
        // This will be called from the UI to refresh the profile data
        // We'll need context to reload, so we'll just update the device state
        // and trigger a UI refresh
        viewModelScope.launch {
            _profileData.value?.let { currentProfile ->
                // Update device state if we have context
                // For now, just update the timestamp
                val updatedDeviceState = currentProfile.deviceState.copy(
                    lastUpdated = System.currentTimeMillis()
                )
                val updatedProfile = currentProfile.copy(deviceState = updatedDeviceState)
                _profileData.value = updatedProfile
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
    
    /**
     * Clear all profile data (useful for logout)
     */
    fun clearProfileData() {
        _profileData.value = null
        _isLoading.value = false
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

 