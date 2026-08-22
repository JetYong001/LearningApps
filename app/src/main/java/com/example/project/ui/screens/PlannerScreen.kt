package com.example.project.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import com.example.project.viewmodel.PlannerViewModel
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val PlannerLightBlue = Color(0xFF7BD5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(viewModel: PlannerViewModel = viewModel()) {
    val context = LocalContext.current
    val plannerItems by viewModel.items.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingItem by remember { mutableStateOf<PlannerItem?>(null) }
    var deletingItem by remember { mutableStateOf<PlannerItem?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var prioritySortingEnabled by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("23:59") }
    var status by remember { mutableStateOf("In progress") }
    var showStatusMenu by remember { mutableStateOf(false) }

    val currentUserId = supabase.auth.currentUserOrNull()?.id

    LaunchedEffect(currentUserId) {
        viewModel.loadItems(context)
    }

    fun openEditor(item: PlannerItem?) {
        editingItem = item
        title = item?.title.orEmpty()
        description = item?.description.orEmpty()
        dueDate = item?.dueAt?.substringBeforeLast(", ").orEmpty()
        dueTime = item?.dueAt?.substringAfterLast(", ", "23:59") ?: "23:59"
        status = item?.status ?: "In progress"
        showEditor = true
    }

    fun saveItem() {
        if (title.isBlank()) {
            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        if (dueDate.isBlank()) {
            Toast.makeText(context, "Please select a due date", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidTime(dueTime)) {
            Toast.makeText(context, "Please enter valid time (HH:mm)", Toast.LENGTH_SHORT).show()
            return
        }

        val fullDueAt = if (dueTime.isBlank()) dueDate.trim() else "${dueDate.trim()}, ${dueTime.trim()}"

        viewModel.saveItem(
            PlannerItem(
                id = editingItem?.id.orEmpty(),
                itemType = if (selectedTab == 0) "task" else "project",
                title = title.trim(),
                description = description.trim(),
                dueAt = fullDueAt,
                status = status
            ),
            context
        )
        showEditor = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(PlannerLightBlue, RoundedCornerShape(50))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Planner",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(10.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                listOf("Task", "Project").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label, fontSize = 24.sp) }
                    )
                }
            }

            Row(
                Modifier
                    .padding(start = 18.dp, top = 20.dp, bottom = 14.dp)
                    .clickable { prioritySortingEnabled = !prioritySortingEnabled },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = if (prioritySortingEnabled) "Priority sorting is on" else "Sort by priority",
                    modifier = Modifier.size(38.dp)
                )
                Spacer(Modifier.width(18.dp))
                Text(
                    "Priority",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (prioritySortingEnabled) FontWeight.Bold else FontWeight.Normal
                )
            }

            val type = if (selectedTab == 0) "task" else "project"
            val addOrderItems = plannerItems.filter { it.itemType == type }
            val visibleItems = if (prioritySortingEnabled) addOrderItems.sortedBy { dueDateTime(it.dueAt) } else addOrderItems

            if (visibleItems.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No ${if (type == "task") "tasks" else "projects"} yet",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 76.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(visibleItems, key = { it.id }) { item ->
                        PlannerCard(
                            item = item,
                            onEdit = { openEditor(item) },
                            onDelete = { deletingItem = item }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { openEditor(null) },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
                .size(78.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add ${if (selectedTab == 0) "task" else "project"}",
                modifier = Modifier.size(42.dp)
            )
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = {
                Text(
                    if (editingItem == null) "Add ${if (selectedTab == 0) "task" else "project"}" else "Edit ${if (selectedTab == 0) "task" else "project"}"
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due date (e.g. 23 July 2026)") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Choose date from calendar"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Time (24-hour format, e.g. 23:59)") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Choose time"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = dueTime.isNotBlank() && !isValidTime(dueTime)
                    )
                    Box {
                        OutlinedTextField(
                            value = status,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = {
                                TextButton(onClick = { showStatusMenu = true }) {
                                    Text("Change")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            listOf("In progress", "Completed", "Not started").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        status = option
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { saveItem() }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        val todayUtcMillis = remember {
            val localCalendar = java.util.Calendar.getInstance()
            val year = localCalendar.get(java.util.Calendar.YEAR)
            val month = localCalendar.get(java.util.Calendar.MONTH)
            val day = localCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            val utcCalendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(year, month, day)
            }
            utcCalendar.timeInMillis
        }

        val pickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= todayUtcMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            dueDate = formatter.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val parts = dueTime.split(":")
        val pickerState = rememberTimePickerState(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 23,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 59,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(pickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueTime = String.format(Locale.US, "%02d:%02d", pickerState.hour, pickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Delete ${if (item.itemType == "task") "task" else "project"}?") },
            text = { Text("Delete \"${item.title}\" permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteItem(item, context)
                    deletingItem = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlannerCard(
    item: PlannerItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .background(PlannerLightBlue, RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(
            Modifier.padding(end = 28.dp)
        ) {
            PlannerDetail("Title:", item.title)
            PlannerDetail("Description:", item.description)
            PlannerDetail("Due Date:", item.dueAt)
            PlannerDetail("Status:", item.status)
        }
        Box(Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Actions for ${item.title}", tint = Color.Black.copy(alpha = 0.65f))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
                DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() })
            }
        }
    }
}

@Composable
private fun PlannerDetail(
    label: String,
    value: String
) {
    Row {
        Text(
            label,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(94.dp)
        )
        Text(
            value,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun dueDateTime(value: String): Long =
    try {
        SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.ENGLISH).parse(value)?.time ?: Long.MAX_VALUE
    } catch (_: Exception) {
        Long.MAX_VALUE
    }

private fun isValidTime(value: String): Boolean =
    Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(value)