package com.example.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.model.PlannerItem
import com.example.project.navigation.Screen
import com.example.project.ui.components.FocusSessionCard
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressSummaryCard
import com.example.project.viewmodel.DashboardViewModel
import com.example.project.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class TaskListType(val title: String) { REMAINING("Remaining"), MISSED("Missed"), COMPLETED("Completed") }
private data class PlannerReminder(val item: PlannerItem, val message: String)

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    plannerViewModel: PlannerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val plannerItems by plannerViewModel.items.collectAsState()
    var selectedList by remember { mutableStateOf<TaskListType?>(null) }
    var selectedDetail by remember { mutableStateOf<PlannerItem?>(null) }
    var showReminders by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { plannerViewModel.loadItems() }

    val completedItems = plannerItems.filter { it.status.equals("Completed", ignoreCase = true) }
    val remainingItems = plannerItems.filterNot { it.status.equals("Completed", ignoreCase = true) }
    val now = Date().time
    val missedItems = remainingItems.filter { dueDateMillis(it.dueAt) <= now }.sortedBy { dueDateMillis(it.dueAt) }
    val upcomingItems = remainingItems.filter { dueDateMillis(it.dueAt) > now }.sortedBy { dueDateMillis(it.dueAt) }
    val selectedItems = when (selectedList) {
        TaskListType.REMAINING -> remainingItems
        TaskListType.MISSED -> missedItems
        TaskListType.COMPLETED -> completedItems
        null -> emptyList()
    }
    val nextItem = upcomingItems.firstOrNull()
    val reminders = plannerReminders(remainingItems)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item { HeaderCard(userName = uiState.userName, hasNotifications = reminders.isNotEmpty(), onNotificationClick = { showReminders = true }) }
        item {
            ProgressSummaryCard(
                completedTasks = completedItems.size,
                totalTasks = plannerItems.size,
                nextTaskName = nextItem?.title ?: "No tasks left",
                nextTaskTime = nextItem?.dueAt?.substringAfterLast(", ").orEmpty()
            )
        }
        item {
            DailyMetrics(
                remainingCount = remainingItems.size,
                missedCount = missedItems.size,
                completedCount = completedItems.size,
                onRemainingClick = { selectedList = TaskListType.REMAINING },
                onMissedClick = { selectedList = TaskListType.MISSED },
                onCompletedClick = { selectedList = TaskListType.COMPLETED }
            )
        }
        item { FocusSessionCard(currentTimeText = uiState.currentTimeText, onStartClick = { navController.navigate(Screen.FocusSession.route) }) }
    }

    selectedList?.let { type ->
        PlannerTitleDialog(title = type.title, items = selectedItems, onDismiss = { selectedList = null }, onItemClick = { selectedDetail = it })
    }
    selectedDetail?.let { item -> PlannerDetailDialog(item = item, onDismiss = { selectedDetail = null }) }
    if (showReminders) ReminderDialog(reminders = reminders, onDismiss = { showReminders = false })
}

