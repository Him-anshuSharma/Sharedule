package himanshu.com.sharedule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import himanshu.com.sharedule.auth.AuthState
import himanshu.com.sharedule.auth.AuthViewModel
import himanshu.com.sharedule.config.AppConfig
import himanshu.com.sharedule.ui.screens.HomeScreen
import himanshu.com.sharedule.ui.screens.LoginScreen
import himanshu.com.sharedule.ui.theme.ShareduleTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize configuration
        AppConfig.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            ShareduleTheme {
                ShareduleApp()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}

@Composable
fun ShareduleApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    
    when (authState) {
        is AuthState.SignedIn -> {
            HomeScreen(
                onSignOut = {
                    // This will be handled by the AuthViewModel
                }
            )
        }
        is AuthState.SignedOut, is AuthState.Initial -> {
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
        is AuthState.Loading -> {
            // You can add a loading screen here if needed
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
        is AuthState.Error -> {
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
    }
}