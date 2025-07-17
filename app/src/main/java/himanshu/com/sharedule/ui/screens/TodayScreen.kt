package himanshu.com.sharedule.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import himanshu.com.sharedule.R
import himanshu.com.sharedule.database.entity.DailyTask
import himanshu.com.sharedule.database.entity.Recurrence
import himanshu.com.sharedule.repository.SyncState
import himanshu.com.sharedule.ui.viewmodels.DailyTaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import himanshu.com.sharedule.ui.theme.ShareduleGreen
import himanshu.com.sharedule.ui.theme.SharedulePink
import himanshu.com.sharedule.ui.theme.ShareduleLightGreen
import himanshu.com.sharedule.ui.theme.ShareduleLightYellow
import himanshu.com.sharedule.ui.theme.ShareduleGrey
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.window.Dialog

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
    val listState = rememberLazyListState()
    var activeTaskForActions by remember { mutableStateOf<DailyTask?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val collapseThreshold = 10
    val expandThreshold = 1
    var overviewCollapsed by remember { mutableStateOf(false) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (!overviewCollapsed && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > collapseThreshold)) {
            overviewCollapsed = true
        } else if (overviewCollapsed && listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < expandThreshold) {
            overviewCollapsed = false
        }
    }

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
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 48.dp)
                )
                Button(
                    onClick = {
                        println("TodayScreen: Profile button clicked")
                        onProfileClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 40.dp)
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
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
                SyncStatusCard(
                    syncState = syncState,
                    onSyncFromFirebase = { viewModel.syncFromFirebase() })

                AnimatedContent(targetState = overviewCollapsed, label = "overviewCollapseAnim") { collapsed ->
                    if (!collapsed && pieVisible) {
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
                                DetailChip(
                                    label = "Total",
                                    value = total.toString(),
                                    color = Color(0xFFE91E63)
                                )
                                DetailChip(
                                    label = "Completed",
                                    value = completed.toString(),
                                    color = Color(0xFF4CAF50)
                                )
                                DetailChip(
                                    label = "Pending",
                                    value = pending.toString(),
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    } else if (collapsed && pieVisible) {
                        // Compact row layout
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PieChart(
                                completedPercent = completedPercent,
                                pendingPercent = pendingPercent,
                                completed = completed,
                                pending = pending,
                                modifier = Modifier.size(64.dp),
                                textSize = 14.sp,
                                showLabel = false
                            )
                            Spacer(Modifier.width(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                DotNumberChip(
                                    value = total.toString(),
                                    color = Color(0xFFE91E63)
                                )
                                DotNumberChip(
                                    value = completed.toString(),
                                    color = Color(0xFF4CAF50)
                                )
                                DotNumberChip(
                                    value = pending.toString(),
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
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
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "No tasks",
                                tint = Color(0xFFE1BEE7),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "No tasks for today!",
                                color = Color(0xFF9E9E9E),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            // Debug button to add sample tasks
                            Button(
                                onClick = { viewModel.addSampleTasks() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFE91E63
                                    )
                                ),
                                modifier = Modifier.padding(horizontal = 32.dp)
                            ) {
                                Text("Add Sample Tasks", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    if (dragAmount < -10) pieVisible = false
                                    if (dragAmount > 10) pieVisible = true
                                }
                            },
                        contentPadding = PaddingValues(top = 16.dp, bottom = 60.dp)
                    ) {
                        items(todayTasks.size) { index ->
                            ModernTaskCard(
                                task = todayTasks[index],
                                onCheckedChange = { checked ->
                                    viewModel.updateTask(todayTasks[index].copy(isDone = checked, updatedAt = System.currentTimeMillis()))
                                },
                                onTaskUpdate = { updatedTask ->
                                    viewModel.updateTask(updatedTask.copy(updatedAt = System.currentTimeMillis()))
                                },
                                onTaskDelete = { taskToDelete ->
                                    viewModel.deleteTask(taskToDelete)
                                },
                                onLongPress = { activeTaskForActions = todayTasks[index] }
                            )
                        }
                        // Add an invisible spacer at the end to ensure enough scrollable content
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
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
                modifier = Modifier.width(360.dp),
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
        if (activeTaskForActions != null) {
            Dialog(onDismissRequest = { activeTaskForActions = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier
                        .widthIn(min = 180.dp, max = 240.dp)
                        .padding(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            showEditDialog = true
                        }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_manage),
                                contentDescription = "Edit",
                                tint = SharedulePink,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = {
                            showDeleteDialog = true
                        }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                contentDescription = "Delete",
                                tint = SharedulePink,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { activeTaskForActions = null }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                                contentDescription = "Cancel",
                                tint = SharedulePink,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            // Show Edit dialog if requested
            if (showEditDialog) {
                EditTaskDialog(
                    initialTask = activeTaskForActions!!,
                    onDismiss = {
                        showEditDialog = false
                        activeTaskForActions = null
                    },
                    onSave = { updatedTask ->
                        viewModel.updateTask(updatedTask.copy(updatedAt = System.currentTimeMillis()))
                        showEditDialog = false
                        activeTaskForActions = null
                    }
                )
            }
            // Show Delete confirmation dialog if requested
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        activeTaskForActions = null
                    },
                    title = { Text("Delete Task") },
                    text = { Text("Are you sure you want to delete this task?") },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.deleteTask(activeTaskForActions!!)
                            showDeleteDialog = false
                            activeTaskForActions = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = SharedulePink)) {
                            Text("Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            activeTaskForActions = null
                        }) {
                            Text("Cancel")
                        }
                    }
                )
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
                                is SyncState.SyncingToFirebase, is SyncState.SyncingFromFirebase -> Color(
                                    0xFFFF9800
                                )

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
                        is SyncState.SyncingToFirebase, is SyncState.SyncingFromFirebase -> Color(
                            0xFFFF9800
                        )

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
fun PieChart(
    completedPercent: Float,
    pendingPercent: Float,
    completed: Int,
    pending: Int,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 26.sp,
    showLabel: Boolean = true
) {
    Box(modifier.then(Modifier.size(180.dp)), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(if (modifier == Modifier) 160.dp else 100.dp)) {
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "$completed/$pending",
                fontSize = textSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (showLabel) {
                Text(
                    "Done/Pending",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurrenceTypeChipRow(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        "NONE" to "None",
        "DAILY" to "Daily",
        "WEEKLY" to "Weekly"
    )
    FlowRow(
        modifier = modifier
    ) {
        types.forEach { (type, label) ->
            val selected = selectedType == type
            Surface(
                shape = RoundedCornerShape(corner = CornerSize(50)),
                color = if (selected) SharedulePink else Color(0xFFF3E5F5),
                border = if (selected) null else BorderStroke(1.dp, SharedulePink),
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .clickable { onTypeSelected(type) }
            ) {
                Text(
                    label,
                    color = if (selected) Color.White else SharedulePink,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyDaysDialog(
    initialDays: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val daysOfWeek = listOf(
        Calendar.MONDAY to "Mon",
        Calendar.TUESDAY to "Tue",
        Calendar.WEDNESDAY to "Wed",
        Calendar.THURSDAY to "Thu",
        Calendar.FRIDAY to "Fri",
        Calendar.SATURDAY to "Sat",
        Calendar.SUNDAY to "Sun"
    )
    var selectedDays by remember { mutableStateOf(initialDays) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days of the Week") },
        text = {
            FlowRow {
                daysOfWeek.forEach { (day, label) ->
                    val selected = selectedDays.contains(day)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) SharedulePink else Color(0xFFF3E5F5),
                        border = if (selected) null else BorderStroke(1.dp, SharedulePink),
                        modifier = Modifier
                            .padding(end = 8.dp, bottom = 8.dp)
                            .clickable {
                                selectedDays = if (selected) selectedDays - day else selectedDays + day
                            }
                    ) {
                        Text(
                            label,
                            color = if (selected) Color.White else SharedulePink,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedDays) }, colors = ButtonDefaults.buttonColors(containerColor = SharedulePink)) {
                Text("OK", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddTaskSection(onAdd: (DailyTask) -> Unit, context: Context) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showRecurrence by remember { mutableStateOf(false) }
    var recurrenceType by remember { mutableStateOf("NONE") }
    var daysOfWeek by remember { mutableStateOf(listOf<Int>()) }
    var interval by remember { mutableStateOf(1) }
    var date by remember { mutableStateOf(getTodayMidnightMillis()) }
    var showWeeklyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Add New Task",
            fontSize = 20.sp,
            color = Color(0xFF667eea),
            modifier = Modifier.padding(bottom = 16.dp)
        )
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
                        date = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(it)?.time
                            ?: System.currentTimeMillis()
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
                RecurrenceTypeChipRow(
                    selectedType = recurrenceType,
                    onTypeSelected = {
                        recurrenceType = it
                        if (it == "WEEKLY") showWeeklyDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (recurrenceType == "WEEKLY") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (daysOfWeek.isEmpty()) "No days selected" else daysOfWeek.sorted().joinToString(", ") { dayIntToLabel(it) },
                        color = SharedulePink,
                        fontSize = 14.sp
                    )
                    Button(onClick = { showWeeklyDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = SharedulePink)) {
                        Text("Choose Days", color = Color.White)
                    }
                }
            }
        }
        if (showWeeklyDialog) {
            WeeklyDaysDialog(
                initialDays = daysOfWeek,
                onDismiss = { showWeeklyDialog = false },
                onConfirm = {
                    daysOfWeek = it
                    showWeeklyDialog = false
                }
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val recurrence = when (recurrenceType) {
                    "NONE" -> null
                    "DAILY" -> Recurrence("DAILY")
                    "WEEKLY" -> Recurrence("WEEKLY", daysOfWeek = daysOfWeek)
                    else -> null // Not supported in chip UI
                }
                onAdd(
                    DailyTask(
                        title = title,
                        description = description.takeIf { it.isNotBlank() },
                        date = if (recurrenceType == "NONE") getTodayMidnightMillis() else System.currentTimeMillis(),
                        recurrence = recurrence
                    )
                )
                title = ""
                description = ""
                recurrenceType = "NONE"
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

private fun dayIntToLabel(day: Int): String = when (day) {
    Calendar.MONDAY -> "Mon"
    Calendar.TUESDAY -> "Tue"
    Calendar.WEDNESDAY -> "Wed"
    Calendar.THURSDAY -> "Thu"
    Calendar.FRIDAY -> "Fri"
    Calendar.SATURDAY -> "Sat"
    Calendar.SUNDAY -> "Sun"
    else -> "?"
}

@Composable
fun ModernTaskCard(
    task: DailyTask,
    onCheckedChange: (Boolean) -> Unit = {},
    onTaskUpdate: ((DailyTask) -> Unit)? = null,
    onTaskDelete: ((DailyTask) -> Unit)? = null,
    showCheckbox: Boolean = true,
    onLongPress: (() -> Unit)? = null
) {
    val isDone = task.isDone
    val cardBg = if (isDone) ShareduleLightGreen else ShareduleLightYellow
    val checkboxColor = if (isDone) ShareduleGreen else SharedulePink
    val titleColor = if (isDone) ShareduleGreen else SharedulePink
    val statusText = if (isDone) "Done" else "Pending"
    val statusColor = if (isDone) ShareduleGreen else SharedulePink
    val detailsColor = ShareduleGrey

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress?.invoke() },
                    onPress = {
                        val press = tryAwaitRelease()
                        if (!press) showActions = false
                    }
                )
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Checkbox
            if (showCheckbox) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = ShareduleGreen,
                        uncheckedColor = SharedulePink,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(28.dp)
                )
            }
            // Center: Title and details (weight 1, left aligned)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = titleColor,
                    maxLines = 1,
                )
                if (!task.description.isNullOrBlank()) {
                    Text(
                        text = task.description ?: "",
                        fontSize = 13.sp,
                        color = detailsColor,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1
                    )
                }
            }
            // Right: Status text or actions
            if (!isDone) {
                if (showActions) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_manage),
                                contentDescription = "Settings",
                                tint = SharedulePink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                contentDescription = "Delete",
                                tint = SharedulePink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            } else {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    onTaskDelete?.invoke(task)
                }, colors = ButtonDefaults.buttonColors(containerColor = SharedulePink)) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    // Settings dialog
    if (showSettingsDialog) {
        EditTaskDialog(
            initialTask = task,
            onDismiss = { showSettingsDialog = false },
            onSave = { updatedTask ->
                showSettingsDialog = false
                onTaskUpdate?.invoke(updatedTask)
            }
        )
    }
}

@Composable
fun EditTaskDialog(
    initialTask: DailyTask,
    onDismiss: () -> Unit,
    onSave: (DailyTask) -> Unit
) {
    var title by remember { mutableStateOf(initialTask.title) }
    var description by remember { mutableStateOf(initialTask.description ?: "") }
    var recurrenceType by remember { mutableStateOf(initialTask.recurrence?.type ?: "NONE") }
    var daysOfWeek by remember { mutableStateOf(initialTask.recurrence?.daysOfWeek ?: listOf<Int>()) }
    var showWeeklyDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SharedulePink,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SharedulePink,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                Spacer(Modifier.height(12.dp))
                Text("Recurrence:", fontSize = 16.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                RecurrenceTypeChipRow(
                    selectedType = recurrenceType,
                    onTypeSelected = {
                        recurrenceType = it
                        if (it == "WEEKLY") showWeeklyDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (recurrenceType == "WEEKLY") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (daysOfWeek.isEmpty()) "No days selected" else daysOfWeek.sorted().joinToString(", ") { dayIntToLabel(it) },
                        color = SharedulePink,
                        fontSize = 14.sp
                    )
                    Button(onClick = { showWeeklyDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = SharedulePink)) {
                        Text("Choose Days", color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val recurrence = when (recurrenceType) {
                        "NONE" -> null
                        "DAILY" -> Recurrence("DAILY")
                        "WEEKLY" -> Recurrence("WEEKLY", daysOfWeek = daysOfWeek)
                        else -> null // Not supported in chip UI
                    }
                    onSave(
                        initialTask.copy(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            recurrence = recurrence
                        )
                    )
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SharedulePink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = Modifier.width(360.dp)
    )
    if (showWeeklyDialog) {
        WeeklyDaysDialog(
            initialDays = daysOfWeek,
            onDismiss = { showWeeklyDialog = false },
            onConfirm = {
                daysOfWeek = it
                showWeeklyDialog = false
            }
        )
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

@Composable
fun DotNumberChip(value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(50))
            )
            Spacer(Modifier.width(8.dp))
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
} 