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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    onBackPressed: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profileData by profileViewModel.profileData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    
    // Load profile data when screen is first displayed
    LaunchedEffect(Unit) {
        // Get current user from Firebase Auth
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            profileViewModel.loadProfileData(context, user)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with back button and refresh
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = { 
                            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            currentUser?.let { user ->
                                profileViewModel.loadProfileData(context, user)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                }

                profileData?.let { data ->
                    // User Profile Section
                    UserProfileSection(data.accountInfo)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Account Information Section
                    AccountInfoSection(data.accountInfo)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Device Status Section
                    DeviceStatusSection(data.deviceState)
                }

                // Error Display
                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ErrorCard(error!!) {
                        profileViewModel.clearError()
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileSection(accountInfo: himanshu.com.sharedule.ui.viewmodels.AccountInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
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
                        .background(Color(0xFF667eea)),
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
                text = accountInfo.displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email
            Text(
                text = accountInfo.email,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            // Email Verification Status
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (accountInfo.isEmailVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = "Email Verification",
                    tint = if (accountInfo.isEmailVerified) Color.Green else Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (accountInfo.isEmailVerified) "Email Verified" else "Email Not Verified",
                    fontSize = 12.sp,
                    color = if (accountInfo.isEmailVerified) Color.Green else Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun AccountInfoSection(accountInfo: himanshu.com.sharedule.ui.viewmodels.AccountInfo) {
    InfoSection(
        title = "Account Information",
        icon = Icons.Default.AccountCircle
    ) {
        InfoRow("User ID", accountInfo.uid, isMonospace = true)
        InfoRow("Phone", accountInfo.phoneNumber)
        InfoRow("Account Created", accountInfo.accountCreated)
        InfoRow("Last Sign In", accountInfo.lastSignIn)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF667eea),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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
            .padding(vertical = 4.dp),
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
            color = Color.Black,
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
            .padding(vertical = 4.dp),
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
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color.Green else Color.Red
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isActive) "Active" else "Inactive",
                fontSize = 14.sp,
                color = if (isActive) Color.Green else Color.Red,
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
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
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
                color = Color.Red,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            
            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "Unknown"
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
} 