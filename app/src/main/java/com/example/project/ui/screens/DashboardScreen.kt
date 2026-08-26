package com.example.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import com.example.project.navigation.Screen
import com.example.project.ui.components.FocusSessionCard
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressSummaryCard
import com.example.project.viewmodel.DashboardViewModel
import com.example.project.viewmodel.PlannerViewModel
import com.example.project.viewmodel.ProfileViewModel
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class TaskListType(
    val title: String
) {
    REMAINING("Remaining"),
    MISSED("Missed"),
    COMPLETED("Completed")
}

private data class PlannerReminder(
    val item: PlannerItem,
    val message: String
)

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    plannerViewModel: PlannerViewModel,
    profileViewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTimeText by viewModel.currentTimeText.collectAsState()
    val plannerItems by plannerViewModel.items.collectAsState()
    val profile by profileViewModel.profile.collectAsState()

    var selectedList by remember {
        mutableStateOf<TaskListType?>(null)
    }

    var selectedDetail by remember {
        mutableStateOf<PlannerItem?>(null)
    }

    var showReminders by remember {
        mutableStateOf(false)
    }

    var notificationsRead by rememberSaveable {
        mutableStateOf(false)
    }

    var firstResume by remember {
        mutableStateOf(true)
    }

    val currentUserId =
        supabase.auth.currentUserOrNull()?.id

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        currentUserId
    ) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (
                    event == Lifecycle.Event.ON_RESUME
                ) {
                    if (firstResume) {
                        firstResume = false
                    } else {
                        plannerViewModel
                            .refreshItemsInBackground()

                        profileViewModel
                            .refreshProfileInBackground()
                    }
                }
            }

        lifecycleOwner.lifecycle.addObserver(
            observer
        )

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(
                observer
            )
        }
    }

    val completedItems =
        remember(plannerItems) {
            plannerItems.filter {
                it.status.equals(
                    "Completed",
                    ignoreCase = true
                )
            }
        }

    val remainingItems =
        remember(plannerItems) {
            plannerItems.filterNot {
                it.status.equals(
                    "Completed",
                    ignoreCase = true
                )
            }
        }

    val missedItems =
        remember(remainingItems) {
            remainingItems
                .filter {
                    dueDateMillis(it.dueAt) <=
                            Date().time
                }
                .sortedBy {
                    dueDateMillis(it.dueAt)
                }
        }

    val upcomingItems =
        remember(remainingItems) {
            remainingItems
                .filter {
                    dueDateMillis(it.dueAt) >
                            Date().time
                }
                .sortedBy {
                    dueDateMillis(it.dueAt)
                }
        }

    val selectedItems =
        when (selectedList) {
            TaskListType.REMAINING ->
                remainingItems

            TaskListType.MISSED ->
                missedItems

            TaskListType.COMPLETED ->
                completedItems

            null ->
                emptyList()
        }

    val nextItem =
        upcomingItems.firstOrNull()

    val reminders =
        remember(remainingItems) {
            plannerReminders(
                remainingItems
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(20.dp),
        contentPadding =
            PaddingValues(
                top = 16.dp,
                bottom = 24.dp
            )
    ) {
        item {
            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                HeaderCard(
                    userName =
                        profile?.username
                            ?: uiState.userName,
                    profilePicture =
                        profile?.profile_picture
                )

                Box(
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            top = 8.dp,
                            end = 8.dp
                        )
                ) {
                    IconButton(
                        onClick = {
                            showReminders = true
                            notificationsRead = true
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Notifications,
                            contentDescription =
                                "Notifications",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimary
                        )
                    }

                    if (
                        reminders.isNotEmpty() &&
                        !notificationsRead
                    ) {
                        Badge(
                            modifier =
                                Modifier
                                    .align(
                                        Alignment.TopEnd
                                    )
                                    .offset(
                                        x = (-8).dp,
                                        y = 8.dp
                                    )
                        )
                    }
                }
            }
        }

        item {
            ProgressSummaryCard(
                completedTasks =
                    completedItems.size,
                totalTasks =
                    plannerItems.size,
                nextTaskName =
                    nextItem?.title
                        ?: "No tasks left",
                nextTaskTime =
                    nextItem?.dueAt
                        ?.substringAfterLast(
                            ", "
                        )
                        .orEmpty()
            )
        }

        item {
            DailyMetrics(
                remainingCount =
                    remainingItems.size,
                missedCount =
                    missedItems.size,
                completedCount =
                    completedItems.size,
                onRemainingClick = {
                    selectedList =
                        TaskListType.REMAINING
                },
                onMissedClick = {
                    selectedList =
                        TaskListType.MISSED
                },
                onCompletedClick = {
                    selectedList =
                        TaskListType.COMPLETED
                }
            )
        }

        item {
            FocusSessionCard(
                currentTimeText =
                    currentTimeText,
                onStartClick = {
                    navController.navigate(
                        Screen.FocusSession.route
                    )
                }
            )
        }
    }

    selectedList?.let { type ->
        PlannerTitleDialog(
            title = type.title,
            items = selectedItems,
            onDismiss = {
                selectedList = null
            },
            onItemClick = {
                selectedDetail = it
            }
        )
    }

    selectedDetail?.let { item ->
        PlannerDetailDialog(
            item = item,
            onDismiss = {
                selectedDetail = null
            }
        )
    }

    if (showReminders) {
        ReminderDialog(
            reminders = reminders,
            onDismiss = {
                showReminders = false
            }
        )
    }
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
    val remainingColor =
        Color(0xFFA7DFFA)
    val deadlineColor =
        Color(0xFFFFA8AC)
    val completedColor =
        Color(0xFFA6F7A1)

    Column {
        Text(
            text = "Task Overview",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme
                    .onBackground
                    .copy(alpha = 0.7f),
            modifier =
                Modifier.padding(
                    bottom = 8.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                label = "Remaining",
                count = remainingCount,
                color = remainingColor,
                contentColor = textColor,
                onClick = onRemainingClick,
                modifier =
                    Modifier.weight(1f)
            )

            MetricCard(
                label = "Missed",
                count = missedCount,
                color = deadlineColor,
                contentColor = textColor,
                onClick = onMissedClick,
                modifier =
                    Modifier.weight(1f)
            )

            MetricCard(
                label = "Completed",
                count = completedCount,
                color = completedColor,
                contentColor = textColor,
                onClick = onCompletedClick,
                modifier =
                    Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    count: Int,
    color: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier =
            modifier
                .height(150.dp)
                .clickable(
                    onClick = onClick
                ),
        shape =
            RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = color
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        vertical = 16.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text = count.toString(),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )

            Text(
                text = label,
                fontSize = 13.sp,
                color = contentColor
            )
        }
    }
}

