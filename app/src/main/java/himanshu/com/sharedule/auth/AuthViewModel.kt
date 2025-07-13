package himanshu.com.sharedule.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Check if user is already signed in
        auth.currentUser?.let { user ->
            _authState.value = AuthState.SignedIn(user)
        }
    }

    fun getUser(): String = auth.currentUser?.displayName.toString()
    
    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                
                result.user?.let { user ->
                    _authState.value = AuthState.SignedIn(user)
                } ?: run {
                    _authState.value = AuthState.Error("Sign in failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _authState.value = AuthState.SignedOut
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign out failed")
            }
        }
    }
    
    /**
     * Comprehensive logout that clears all local data
     */
    fun logout(context: android.content.Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Clear all local data
                himanshu.com.sharedule.services.DataClearService.clearAllLocalData(context)
                
                // Update auth state
                _authState.value = AuthState.SignedOut
                
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }
    
    /**
     * Logout with complete data removal (including Firestore)
     */
    fun logoutWithDataRemoval(context: android.content.Context) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                
                // Clear all local data
                himanshu.com.sharedule.services.DataClearService.clearAllLocalData(context)
                
                // Clear user data from Firestore (optional)
                himanshu.com.sharedule.services.DataClearService.clearUserDataFromFirestore()
                
                // Update auth state
                _authState.value = AuthState.SignedOut
                
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout with data removal failed")
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class SignedIn(val user: com.google.firebase.auth.FirebaseUser) : AuthState()
    object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
} 