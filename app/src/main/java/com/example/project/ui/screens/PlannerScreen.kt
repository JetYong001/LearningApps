package com.example.project.ui.screens

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
import com.example.project.model.PlannerItem
import com.example.project.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var title by remember { mutableStateOf("") };
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") };
    var dueTime by remember { mutableStateOf("23:59") }
    var status by remember { mutableStateOf("In progress") };
    var showStatusMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadItems(context) }

    fun openEditor(item: PlannerItem?) {
        editingItem = item; title = item?.title.orEmpty();
        description = item?.description.orEmpty()
        dueDate = item?.dueAt?.substringBeforeLast(", ").orEmpty();
        dueTime = item?.dueAt?.substringAfterLast(", ", "23:59") ?: "23:59"
        status = item?.status ?: "In progress"; showEditor = true
    }
    fun saveItem() {
        if (title.isBlank() || dueDate.isBlank() || !isValidTime(dueTime))
            return
        editingItem?.createdAt?.let {
            viewModel.saveItem(
                PlannerItem(
                    editingItem?.id.orEmpty(),
                    if (selectedTab == 0) "task" else "project", title.trim(), description.trim(),
                    "${dueDate.trim()}, ${dueTime.trim()}", status, it
                ),
                context
            )
        }
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
                listOf("Task", "Project").forEachIndexed {
                    index, label -> Tab(selectedTab == index,
                    { selectedTab = index },
                    text = { Text(label, fontSize = 24.sp) })
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
                    if (prioritySortingEnabled) "Priority sorting is on" else "Sort by priority",
                    Modifier.size(38.dp)
                );
                Spacer(Modifier.width(18.dp));
                Text(
                    "Priority",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = if (prioritySortingEnabled) FontWeight.Bold else FontWeight.Normal
                )
            }
            val type = if (selectedTab == 0) "task" else "project"
            val addOrderItems = plannerItems.filter { it.itemType == type }.sortedBy { it.createdAt ?: "" }
            val visibleItems = if (prioritySortingEnabled) addOrderItems.sortedBy { dueDateTime(it.dueAt) } else addOrderItems
            if (visibleItems.isEmpty())
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
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 76.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(visibleItems, key = { it.id }) {
                    item -> PlannerCard(item, onEdit = { openEditor(item) }, onDelete = { deletingItem = item })
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
            Icon(Icons.Default.Add,
                "Add ${if (selectedTab == 0) "task" else "project"}",
                Modifier.size(42.dp)
            )
        }
    }
    if (showEditor) AlertDialog(onDismissRequest = { showEditor = false },
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
            title,
            { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            description,
            { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            dueDate,
            { dueDate = it },
            label = { Text("Due date (e.g. 23 July 2026)") },
            trailingIcon = { IconButton(
                { showDatePicker = true }) { Icon(
                Icons.Default.CalendarToday,
                "Choose date from calendar") } },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            dueTime,
            { dueTime = it },
            label = { Text("Time (24-hour format, e.g. 23:59)") },
            singleLine = true,
            trailingIcon = { IconButton(
                { showTimePicker = true }) { Icon(
                Icons.Default.Schedule,
                "Choose time") } },
            modifier = Modifier.fillMaxWidth(),
            isError = dueTime.isNotBlank() && !isValidTime(dueTime)
        )
        Box {
            OutlinedTextField(
                status,
                { },
                readOnly = true,
                label = { Text("Status") },
                trailingIcon = { TextButton(
                    { showStatusMenu = true }) { Text("Change") } },
                modifier = Modifier.fillMaxWidth());
            DropdownMenu(
                showStatusMenu,
                { showStatusMenu = false }
            ) {
                listOf("In progress", "Completed", "Not started").forEach {
                    option -> DropdownMenuItem(
                    { Text(option) },
                    { status = option; showStatusMenu = false }
                    )
                }
            }
        }
    } },
        confirmButton = {
            Button(
                { saveItem() }
            ) {
                Text("Save")
            }
                        },
        dismissButton = {
            TextButton(
                { showEditor = false }
            ) {
                Text("Cancel")
            }
        }
    )
    if (showDatePicker) {
        val pickerState = rememberDatePickerState();
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                { pickerState.selectedDateMillis?.let {
                    dueDate = SimpleDateFormat("dd MMMM yyyy",
                        Locale.getDefault()).format(Date(it))
                };
                    showDatePicker = false
                }
            ) {
                Text("OK")
            }
                            },
            dismissButton = {
                TextButton(
                    { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(pickerState)
        }
    }
    if (showTimePicker) {
        val parts = dueTime.split(":");
        val pickerState = rememberTimePickerState(initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 23,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 59, is24Hour = true);
        AlertDialog(onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(pickerState) },
            confirmButton = {
                TextButton(
                    { dueTime = String.format(Locale.US, "%02d:%02d",
                        pickerState.hour,
                        pickerState.minute
                    );
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
                            },
            dismissButton = {
                TextButton(
                    { showTimePicker = false }
                ) {
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
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingItem = null }) { Text("Cancel") } }
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
            .background(PlannerLightBlue,
                RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Column(
            Modifier.padding(end = 28.dp)
        ) {
            PlannerDetail("Title:", item.title);
            PlannerDetail("Description:", item.description);
            PlannerDetail("Due Date:", item.dueAt);
            PlannerDetail("Status:", item.status) };
        Box(Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, "Actions for ${item.title}", tint = Color.Black.copy(alpha = 0.65f))
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
        );
        Text(
            value,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold)
    }
}
private fun dueDateTime(value: String): Long =
    try {
        SimpleDateFormat(
            "dd MMMM yyyy, HH:mm",
            Locale.getDefault()
        ).parse(value)?.time ?: Long.MAX_VALUE
    } catch (_: Exception) { Long.MAX_VALUE }
private fun isValidTime(value: String): Boolean =
    Regex("^([01]\\d|2[0-3]):[0-5]\\d$").matches(value)