@Composable
private fun PlannerTitleDialog(
    title: String,
    items: List<PlannerItem>,
    onDismiss: () -> Unit,
    onItemClick: (PlannerItem) -> Unit
) {
    val isDarkMode =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDarkMode) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val rowColor =
        Color(0xFFA7DFFA)

    val dialogTextColor =
        if (isDarkMode) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape =
                RoundedCornerShape(24.dp),
            color = dialogColor,
            tonalElevation = 8.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 360.dp,
                        max = 540.dp
                    )
        ) {
            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = dialogTextColor,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier.align(
                                Alignment.CenterEnd
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription =
                                "Close",
                            tint = dialogTextColor
                        )
                    }
                }

                HorizontalDivider(
                    color = dialogTextColor
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                if (items.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text = "No items",
                            color =
                                dialogTextColor
                                    .copy(alpha = 0.65f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(
                                14.dp
                            ),
                        modifier =
                            Modifier.weight(
                                1f,
                                fill = false
                            )
                    ) {
                        items(
                            items,
                            key = { it.id }
                        ) { item ->
                            Surface(
                                color = rowColor,
                                shape =
                                    RoundedCornerShape(
                                        14.dp
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onItemClick(
                                                item
                                            )
                                        }
                            ) {
                                Row(
                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                16.dp,
                                            vertical =
                                                17.dp
                                        ),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        text =
                                            item.title,
                                        color =
                                            Color.Black,
                                        fontSize =
                                            16.sp,
                                        modifier =
                                            Modifier.weight(
                                                1f
                                            )
                                    )

                                    Icon(
                                        imageVector =
                                            Icons.Default
                                                .ArrowForwardIos,
                                        contentDescription =
                                            null,
                                        tint =
                                            Color.Black,
                                        modifier =
                                            Modifier.size(
                                                18.dp
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerDetailDialog(
    item: PlannerItem,
    onDismiss: () -> Unit
) {
    val isDarkMode =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDarkMode) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val detailColor =
        Color(0xFFA7DFFA)

    val textColor =
        if (isDarkMode) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape =
                RoundedCornerShape(24.dp),
            color = dialogColor,
            tonalElevation = 8.dp,
            modifier =
                Modifier.fillMaxWidth()
        ) {
            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        text =
                            if (
                                item.itemType ==
                                "project"
                            ) {
                                "Project details"
                            } else {
                                "Task details"
                            },
                        fontSize = 24.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color = textColor,
                        modifier =
                            Modifier.align(
                                Alignment.CenterStart
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier.align(
                                Alignment.CenterEnd
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription =
                                "Close",
                            tint = textColor
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                Surface(
                    color = detailColor,
                    shape =
                        RoundedCornerShape(18.dp),
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier =
                            Modifier.padding(18.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        DetailLine(
                            label = "Title",
                            value = item.title,
                            textColor = Color.Black
                        )

                        DetailLine(
                            label = "Description",
                            value =
                                item.description
                                    .ifBlank {
                                        "—"
                                    },
                            textColor = Color.Black
                        )

                        DetailLine(
                            label = "Due date",
                            value = item.dueAt,
                            textColor = Color.Black
                        )

                        DetailLine(
                            label = "Status",
                            value = item.status,
                            textColor = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
    textColor: Color
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight =
                FontWeight.Bold,
            color = textColor
        )

        Text(
            text = value,
            color = textColor
        )
    }
}

@Composable
private fun ReminderDialog(
    reminders: List<PlannerReminder>,
    onDismiss: () -> Unit
) {
    val isDarkMode =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDarkMode) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val dialogTextColor =
        if (isDarkMode) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape =
                RoundedCornerShape(24.dp),
            color = dialogColor,
            tonalElevation = 8.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 300.dp,
                        max = 540.dp
                    )
        ) {
            Column(
                modifier =
                    Modifier.padding(18.dp)
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {
                    Text(
                        text =
                            "Deadline reminders",
                        fontSize = 24.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            dialogTextColor,
                        modifier =
                            Modifier.align(
                                Alignment.CenterStart
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier.align(
                                Alignment.CenterEnd
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.Close,
                            contentDescription =
                                "Close",
                            tint =
                                dialogTextColor
                        )
                    }
                }

                HorizontalDivider(
                    color =
                        dialogTextColor
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                if (reminders.isEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            text =
                                "No upcoming reminders",
                            color =
                                dialogTextColor
                                    .copy(
                                        alpha = 0.65f
                                    )
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        items(
                            reminders
                        ) { reminder ->
                            Card(
                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    ),
                                colors =
                                    CardDefaults
                                        .cardColors(
                                            containerColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .surfaceVariant
                                        ),
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier =
                                        Modifier.padding(
                                            12.dp
                                        )
                                ) {
                                    Text(
                                        text =
                                            reminder
                                                .item
                                                .title,
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize =
                                            15.sp,
                                        color =
                                            dialogTextColor
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(
                                                4.dp
                                            )
                                    )

                                    Text(
                                        text =
                                            reminder.message,
                                        fontSize =
                                            13.sp,
                                        color =
                                            dialogTextColor
                                                .copy(
                                                    alpha = 0.8f
                                                )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dueDateMillis(
    dueAt: String
): Long {
    return try {
        val sdf =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            )

        sdf.parse(dueAt)?.time
            ?: Long.MAX_VALUE

    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}

private fun plannerReminders(
    items: List<PlannerItem>
): List<PlannerReminder> {
    val now =
        Date().time

    return items.mapNotNull { item ->
        val due =
            dueDateMillis(item.dueAt)

        val diff =
            due - now

        if (
            diff in 0..(24 * 3600 * 1000)
        ) {
            val hours =
                diff / (3600 * 1000)

            PlannerReminder(
                item,
                "Due in $hours hour(s)"
            )
        } else if (diff < 0) {
            PlannerReminder(
                item,
                "Overdue"
            )
        } else {
            null
        }
    }
}