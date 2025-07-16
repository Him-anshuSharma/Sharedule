package himanshu.com.sharedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import himanshu.com.sharedule.ui.viewmodels.ProfileViewModel
import himanshu.com.sharedule.ui.viewmodels.DeviceState
import himanshu.com.sharedule.ui.viewmodels.AccountInfo
import himanshu.com.sharedule.auth.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.clickable

@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val profileData by profileViewModel.profileData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val context = LocalContext.current

    // Load profile data when the screen is first displayed
    LaunchedEffect(Unit) {
        profileViewModel.loadProfileData(context)
    }
    


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8)) // Soft pink background
    ) {
        // Top banner with Taylor Swift inspired gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE91E63), // Pink
                            Color(0xFF9C27B0)  // Purple
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { 
                        println("ProfileScreen: Refresh button clicked")
                        profileViewModel.loadProfileData(context)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            }
        }

        // Main content with proper padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFE91E63)
                    )
                }
            } else {
                if (profileData != null) {
                    val data = profileData!!
                    
                    // User Profile Section
                    UserProfileSection(data.accountInfo)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Account Information Section
                    AccountInfoSection(data.accountInfo)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Device Status Section
                    DeviceStatusSection(data.deviceState)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Logout Section
                    LogoutSection()
                } else {
                    // Show a placeholder when no profile data is available
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "No Profile Data",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color(0xFFE91E63)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Profile Data Available",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9C27B0)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Profile information will appear here once loaded",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { profileViewModel.loadProfileData(context) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE91E63)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retry Loading")
                                }
                            }
                        }
                    }
                }

                // Error Display
                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorCard(error!!) {
                        profileViewModel.clearError()
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}



@Composable
private fun UserProfileSection(accountInfo: AccountInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            if (accountInfo.photoUrl != null) {
                AsyncImage(
                    model = accountInfo.photoUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User Name
            Text(
                text = accountInfo.displayName.ifEmpty { "Unknown User" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email
            Text(
                text = accountInfo.email.ifEmpty { "No email available" },
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AccountInfoSection(accountInfo: AccountInfo) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    InfoSection(
        title = "Account Information",
        icon = Icons.Default.AccountCircle
    ) {
        // UID row with tap-to-copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable {
                    clipboardManager.setText(AnnotatedString(accountInfo.uid))
                    Toast.makeText(context, "UID copied!", Toast.LENGTH_SHORT).show()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User ID",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = accountInfo.uid.ifEmpty { "Unknown" },
                fontSize = 14.sp,
                color = Color(0xFF9C27B0),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        InfoRow("Email", accountInfo.email.ifEmpty { "No email" })
        InfoRow("Display Name", accountInfo.displayName.ifEmpty { "Unknown" })
        InfoRow("Account Created", accountInfo.accountCreated.ifEmpty { "Unknown" })
        InfoRow("Last Sign In", accountInfo.lastSignIn.ifEmpty { "Unknown" })
    }
}

@Composable
private fun DeviceStatusSection(deviceState: DeviceState) {
    InfoSection(
        title = "Device Status",
        icon = Icons.Default.Info
    ) {
        StatusRow("Silent Mode", deviceState.isSilentMode)
        StatusRow("Do Not Disturb", deviceState.isDndMode)
        StatusRow("Internet Connected", deviceState.isInternetConnected)
        InfoRow("Last Connected", formatDate(deviceState.lastConnected))
        InfoRow("Last Updated", formatDate(deviceState.lastUpdated))
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section Content
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF9C27B0),
            fontWeight = if (isMonospace) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color(0xFF4CAF50) else Color(0xFFE91E63)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) "Active" else "Inactive",
                fontSize = 14.sp,
                color = if (isActive) Color(0xFF4CAF50) else Color(0xFFE91E63),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE91E63).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = error,
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            
            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 

private fun formatDate(timestamp: Long): String {
    return try {
        if (timestamp == 0L) return "Unknown"
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        "Unknown"
    }
}

@Composable
private fun LogoutSection() {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDataRemovalDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0).copy(alpha = 0.95f) // Warm orange background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Account Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Data Removal Button
            OutlinedButton(
                onClick = { showDataRemovalDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFE91E63)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Remove Data",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout & Remove All Data",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9C27B0)
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to logout? This will clear all local data and sign you out of your account.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout(context)
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF9C27B0)
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Data Removal Confirmation Dialog
    if (showDataRemovalDialog) {
        AlertDialog(
            onDismissRequest = { showDataRemovalDialog = false },
            title = {
                Text(
                    text = "Remove All Data",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
            },
            text = {
                Text(
                    text = "This action will permanently delete all your data from both this device and the cloud. This action cannot be undone. Are you absolutely sure?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logoutWithDataRemoval(context)
                        showDataRemovalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    )
                ) {
                    Text("Delete All Data & Logout")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDataRemovalDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF9C27B0)
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
} 