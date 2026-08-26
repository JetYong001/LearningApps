package com.example.project.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.model.PlannerItem
import com.example.project.viewmodel.PlannerViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: PlannerViewModel
) {
    val context = LocalContext.current
    val plannerItems by viewModel.items.collectAsState()

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    var editingItem by remember {
        mutableStateOf<PlannerItem?>(null)
    }

    var deletingItem by remember {
        mutableStateOf<PlannerItem?>(null)
    }

    var showEditor by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    var prioritySortingEnabled by remember {
        mutableStateOf(false)
    }

    var title by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    var dueDate by remember {
        mutableStateOf("")
    }

    var dueTime by remember {
        mutableStateOf("23:59")
    }

    var status by remember {
        mutableStateOf("In progress")
    }

    var showStatusMenu by remember {
        mutableStateOf(false)
    }

    fun openEditor(item: PlannerItem?) {
        if (item != null && isMissed(item)) {
            return
        }

        editingItem = item

        title =
            item?.title.orEmpty()

        description =
            item?.description.orEmpty()

        dueDate =
            item?.dueAt
                ?.substringBeforeLast(", ")
                .orEmpty()

        dueTime =
            item?.dueAt
                ?.substringAfterLast(
                    ", ",
                    "23:59"
                )
                ?: "23:59"

        status =
            item?.status
                ?: "In progress"

        showEditor = true
    }

    fun savePlannerItem() {

        if (title.isBlank()) {
            Toast.makeText(
                context,
                "Please enter a title",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (dueDate.isBlank()) {
            Toast.makeText(
                context,
                "Please select a due date",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (!isValidTime(dueTime)) {
            Toast.makeText(
                context,
                "Please enter valid time (HH:mm)",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val itemType =
            if (selectedTab == 0) {
                "task"
            } else {
                "project"
            }

        viewModel.saveItem(
            PlannerItem(
                id =
                    editingItem?.id.orEmpty(),
                itemType =
                    itemType,
                title =
                    title.trim(),
                description =
                    description.trim(),
                dueAt =
                    "${dueDate.trim()}, ${dueTime.trim()}",
                status =
                    status
            )
        )

        showEditor = false
    }

    val type =
        if (selectedTab == 0) {
            "task"
        } else {
            "project"
        }

    val filteredItems =
        plannerItems.filter {
            it.itemType == type
        }

    val visibleItems =
        if (prioritySortingEnabled) {
            filteredItems.sortedBy {
                dueDateTime(it.dueAt)
            }
        } else {
            filteredItems
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
    ) {

        Column(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                        .clip(
                            RoundedCornerShape(50.dp)
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                        )
                        .padding(
                            vertical = 13.dp
                        ),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "Planner",
                    fontSize = 24.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                )
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),
                shape =
                    RoundedCornerShape(18.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                ) {

                    PlannerTab(
                        text = "Tasks",
                        selected =
                            selectedTab == 0,
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            selectedTab = 0
                        }
                    )

                    PlannerTab(
                        text = "Projects",
                        selected =
                            selectedTab == 1,
                        modifier =
                            Modifier.weight(1f),
                        onClick = {
                            selectedTab = 1
                        }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp
                        ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column {

                    Text(
                        text =
                            if (selectedTab == 0) {
                                "Your Tasks"
                            } else {
                                "Your Projects"
                            },
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onBackground
                    )

                    Text(
                        text =
                            "${filteredItems.size} items",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Surface(
                    modifier =
                        Modifier.clickable {
                            prioritySortingEnabled =
                                !prioritySortingEnabled
                        },
                    shape =
                        RoundedCornerShape(14.dp),
                    color =
                        if (prioritySortingEnabled) {
                            MaterialTheme
                                .colorScheme
                                .primaryContainer
                        } else {
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                        }
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.Sort,
                            contentDescription =
                                "Sort by due date",
                            modifier =
                                Modifier.size(19.dp),
                            tint =
                                if (prioritySortingEnabled) {
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text(
                            text =
                                if (prioritySortingEnabled) {
                                    "Priority"
                                } else {
                                    "Sort"
                                },
                            fontSize = 13.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color =
                                if (prioritySortingEnabled) {
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                }
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            if (visibleItems.isEmpty()) {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = 80.dp
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Surface(
                            shape =
                                RoundedCornerShape(
                                    22.dp
                                ),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                        ) {

                            Icon(
                                imageVector =
                                    if (type == "task") {
                                        Icons.Default.Edit
                                    } else {
                                        Icons.Default.CalendarToday
                                    },
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier
                                        .padding(20.dp)
                                        .size(42.dp),
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.height(14.dp)
                        )

                        Text(
                            text =
                                if (type == "task") {
                                    "No tasks yet"
                                } else {
                                    "No projects yet"
                                },
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onBackground
                        )

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                "Create one using the + button",
                            fontSize = 13.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp),
                    contentPadding =
                        PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 100.dp
                        )
                ) {

                    items(
                        visibleItems,
                        key = {
                            it.id
                        }
                    ) { item ->

                        PlannerCard(
                            item = item,
                            onEdit = {
                                openEditor(item)
                            },
                            onDelete = {
                                deletingItem = item
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                openEditor(null)
            },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp)
                    .size(64.dp),
            shape =
                RoundedCornerShape(20.dp),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .primaryContainer,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer
        ) {

            Icon(
                imageVector =
                    Icons.Default.Add,
                contentDescription =
                    "Add",
                modifier =
                    Modifier.size(30.dp)
            )
        }
    }

    if (showEditor) {

        AlertDialog(
            onDismissRequest = {
                showEditor = false
            },
            shape =
                RoundedCornerShape(28.dp),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,
            title = {

                Text(
                    text =
                        if (editingItem == null) {
                            "New ${
                                if (selectedTab == 0) {
                                    "Task"
                                } else {
                                    "Project"
                                }
                            }"
                        } else {
                            "Edit ${
                                if (selectedTab == 0) {
                                    "Task"
                                } else {
                                    "Project"
                                }
                            }"
                        },
                    fontSize = 22.sp,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )
            },
            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(11.dp)
                ) {

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        label = {
                            Text("Title")
                        },
                        singleLine = true,
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(15.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        label = {
                            Text("Description")
                        },
                        minLines = 3,
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(15.dp)
                    )

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {
                            dueDate = it
                        },
                        label = {
                            Text("Due date")
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(15.dp),
                        trailingIcon = {

                            IconButton(
                                onClick = {
                                    showDatePicker = true
                                }
                            ) {

                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription =
                                        "Select date"
                                )
                            }
                        }
                    )

                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = {
                            dueTime = it
                        },
                        label = {
                            Text("Time")
                        },
                        placeholder = {
                            Text("23:59")
                        },
                        singleLine = true,
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(15.dp),
                        isError =
                            dueTime.isNotBlank() &&
                                    !isValidTime(
                                        dueTime
                                    ),
                        trailingIcon = {

                            IconButton(
                                onClick = {
                                    showTimePicker = true
                                }
                            ) {

                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription =
                                        "Select time"
                                )
                            }
                        }
                    )

                    Box {

                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text("Status")
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(15.dp),
                            trailingIcon = {

                                TextButton(
                                    onClick = {
                                        showStatusMenu = true
                                    }
                                ) {

                                    Text(
                                        "Change",
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .primary
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded =
                                showStatusMenu,
                            onDismissRequest = {
                                showStatusMenu = false
                            }
                        ) {

                            listOf(
                                "In progress",
                                "Completed",
                                "Not started"
                            ).forEach { option ->

                                DropdownMenuItem(
                                    text = {
                                        Text(option)
                                    },
                                    onClick = {

                                        status = option

                                        showStatusMenu =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {

                Button(
                    onClick = {
                        savePlannerItem()
                    },
                    shape =
                        RoundedCornerShape(13.dp)
                ) {

                    Text(
                        "Save",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showEditor = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {

        val todayUtcMillis =
            remember {

                val local =
                    Calendar.getInstance()

                val year =
                    local.get(
                        Calendar.YEAR
                    )

                val month =
                    local.get(
                        Calendar.MONTH
                    )

                val day =
                    local.get(
                        Calendar.DAY_OF_MONTH
                    )

                Calendar
                    .getInstance(
                        TimeZone.getTimeZone(
                            "UTC"
                        )
                    )
                    .apply {
                        clear()
                        set(
                            year,
                            month,
                            day
                        )
                    }
                    .timeInMillis
            }

        val pickerState =
            rememberDatePickerState(
                selectableDates =
                    object :
                        SelectableDates {

                        override fun isSelectableDate(
                            utcTimeMillis: Long
                        ): Boolean {

                            return utcTimeMillis >=
                                    todayUtcMillis
                        }
                    }
            )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        pickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                val formatter =
                                    SimpleDateFormat(
                                        "dd MMMM yyyy",
                                        Locale.ENGLISH
                                    ).apply {
                                        timeZone =
                                            TimeZone.getTimeZone(
                                                "UTC"
                                            )
                                    }

                                dueDate =
                                    formatter.format(
                                        Date(millis)
                                    )
                            }

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = pickerState
            )
        }
    }

    if (showTimePicker) {

        val parts =
            dueTime.split(":")

        val pickerState =
            rememberTimePickerState(
                initialHour =
                    parts
                        .getOrNull(0)
                        ?.toIntOrNull()
                        ?: 23,
                initialMinute =
                    parts
                        .getOrNull(1)
                        ?.toIntOrNull()
                        ?: 59,
                is24Hour = true
            )

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            shape =
                RoundedCornerShape(26.dp),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,
            title = {

                Text(
                    "Select Time",
                    fontWeight =
                        FontWeight.Bold
                )
            },
            text = {

                TimePicker(
                    state = pickerState
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        dueTime =
                            String.format(
                                Locale.US,
                                "%02d:%02d",
                                pickerState.hour,
                                pickerState.minute
                            )

                        showTimePicker = false
                    }
                ) {

                    Text("OK")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    deletingItem?.let { item ->

        AlertDialog(
            onDismissRequest = {
                deletingItem = null
            },
            shape =
                RoundedCornerShape(26.dp),
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface,
            title = {

                Text(
                    text =
                        "Delete ${
                            if (item.itemType == "task") {
                                "Task"
                            } else {
                                "Project"
                            }
                        }?",
                    fontWeight =
                        FontWeight.Bold
                )
            },
            text = {

                Text(
                    "Are you sure you want to delete \"${item.title}\"?"
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.deleteItem(
                            item
                        )

                        deletingItem = null
                    }
                ) {

                    Text(
                        "Delete",
                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        deletingItem = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlannerTab(
    text: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier =
            modifier.clickable {
                onClick()
            },
        shape =
            RoundedCornerShape(14.dp),
        color =
            if (selected) {
                MaterialTheme
                    .colorScheme
                    .primary
            } else {
                Color.Transparent
            }
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 10.dp
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight =
                    if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                color =
                    if (selected) {
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}

@Composable
private fun PlannerCard(
    item: PlannerItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember {
        mutableStateOf(false)
    }

    val missed =
        isMissed(item)

    val displayStatus =
        if (missed) {
            "Missed"
        } else {
            item.status
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Top
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = item.title,
                        fontSize = 19.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )

                    if (
                        item.description.isNotBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(5.dp)
                        )

                        Text(
                            text =
                                item.description,
                            fontSize = 14.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

                Box {

                    IconButton(
                        onClick = {
                            showMenu = true
                        }
                    ) {

                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription =
                                "Actions",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded =
                            showMenu,
                        onDismissRequest = {
                            showMenu = false
                        }
                    ) {

                        if (!missed) {

                            DropdownMenuItem(
                                leadingIcon = {

                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription =
                                            null
                                    )
                                },
                                text = {
                                    Text("Edit")
                                },
                                onClick = {

                                    showMenu = false
                                    onEdit()
                                }
                            )
                        }

                        DropdownMenuItem(
                            leadingIcon = {

                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription =
                                        null,
                                    tint =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            },
                            text = {

                                Text(
                                    "Delete",
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            },
                            onClick = {

                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
            )

            Spacer(
                modifier =
                    Modifier.height(13.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                InfoChip(
                    icon =
                        Icons.Default.CalendarToday,
                    text =
                        item.dueAt.substringBefore(
                            ","
                        )
                )

                InfoChip(
                    icon =
                        Icons.Default.Schedule,
                    text =
                        item.dueAt.substringAfterLast(
                            ", ",
                            ""
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            StatusChip(
                status =
                    displayStatus
            )
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape =
            RoundedCornerShape(10.dp),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 6.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier =
                    Modifier.size(15.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.width(5.dp)
            )

            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.Medium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(
    status: String
) {
    val containerColor =
        when (status) {
            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer

            "In progress" ->
                MaterialTheme
                    .colorScheme
                    .primary

            "Not started" ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant

            "Missed" ->
                MaterialTheme
                    .colorScheme
                    .errorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        }

    val contentColor =
        when (status) {
            "Completed" ->
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer

            "In progress" ->
                MaterialTheme
                    .colorScheme
                    .onPrimary

            "Not started" ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant

            "Missed" ->
                MaterialTheme
                    .colorScheme
                    .onErrorContainer

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    Surface(
        shape =
            RoundedCornerShape(10.dp),
        color =
            containerColor
    ) {

        Row(
            modifier =
                Modifier.padding(
                    horizontal = 11.dp,
                    vertical = 6.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(
                            contentColor
                        )
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight =
                    FontWeight.Bold,
                color =
                    contentColor
            )
        }
    }
}

private fun isMissed(
    item: PlannerItem
): Boolean {
    if (
        item.status.equals(
            "Completed",
            ignoreCase = true
        )
    ) {
        return false
    }

    return dueDateTime(item.dueAt) <
            System.currentTimeMillis()
}

private fun dueDateTime(
    value: String
): Long {
    return try {
        SimpleDateFormat(
            "dd MMMM yyyy, HH:mm",
            Locale.ENGLISH
        )
            .parse(value)
            ?.time
            ?: Long.MAX_VALUE
    } catch (_: Exception) {
        Long.MAX_VALUE
    }
}

private fun isValidTime(
    value: String
): Boolean {
    return Regex(
        "^([01]\\d|2[0-3]):[0-5]\\d$"
    ).matches(value)
}