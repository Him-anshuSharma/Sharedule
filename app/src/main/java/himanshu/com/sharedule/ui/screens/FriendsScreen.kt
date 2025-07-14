package himanshu.com.sharedule.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import himanshu.com.sharedule.model.Friend
import himanshu.com.sharedule.ui.viewmodels.FriendViewModel
import androidx.compose.runtime.LaunchedEffect
import himanshu.com.sharedule.model.FriendRequest
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import himanshu.com.sharedule.repository.DailyTaskRepository
import java.text.SimpleDateFormat
import java.util.*
import himanshu.com.sharedule.ui.viewmodels.DailyTaskViewModel

@Composable
fun FriendsScreen(viewModel: FriendViewModel, onFriendClick: (Friend) -> Unit = {}) {
    val friends by viewModel.friends.collectAsState()
    var showRequestsDialog by remember { mutableStateOf(false) }
    var friendUidToAdd by remember { mutableStateOf("") }
    var showSendSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8))
    ) {
        // Top banner with gradient
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
            Text(
                "Friends",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 48.dp)
            )
            Button(
                onClick = { showRequestsDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Friend Requests", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Requests", color = Color.White)
            }
        }
        Spacer(Modifier.height(16.dp))
        // Add Friend by UID
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Add Friend by UID", fontSize = 18.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = friendUidToAdd,
                    onValueChange = { friendUidToAdd = it },
                    label = { Text("Enter UID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        viewModel.sendFriendRequest(friendUidToAdd)
                        friendUidToAdd = ""
                        showSendSuccess = true
                    },
                    enabled = friendUidToAdd.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send Request")
                }
                if (showSendSuccess) {
                    Text("Request sent!", color = Color(0xFF4CAF50), modifier = Modifier.padding(top = 8.dp))
                    LaunchedEffect(showSendSuccess) {
                        kotlinx.coroutines.delay(1500)
                        showSendSuccess = false
                    }
                }
            }
        }
        // Friends List Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Your Friends", fontSize = 18.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (friends.isEmpty()) {
                    Text("No friends yet!", color = Color.Gray, fontSize = 16.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(friends) { friend ->
                            FriendCard(friend, onClick = { onFriendClick(friend) })
                        }
                    }
                }
            }
        }
    }
    if (showRequestsDialog) {
        FriendRequestsDialog(onDismiss = { showRequestsDialog = false }, viewModel = viewModel)
    }
}

@Composable
fun FriendCard(friend: Friend, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Text(friend.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }
    }
}

@Composable
fun FriendRequestsDialog(onDismiss: () -> Unit, viewModel: FriendViewModel) {
    val requests by viewModel.friendRequests.collectAsState()
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        loading = true
        viewModel.getFriendRequests()
        loading = false
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Friend Requests") },
        text = {
            when {
                loading -> Text("Loading...")
                requests.isEmpty() -> Text("No pending friend requests.")
                else -> {
                    Column {
                        requests.map { req ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(req.displayName, modifier = Modifier.weight(1f))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.acceptFriendRequest(req)
                                            loading = true
                                            viewModel.getFriendRequests()
                                            loading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) { Text("Accept", color = Color.White) }
                                Button(
                                    onClick = {
                                        // TODO: Implement reject logic
                                        scope.launch {
                                            loading = true
                                            viewModel.getFriendRequests()
                                            loading = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) { Text("Reject", color = Color.White) }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun FriendProgressDialog(friend: Friend, onDismiss: () -> Unit,context: Context) {
    var tasks by remember { mutableStateOf<List<himanshu.com.sharedule.database.entity.DailyTask>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var progressMode by remember { mutableStateOf("Daily") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(friend.uid) {
        loading = true
        tasks = DailyTaskRepository(context).getTasksForUser(friend.uid)
        loading = false
    }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val todayTasks = tasks.filter { it.date == today }
    val todayTotal = todayTasks.size
    val todayCompleted = todayTasks.count { it.isDone }
    val todayPercent = if (todayTotal > 0) todayCompleted.toFloat() / todayTotal else 0f
    val weekDays = (0..6).map { offset -> today - offset * 24 * 60 * 60 * 1000L }.reversed()
    val weekLabels = weekDays.map { SimpleDateFormat("EEE", Locale.getDefault()).format(Date(it)) }
    val weekData = weekDays.map { dayMillis ->
        val dayTasks = tasks.filter { it.date == dayMillis }
        dayTasks.count { it.isDone }
    }
    val weekTotals = weekDays.map { dayMillis ->
        tasks.count { it.date == dayMillis }
    }
    val total = tasks.size
    val completed = tasks.count { it.isDone }
    val pending = tasks.count { !it.isDone }
    val completedPercent = if (total > 0) completed.toFloat() / total else 0f
    val pendingPercent = 1f - completedPercent
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${friend.name}'s Progress") },
        text = {
            if (loading) {
                Text("Loading...")
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Switch bar
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val selectedColor = Color(0xFF9C27B0)
                        val unselectedColor = Color(0xFFE1BEE7)
                        Button(
                            onClick = { progressMode = "Daily" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (progressMode == "Daily") selectedColor else unselectedColor
                            ),
                            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Daily", color = if (progressMode == "Daily") Color.White else Color.Black) }
                        Button(
                            onClick = { progressMode = "Weekly" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (progressMode == "Weekly") selectedColor else unselectedColor
                            ),
                            shape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Weekly", color = if (progressMode == "Weekly") Color.White else Color.Black) }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (progressMode == "Daily") {
                        // Daily progress chart
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val sweepCompleted = todayPercent * 360f
                                val sweepPending = (1f - todayPercent) * 360f
                                drawArc(
                                    color = Color(0xFF4CAF50),
                                    startAngle = -90f,
                                    sweepAngle = sweepCompleted,
                                    useCenter = true
                                )
                                drawArc(
                                    color = Color(0xFFE91E63),
                                    startAngle = -90f + sweepCompleted,
                                    sweepAngle = sweepPending,
                                    useCenter = true
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$todayCompleted/$todayTotal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (todayPercent == 1f) Color(0xFF4CAF50) else Color(0xFFE91E63))
                                Text("Completed Today", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            DetailChip(label = "Total", value = todayTotal.toString(), color = Color(0xFFE91E63))
                            DetailChip(label = "Completed", value = todayCompleted.toString(), color = Color(0xFF4CAF50))
                            DetailChip(label = "Pending", value = (todayTotal - todayCompleted).toString(), color = Color(0xFFFF9800))
                        }
                    } else {
                        // Weekly bar chart
                        val maxBar = (weekData.maxOrNull() ?: 1).coerceAtLeast(1)
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            weekLabels.forEachIndexed { idx, label ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        Modifier
                                            .width(18.dp)
                                            .height((weekData[idx].toFloat() / maxBar * 60).coerceAtLeast(6f).dp)
                                            .background(
                                                if (weekData[idx] > 0) Color(0xFF4CAF50) else Color(0xFFE1BEE7),
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(label, fontSize = 12.sp, color = Color.Gray)
                                    Text("${weekData[idx]}/${weekTotals[idx]}", fontSize = 11.sp, color = Color(0xFF9C27B0))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(tasks) { task ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (task.isDone) Color(0xFF4CAF50) else Color(0xFFE91E63), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(task.title, fontWeight = FontWeight.SemiBold)
                                    Text("Expected Hours: ${task.expectedHours}", fontSize = 12.sp, color = Color(0xFF9C27B0))
                                    task.recurrence?.let {
                                        Text("Recurrence: ${it.type}", fontSize = 12.sp, color = Color(0xFF4CAF50))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
} 