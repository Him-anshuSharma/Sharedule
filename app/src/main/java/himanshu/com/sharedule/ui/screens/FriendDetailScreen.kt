package himanshu.com.sharedule.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import himanshu.com.sharedule.ui.screens.ModernTaskCard
import java.util.Date
import java.util.Locale

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
            // --- Progress Bar Graph (copied/adapted from DailyProgressScreen) ---
            val grouped = tasks.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date)) }
            val todayMidnight = today
            val barDays = (0..6).map { offset -> todayMidnight - offset * 24 * 60 * 60 * 1000L }.reversed()
            val filteredBarDays = barDays.filter { it <= todayMidnight }
            val barLabels = filteredBarDays.map { SimpleDateFormat("EEE", Locale.getDefault()).format(Date(it)) }
            val barData = filteredBarDays.map { dayMillis ->
                val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dayMillis))
                grouped[dayKey]?.count { it.isDone } ?: 0
            }
            val barTaskCounts = filteredBarDays.map { dayMillis ->
                val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dayMillis))
                grouped[dayKey]?.size ?: 0
            }
            val maxBarValue = (barData.maxOrNull() ?: 1).coerceAtLeast(1)
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    barLabels.forEachIndexed { index, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .width(30.dp)
                                    .height((barData[index].toFloat() / maxBarValue * 100).coerceAtLeast(10f).dp)
                                    .background(
                                        if (barData[index] > 0) Color(0xFFE91E63) else Color(0xFFE1BEE7),
                                        androidx.compose.foundation.shape.RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(label, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- Friend's Tasks using ModernTaskCard ---
            val showTasks = if (selectedTab == 0) todayTasks else overallTasks
            if (showTasks.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tasks found.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                        .weight(1f, fill = false)
                ) {
                    items(showTasks) { task ->
                        ModernTaskCard(
                            task = task,
                            showCheckbox = false
                        )
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
