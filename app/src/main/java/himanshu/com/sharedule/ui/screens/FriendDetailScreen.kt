package himanshu.com.sharedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import himanshu.com.sharedule.model.Friend
import androidx.compose.ui.platform.LocalContext
import himanshu.com.sharedule.database.entity.DailyTask
import himanshu.com.sharedule.repository.DailyTaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FriendDetailScreen(friend: Friend, onBack: () -> Unit) {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf<List<DailyTask>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Today, 1 = Overall
    val scope = rememberCoroutineScope()

    // Fetch all tasks for the friend
    LaunchedEffect(friend.uid) {
        loading = true
        tasks = DailyTaskRepository(context).getTasksForUser(friend.uid)
        loading = false
    }

    // Calculate today's midnight
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val todayTasks = tasks.filter { it.date == today }
    val overallTasks = tasks

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8))
    ) {
        // Top banner with gradient and back button
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
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "Friend Details",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF9C27B0))
                Spacer(Modifier.height(8.dp))
                // Score line
                val (completed, total) = if (selectedTab == 0) {
                    val c = todayTasks.count { it.isDone }
                    val t = todayTasks.size
                    c to t
                } else {
                    val c = overallTasks.count { it.isDone }
                    val t = overallTasks.size
                    c to t
                }
                Text("Score: $completed/$total tasks completed", fontSize = 15.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text("UID: ${friend.uid}", fontSize = 14.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(8.dp))
        // Tabs for Today and Overall
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(selected = selectedTab == 0, text = "Today") { selectedTab = 0 }
            TabButton(selected = selectedTab == 1, text = "Overall") { selectedTab = 1 }
        }
        Spacer(Modifier.height(8.dp))
        if (loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF9C27B0))
            }
        } else {
            val showTasks = if (selectedTab == 0) todayTasks else overallTasks
            if (showTasks.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tasks found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f, fill = false)
                ) {
                    items(showTasks.size) { index ->
                        FriendTaskItem(showTasks[index])
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(selected: Boolean, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF9C27B0) else Color(0xFFE1BEE7)
        ),
        shape = RoundedCornerShape(20.dp),

        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Text(text, color = if (selected) Color.White else Color.Black)
    }
}

@Composable
private fun FriendTaskItem(task: DailyTask) {
    val isDone = task.isDone
    if (task.recurrence == null) "One Time" else task.recurrence!!.type.toString()
    val accentColor = if (isDone) Color(0xFF4CAF50) else Color(0xFFE91E63)
    val bgGradient = Brush.linearGradient(
        colors = if (isDone)
            listOf(Color(0xFFE8F5E9), Color(0xFFFAFAFA))
        else
            listOf(Color(0xFFFFF3E0), Color(0xFFFAFAFA)),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(400f, 400f)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .background(bgGradient, shape = RoundedCornerShape(20.dp))
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar
            Box(
                Modifier
                    .width(8.dp)
                    .height(80.dp)
                    .background(accentColor, shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            )
            Spacer(Modifier.width(16.dp))
            // Icon
            Icon(
                if (isDone) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f).padding(16.dp)) {
                Text(
                    task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF222222),
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        task.description ?: "",
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isDone) "Done" else "Pending",
                        color = if (isDone) Color(0xFF4CAF50) else Color(0xFFE91E63),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${task.expectedHours}h",
                        fontSize = 12.sp,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
        }
    }
}