@Composable
private fun DailyMetrics(
    remainingCount: Int,
    missedCount: Int,
    completedCount: Int,
    onRemainingClick: () -> Unit,
    onMissedClick: () -> Unit,
    onCompletedClick: () -> Unit
) {
    val textColor = Color.Black
    val remainingColor = Color(0xFFA7DFFA)
    val deadlineColor = Color(0xFFFFA8AC)
    val completedColor = Color(0xFFA6F7A1)
    Column {
        Text("Task Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Remaining", remainingCount, remainingColor, textColor, onRemainingClick, Modifier.weight(1f))
            MetricCard("Missed", missedCount, deadlineColor, textColor, onMissedClick, Modifier.weight(1f))
            MetricCard("Completed", completedCount, completedColor, textColor, onCompletedClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(label: String, count: Int, color: androidx.compose.ui.graphics.Color, contentColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit, modifier: Modifier) {
    Card(modifier = modifier.height(150.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.fillMaxSize().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Text(count.toString(), fontSize = 52.sp, fontWeight = FontWeight.Bold, color = contentColor)
            Text(label, fontSize = 13.sp, color = contentColor)
        }
    }
}

@Composable
private fun PlannerTitleDialog(title: String, items: List<PlannerItem>, onDismiss: () -> Unit, onItemClick: (PlannerItem) -> Unit) {
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dialogColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val rowColor = Color(0xFFA7DFFA)
    val dialogTextColor = if (isDarkMode) Color(0xFFE0E0E0) else Color.Black
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = dialogColor, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth().heightIn(min = 360.dp, max = 540.dp)) {
            Column(Modifier.padding(18.dp)) {
                Box(Modifier.fillMaxWidth()) {
                    Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = dialogTextColor, modifier = Modifier.align(Alignment.Center))
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Default.Close, contentDescription = "Close", tint = dialogTextColor) }
                }
                HorizontalDivider(color = dialogTextColor)
                Spacer(Modifier.height(16.dp))
                if (items.isEmpty()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("No items", color = dialogTextColor.copy(alpha = 0.65f)) }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f, fill = false)) {
                    items(items, key = { it.id }) { item ->
                        Surface(color = rowColor, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable { onItemClick(item) }) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(item.title, color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerDetailDialog(item: PlannerItem, onDismiss: () -> Unit) {
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dialogColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val detailColor = Color(0xFFA7DFFA)
    val textColor = if (isDarkMode) Color(0xFFE0E0E0) else Color.Black
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = dialogColor, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Box(Modifier.fillMaxWidth()) {
                    Text(if (item.itemType == "project") "Project details" else "Task details", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.align(Alignment.CenterStart))
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor) }
                }
                Spacer(Modifier.height(12.dp))
                Surface(color = detailColor, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailLine("Title", item.title, Color.Black)
                        DetailLine("Description", item.description.ifBlank { "—" }, Color.Black)
                        DetailLine("Due date", item.dueAt, Color.Black)
                        DetailLine("Status", item.status, Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String, textColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$label:", fontWeight = FontWeight.Bold, color = textColor)
        Text(value, color = textColor)
    }
}

@Composable
private fun ReminderDialog(reminders: List<PlannerReminder>, onDismiss: () -> Unit) {
    val isDarkMode = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dialogColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val dialogTextColor = if (isDarkMode) Color(0xFFE0E0E0) else Color.Black
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = dialogColor, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 540.dp)) {
            Column(Modifier.padding(18.dp)) {
                Box(Modifier.fillMaxWidth()) {
                    Text("Deadline reminders", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = dialogTextColor, modifier = Modifier.align(Alignment.CenterStart))
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Default.Close, "Close", tint = dialogTextColor) }
                }
                HorizontalDivider(color = dialogTextColor)
                Spacer(Modifier.height(14.dp))
                if (reminders.isEmpty()) Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("No upcoming reminders", color = dialogTextColor.copy(alpha = 0.65f)) }
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f, fill = false)) {
                    items(reminders, key = { it.item.id }) { reminder ->
                        Surface(color = Color(0xFFA7DFFA), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(reminder.item.title, color = Color.Black, fontWeight = FontWeight.Bold)
                                Text("Deadline: ${reminder.item.dueAt}", color = Color.Black)
                                Text(reminder.message, color = Color.Black.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun plannerReminders(items: List<PlannerItem>): List<PlannerReminder> {
    val now = Date().time
    val hourMillis = 60L * 60L * 1000L
    val dayMillis = 24L * hourMillis
    return items.mapNotNull { item ->
        val due = dueDateMillis(item.dueAt)
        val remaining = due - now
        when {
            item.itemType == "task" && remaining in 0..(2 * hourMillis) -> PlannerReminder(item, "Due within 2 hours")
            item.itemType == "project" && remaining in 0..(3 * dayMillis) -> PlannerReminder(item, "Due within 3 days")
            item.itemType == "project" && remaining in 0..(7 * dayMillis) -> PlannerReminder(item, "Due within 1 week")
            else -> null
        }
    }.sortedBy { dueDateMillis(it.item.dueAt) }
}

private fun dueDateMillis(value: String): Long = try { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).parse(value)?.time ?: Long.MAX_VALUE } catch (_: Exception) { Long.MAX_VALUE }
