package himanshu.com.sharedule.ui.screens

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import himanshu.com.sharedule.model.DailyTask
import himanshu.com.sharedule.model.DailyTaskViewModel
import himanshu.com.sharedule.model.Recurrence
import himanshu.com.sharedule.model.RecurrenceType
import himanshu.com.sharedule.model.SyncState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TextButton

@Composable
fun TodayScreen(viewModel: DailyTaskViewModel, onProfileClick: () -> Unit) {
    val todayTasks by viewModel.todayTasks.collectAsState()
    val error by viewModel.error.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val context = LocalContext.current
    val completed = todayTasks.count { it.isDone }
    val pending = todayTasks.count { !it.isDone }
    val total = todayTasks.size
    val completedPercent = if (total > 0) completed.toFloat() / total else 0f
    val pendingPercent = 1f - completedPercent

    var showAddDialog by remember { mutableStateOf(false) }
    var pieVisible by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF2F8)) // Soft pink background
                .padding(0.dp)
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
                    "Today",
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 48.dp)
                )
                Button(
                    onClick = { 
                        println("TodayScreen: Profile button clicked")
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
                // Sync status indicator
                SyncStatusCard(syncState = syncState, onSyncFromFirebase = { viewModel.syncFromFirebase() })
                
                AnimatedVisibility(visible = pieVisible) {
                    Column {
                        Text(
                            "Today's Overview", 
                            fontSize = 24.sp, 
                            color = Color(0xFFE91E63), 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            PieChart(
                                completedPercent = completedPercent,
                                pendingPercent = pendingPercent,
                                completed = completed,
                                pending = pending
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DetailChip(label = "Total", value = total.toString(), color = Color(0xFFE91E63))
                            DetailChip(label = "Completed", value = completed.toString(), color = Color(0xFF4CAF50))
                            DetailChip(label = "Pending", value = pending.toString(), color = Color(0xFFFF9800))
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
                Text(
                    "Today's Tasks", 
                    fontSize = 20.sp, 
                    color = Color(0xFF9C27B0), 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (todayTasks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "No tasks", tint = Color(0xFFE1BEE7), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("No tasks for today!", color = Color(0xFF9E9E9E), fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                            Spacer(Modifier.height(16.dp))
                            // Debug button to add sample tasks
                            Button(
                                onClick = { viewModel.addSampleTasks() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text("Add Sample Tasks", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    if (dragAmount < -10) pieVisible = false
                                    if (dragAmount > 10) pieVisible = true
                                }
                            },
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(todayTasks) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isDone) Color(0xFFFFFFFF) else Color(0xFFFFF3E0)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { checked ->
                                            viewModel.updateTask(task.copy(isDone = checked))
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF4CAF50),
                                            uncheckedColor = Color(0xFFE91E63)
                                        ),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (task.isDone) Color(0xFF388E3C) else Color(0xFFE91E63),
                                            fontSize = 18.sp
                                        )
                                        if (!task.description.isNullOrBlank()) {
                                            Text(
                                                text = task.description ?: "",
                                                color = Color.Gray,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    if (task.isDone) {
                                        Text("Done", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Text("Pending", color = Color(0xFFE91E63), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFE91E63)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Add Task", tint = Color.White)
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                confirmButton = {},
                title = { Text("Add Task") },
                text = {
                    AddTaskSection(onAdd = {
                        viewModel.addTask(it)
                        showAddDialog = false
                    }, context = context)
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(error ?: "", color = Color.Red, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Text("×", fontSize = 16.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun SyncStatusCard(syncState: SyncState, onSyncFromFirebase: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sync status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            when (syncState) {
                                is SyncState.Idle -> Color.Gray
                                is SyncState.SyncingToFirebase, is SyncState.SyncingFromFirebase -> Color(0xFFFF9800)
                                is SyncState.Synced -> Color(0xFF4CAF50)
                                is SyncState.Error -> Color(0xFFE91E63)
                            },
                            CircleShape
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (syncState) {
                        is SyncState.Idle -> "Ready"
                        is SyncState.SyncingToFirebase -> "Syncing to cloud..."
                        is SyncState.SyncingFromFirebase -> "Syncing from cloud..."
                        is SyncState.Synced -> "Synced"
                        is SyncState.Error -> "Sync error"
                    },
                    fontSize = 12.sp,
                    color = when (syncState) {
                        is SyncState.Idle -> Color.Gray
                        is SyncState.SyncingToFirebase, is SyncState.SyncingFromFirebase -> Color(0xFFFF9800)
                        is SyncState.Synced -> Color(0xFF4CAF50)
                        is SyncState.Error -> Color(0xFFE91E63)
                    }
                )
            }
            
            // Sync button
            TextButton(
                onClick = onSyncFromFirebase,
                enabled = syncState !is SyncState.SyncingFromFirebase && syncState !is SyncState.SyncingToFirebase
            ) {
                Text(
                    "Sync",
                    fontSize = 12.sp,
                    color = Color(0xFFE91E63)
                )
            }
        }
    }
}

@Composable
fun PieChart(completedPercent: Float, pendingPercent: Float, completed: Int, pending: Int) {
    Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val sweepCompleted = completedPercent * 360f
            val sweepPending = pendingPercent * 360f
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
        // Beautiful centered text with better styling
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "$completed/$pending", 
                fontSize = 26.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White
            )
            Text(
                "Done/Pending", 
                fontSize = 14.sp, 
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DetailChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(8.dp))
            Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(6.dp))
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddTaskSection(onAdd: (DailyTask) -> Unit, context: Context) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showRecurrence by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var daysOfWeek by remember { mutableStateOf(listOf<Int>()) }
    var interval by remember { mutableStateOf(1) }
    var date by remember { mutableStateOf(getTodayMidnightMillis()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text("Add New Task", fontSize = 20.sp, color = Color(0xFF667eea), modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                unfocusedBorderColor = Color.Gray
            )
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667eea),
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Due Date:", fontSize = 16.sp, color = Color.Gray)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(date)),
                onValueChange = {
                    try {
                        date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(it)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        // Ignore invalid date input
                    }
                },
                label = { Text("Due Date") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color.Gray
                )
            )
            IconButton(onClick = { showRecurrence = !showRecurrence }) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Recurrence")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (showRecurrence) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text("Recurrence:", fontSize = 16.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = recurrenceType == RecurrenceType.NONE,
                        onClick = { recurrenceType = RecurrenceType.NONE }
                    )
                    Text("None", fontSize = 16.sp)
                    Spacer(Modifier.width(20.dp))
                    RadioButton(
                        selected = recurrenceType == RecurrenceType.DAILY,
                        onClick = { recurrenceType = RecurrenceType.DAILY }
                    )
                    Text("Daily", fontSize = 16.sp)
                    Spacer(Modifier.width(20.dp))
                    RadioButton(
                        selected = recurrenceType == RecurrenceType.WEEKLY,
                        onClick = { recurrenceType = RecurrenceType.WEEKLY }
                    )
                    Text("Weekly", fontSize = 16.sp)
                    Spacer(Modifier.width(20.dp))
                    RadioButton(
                        selected = recurrenceType == RecurrenceType.CUSTOM,
                        onClick = { recurrenceType = RecurrenceType.CUSTOM }
                    )
                    Text("Custom", fontSize = 16.sp)
                }

                when (recurrenceType) {
                    RecurrenceType.NONE -> {
                        // No recurrence fields
                    }
                    RecurrenceType.DAILY -> {
                        // No recurrence fields
                    }
                    RecurrenceType.WEEKLY -> {
                        Text("Days of the week:", fontSize = 16.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.MONDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.MONDAY
                                    else daysOfWeek -= Calendar.MONDAY
                                }
                            )
                            Text("Mon", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.TUESDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.TUESDAY
                                    else daysOfWeek -= Calendar.TUESDAY
                                }
                            )
                            Text("Tue", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.WEDNESDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.WEDNESDAY
                                    else daysOfWeek -= Calendar.WEDNESDAY
                                }
                            )
                            Text("Wed", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.THURSDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.THURSDAY
                                    else daysOfWeek -= Calendar.THURSDAY
                                }
                            )
                            Text("Thu", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.FRIDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.FRIDAY
                                    else daysOfWeek -= Calendar.FRIDAY
                                }
                            )
                            Text("Fri", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.SATURDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.SATURDAY
                                    else daysOfWeek -= Calendar.SATURDAY
                                }
                            )
                            Text("Sat", fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Checkbox(
                                checked = daysOfWeek.contains(Calendar.SUNDAY),
                                onCheckedChange = { checked ->
                                    if (checked) daysOfWeek += Calendar.SUNDAY
                                    else daysOfWeek -= Calendar.SUNDAY
                                }
                            )
                            Text("Sun", fontSize = 16.sp)
                        }
                    }
                    RecurrenceType.CUSTOM -> {
                        Text("Interval (days):", fontSize = 16.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = interval.toString(),
                            onValueChange = {
                                try {
                                    interval = it.toInt()
                                } catch (e: NumberFormatException) {
                                    // Ignore invalid input
                                }
                            },
                            label = { Text("Interval") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF667eea),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val recurrence = when (recurrenceType) {
                    RecurrenceType.NONE -> null
                    RecurrenceType.DAILY -> Recurrence(RecurrenceType.DAILY)
                    RecurrenceType.WEEKLY -> Recurrence(RecurrenceType.WEEKLY, daysOfWeek = daysOfWeek)
                    RecurrenceType.CUSTOM -> Recurrence(RecurrenceType.CUSTOM, interval = interval)
                }
                onAdd(
                    DailyTask(
                        title = title,
                        description = description.takeIf { it.isNotBlank() },
                        date = if (recurrenceType == RecurrenceType.NONE) getTodayMidnightMillis() else System.currentTimeMillis(),
                        recurrence = recurrence
                    )
                )
                title = ""
                description = ""
                recurrenceType = RecurrenceType.NONE
                daysOfWeek = listOf()
                interval = 1
                date = getTodayMidnightMillis()
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
    }
}

private fun getTodayMidnightMillis(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
} 