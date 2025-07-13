package himanshu.com.sharedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import himanshu.com.sharedule.model.DailyTaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush

@Composable
fun DailyProgressScreen(viewModel: DailyTaskViewModel, onProfileClick: () -> Unit) {
    val allTasks by viewModel.allTasks.collectAsState()
    val grouped = allTasks.groupBy { formatDate(it.date) }
    val totalTasks = allTasks.size
    val totalDone = allTasks.count { it.isDone }
    val overallRatio = if (totalTasks > 0) totalDone.toFloat() / totalTasks else 0f
    val daysTracked = grouped.size
    val bestDay = grouped.maxByOrNull { it.value.count { t -> t.isDone } }?.key
    val bestDayCount = grouped.maxOfOrNull { it.value.count { t -> t.isDone } } ?: 0
    val streak = calculateStreak(grouped)

    // Bar graph state
    val todayMidnight = getTodayMidnightMillis()
    var barStartDay by remember { mutableStateOf(todayMidnight) }
    val barDays = (0..6).map { offset -> barStartDay - offset * 24 * 60 * 60 * 1000L }.reversed()
    val filteredBarDays = barDays.filter { it <= todayMidnight }
    val barLabels = filteredBarDays.map { SimpleDateFormat("EEE", Locale.getDefault()).format(Date(it)) }
    val barData = filteredBarDays.map { dayMillis ->
        val dayKey = formatDate(dayMillis)
        grouped[dayKey]?.count { it.isDone } ?: 0
    }
    val barTaskCounts = filteredBarDays.map { dayMillis ->
        val dayKey = formatDate(dayMillis)
        grouped[dayKey]?.size ?: 0
    }
    var selectedDay by remember { mutableStateOf(filteredBarDays.lastOrNull() ?: todayMidnight) }
    val selectedDayKey = formatDate(selectedDay)
    val selectedTasks = grouped[selectedDayKey] ?: emptyList()

    // Progress bar for 7-day window
    val windowTotal = barTaskCounts.sum()
    val windowDone = barData.sum()
    val windowRatio = if (windowTotal > 0) windowDone.toFloat() / windowTotal else 0f
    val animatedProgress = animateFloatAsState(targetValue = windowRatio, animationSpec = tween(600))

    var showAllDialog by remember { mutableStateOf(false) }
    var dialogTasks by remember { mutableStateOf<List<himanshu.com.sharedule.model.DailyTask>>(emptyList()) }
    var dialogDay by remember { mutableStateOf(selectedDay) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8)) // Soft pink background
            .padding(0.dp)
            .verticalScroll(scrollState)
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
            Text(
                "Progress",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 48.dp)
            )
            Button(
                onClick = { 
                    println("DailyProgressScreen: Profile button clicked")
                    onProfileClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 40.dp)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Profile", color = Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        
        // Main content with proper padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // All Time Card at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("All Time", fontSize = 18.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                        CircularProgressIndicator(
                            progress = overallRatio,
                            strokeWidth = 10.dp,
                            color = if (overallRatio == 1f) Color(0xFF4CAF50) else Color(0xFFE91E63),
                            trackColor = Color(0xFFE1BEE7),
                            modifier = Modifier.size(150.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$totalDone/$totalTasks", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (overallRatio == 1f) Color(0xFF4CAF50) else Color(0xFFE91E63))
                            Text("Completed", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Days Tracked: $daysTracked", fontSize = 14.sp, color = Color.Gray)
                        Text("Current Streak: $streak", fontSize = 14.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(4.dp))
                    if (bestDay != null && bestDayCount > 0) {
                        Text("Best Day: $bestDay ($bestDayCount tasks completed)", fontSize = 14.sp, color = Color(0xFF4CAF50))
                    }
                }
            }
            Divider(Modifier.padding(vertical = 16.dp), color = Color(0x11000000), thickness = 1.dp)
            
            // Horizontal progress bar for 7-day window
            Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                LinearProgressIndicator(
                    progress = animatedProgress.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFFE91E63),
                    trackColor = Color(0xFFE1BEE7)
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(windowRatio * 100).toInt()}% completed", fontSize = 13.sp, color = Color.Gray)
                    Text("${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(filteredBarDays.lastOrNull() ?: todayMidnight))}", fontSize = 13.sp, color = Color.Gray)
                }
            }
            
            Text("Daily Progress", fontSize = 18.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.size(16.dp))
            
            // Bar graph navigation
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { barStartDay -= 24 * 60 * 60 * 1000L }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous", tint = Color(0xFFE91E63))
                }
                Spacer(Modifier.width(8.dp))
                Text("Last 7 Days", fontSize = 16.sp, color = Color.Gray)
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { barStartDay += 24 * 60 * 60 * 1000L }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color(0xFFE91E63))
                }
            }

            // Bar graph
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
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(label, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Selected day tasks
            Text("Tasks for ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDay))}", fontSize = 16.sp, color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            
            if (selectedTasks.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "No tasks", tint = Color(0xFFE1BEE7), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("No tasks for this day.", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                val completed = selectedTasks.filter { it.isDone }
                val notCompleted = selectedTasks.filter { !it.isDone }
                if (completed.isNotEmpty()) {
                    Text("Completed", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Column(Modifier.fillMaxWidth()) {
                        completed.take(4).forEach { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(task.title, color = Color(0xFF388E3C), fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        if (completed.size > 4) {
                            TextButton(onClick = {
                                dialogTasks = completed
                                dialogDay = selectedDay
                                showAllDialog = true
                            }, modifier = Modifier.align(Alignment.End)) {
                                Text("View All", color = Color(0xFFE91E63))
                            }
                        }
                    }
                }
                if (notCompleted.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Not Completed", fontWeight = FontWeight.Bold, color = Color(0xFFE91E63), fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Column(Modifier.fillMaxWidth()) {
                        notCompleted.take(4).forEach { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Not done", tint = Color(0xFFE91E63), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(task.title, color = Color(0xFFE91E63), fontSize = 15.sp, fontWeight = FontWeight.Normal)
                                }
                            }
                        }
                        if (notCompleted.size > 4) {
                            TextButton(onClick = {
                                dialogTasks = notCompleted
                                dialogDay = selectedDay
                                showAllDialog = true
                            }, modifier = Modifier.align(Alignment.End)) {
                                Text("View All", color = Color(0xFFE91E63))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    
    if (showAllDialog) {
        AlertDialog(
            onDismissRequest = { showAllDialog = false },
            title = { Text("Tasks for ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dialogDay))}") },
            text = {
                LazyColumn {
                    items(dialogTasks) { task ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = if (task.isDone) "Done" else "Not done",
                                tint = if (task.isDone) Color(0xFF4CAF50) else Color(0xFFE91E63),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(task.title, color = if (task.isDone) Color(0xFF388E3C) else Color(0xFFE91E63))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun calculateStreak(grouped: Map<String, List<*>>): Int {
    val today = Calendar.getInstance()
    var streak = 0
    val sortedDates = grouped.keys.sortedDescending()
    for (dateStr in sortedDates) {
        val cal = Calendar.getInstance()
        try {
            cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: continue
        } catch (e: Exception) {
            continue
        }
        if (streak == 0) {
            // Start streak from today or yesterday
            val diff = ((today.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (diff > 1) break
        }
        streak++
    }
    return streak
}

private fun getTodayMidnightMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
} 