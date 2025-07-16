package himanshu.com.sharedule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import himanshu.com.sharedule.model.Friend
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import himanshu.com.sharedule.auth.AuthState
import himanshu.com.sharedule.auth.AuthViewModel
import himanshu.com.sharedule.config.AppConfig
import himanshu.com.sharedule.services.DeviceStatusService
import himanshu.com.sharedule.ui.theme.ShareduleTheme
import himanshu.com.sharedule.ui.viewmodels.DailyTaskViewModel
import himanshu.com.sharedule.ui.viewmodels.FriendViewModel
import himanshu.com.sharedule.ui.screens.*
import androidx.activity.compose.BackHandler

sealed class MainNavItem(val label: String, val icon: ImageVector) {
    object Today : MainNavItem("Today", Icons.Default.DateRange)
    object Progress : MainNavItem("Progress", Icons.Default.CheckCircle)
    object Friends : MainNavItem("Friends", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize configuration
        AppConfig.initialize(this)
        
        // Start device state monitoring
        DeviceStatusService.startMonitoring(this)

        setContent {
            enableEdgeToEdge()
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
    var showProfile by remember { mutableStateOf(false) }
    var selectedFriend: Friend? by remember { mutableStateOf(null) }
    
    // Debug: Track showProfile state changes
    LaunchedEffect(showProfile) {
        println("MainActivity: showProfile changed to: $showProfile")
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val dailyTaskViewModel = remember { DailyTaskViewModel(context) }
    val friendViewModel = remember { FriendViewModel(context,) }
    var selectedTab by remember { mutableStateOf<MainNavItem>(MainNavItem.Today) }

    // Handle system back button
    BackHandler(enabled = true) {
        when {
            // If on a main tab (with nav bar), exit app
            !showProfile && selectedFriend == null && authState is AuthState.SignedIn -> {
                // Exit app
                (context as? android.app.Activity)?.finish()
            }
            // If in profile or friend detail, go back to previous screen
            showProfile -> showProfile = false
            selectedFriend != null -> selectedFriend = null
        }
    }

    when {
        showProfile && authState is AuthState.SignedIn -> {
            ProfileScreen(
                onBackPressed = {
                    showProfile = false
                }
            )
        }
        selectedFriend != null && authState is AuthState.SignedIn -> {
            FriendDetailScreen(friend = selectedFriend!!, onBack = { selectedFriend = null })
        }
        authState is AuthState.SignedIn -> {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        is MainNavItem.Today -> TodayScreen(viewModel = dailyTaskViewModel, onProfileClick = { 
                            println("MainActivity: Profile button clicked from TodayScreen")
                            showProfile = true 
                        })
                        is MainNavItem.Progress -> DailyProgressScreen(viewModel = dailyTaskViewModel, onProfileClick = { 
                            println("MainActivity: Profile button clicked from DailyProgressScreen")
                            showProfile = true 
                        })
                        is MainNavItem.Friends -> FriendsScreen(viewModel = friendViewModel, onFriendClick = { friend -> selectedFriend = friend })
                    }
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab is MainNavItem.Today,
                        onClick = { selectedTab = MainNavItem.Today },
                        icon = { Icon(MainNavItem.Today.icon, contentDescription = MainNavItem.Today.label) },
                        label = { Text(MainNavItem.Today.label) }
                    )
                    NavigationBarItem(
                        selected = selectedTab is MainNavItem.Progress,
                        onClick = { selectedTab = MainNavItem.Progress },
                        icon = { Icon(MainNavItem.Progress.icon, contentDescription = MainNavItem.Progress.label) },
                        label = { Text(MainNavItem.Progress.label) }
                    )
                    NavigationBarItem(
                        selected = selectedTab is MainNavItem.Friends,
                        onClick = { selectedTab = MainNavItem.Friends },
                        icon = { Icon(MainNavItem.Friends.icon, contentDescription = MainNavItem.Friends.label) },
                        label = { Text(MainNavItem.Friends.label) }
                    )
                }
            }
        }
        authState is AuthState.SignedOut || authState is AuthState.Initial -> {
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
        authState is AuthState.Loading -> {
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
        authState is AuthState.Error -> {
            LoginScreen(
                onSignInSuccess = {
                    // Navigation will be handled automatically by the state change
                }
            )
        }
    }
}