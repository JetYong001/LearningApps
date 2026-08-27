package com.example.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    val uiState by
    viewModel.uiState.collectAsState()

    val currentTimeText by
    viewModel.currentTimeText.collectAsState()

    val plannerItems by
    plannerViewModel.items.collectAsState()

    val profile by
    profileViewModel.profile.collectAsState()

    var selectedList by
    remember {
        mutableStateOf<TaskListType?>(null)
    }

    var selectedDetail by
    remember {
        mutableStateOf<PlannerItem?>(null)
    }

    var showReminders by
    remember {
        mutableStateOf(false)
    }

    var notificationsRead by
    rememberSaveable {
        mutableStateOf(false)
    }

    val currentUserId =
        supabase
            .auth
            .currentUserOrNull()
            ?.id

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        currentUserId
    ) {

        val observer =
            LifecycleEventObserver {
                    _, event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    plannerViewModel
                        .refreshItemsInBackground()

                    profileViewModel
                        .refreshProfileInBackground()
                }
            }

        lifecycleOwner
            .lifecycle
            .addObserver(
                observer
            )

        onDispose {

            lifecycleOwner
                .lifecycle
                .removeObserver(
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

    val missedItems =
        remember(plannerItems) {

            plannerItems
                .filter {

                    it.status.equals(
                        "Missed",
                        ignoreCase = true
                    )
                }
                .sortedBy {

                    dueDateMillis(
                        it.dueAt
                    )
                }
        }

    val remainingItems =
        remember(plannerItems) {

            plannerItems
                .filter {

                    !it.status.equals(
                        "Completed",
                        ignoreCase = true
                    ) &&
                            !it.status.equals(
                                "Missed",
                                ignoreCase = true
                            )
                }
                .sortedBy {

                    dueDateMillis(
                        it.dueAt
                    )
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
        remainingItems.firstOrNull()

    val reminders =
        remember(plannerItems) {

            plannerReminders(
                plannerItems
            )
        }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                20.dp
            ),

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
                    modifier =
                        Modifier
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

                            showReminders =
                                true

                            notificationsRead =
                                true
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default
                                    .Notifications,

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
                    nextItem
                        ?.dueAt
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
            title =
                type.title,

            items =
                selectedItems,

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
            item =
                item,

            onDismiss = {
                selectedDetail = null
            }
        )
    }

    if (showReminders) {

        ReminderDialog(
            reminders =
                reminders,

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
    Column {

        Text(
            text =
                "Task Overview",

            fontSize =
                14.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
                    .copy(
                        alpha = 0.7f
                    ),

            modifier =
                Modifier.padding(
                    bottom = 8.dp
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            MetricCard(
                label =
                    "Remaining",

                count =
                    remainingCount,

                color =
                    Color(0xFFA7DFFA),

                onClick =
                    onRemainingClick,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            MetricCard(
                label =
                    "Missed",

                count =
                    missedCount,

                color =
                    Color(0xFFFFA8AC),

                onClick =
                    onMissedClick,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            MetricCard(
                label =
                    "Completed",

                count =
                    completedCount,

                color =
                    Color(0xFFA6F7A1),

                onClick =
                    onCompletedClick,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    count: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier =
            modifier
                .height(150.dp)
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(
                20.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    color
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
                text =
                    count.toString(),

                fontSize =
                    52.sp,

                fontWeight =
                    FontWeight.Bold,

                color =
                    Color.Black
            )

            Text(
                text =
                    label,

                fontSize =
                    13.sp,

                color =
                    Color.Black
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
    val isDark =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDark) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val textColor =
        if (isDark) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            shape =
                RoundedCornerShape(
                    24.dp
                ),

            color =
                dialogColor,

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
                    Modifier.padding(
                        18.dp
                    )
            ) {

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            title,

                        fontSize =
                            28.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            textColor,

                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )

                    IconButton(
                        onClick =
                            onDismiss,

                        modifier =
                            Modifier.align(
                                Alignment.CenterEnd
                            )
                    ) {

                        Icon(
                            Icons.Default.Close,
                            contentDescription =
                                "Close",

                            tint =
                                textColor
                        )
                    }
                }

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                if (
                    items.isEmpty()
                ) {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            "No items",
                            color =
                                textColor.copy(
                                    alpha =
                                        0.65f
                                )
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
                            key = {
                                it.id
                            }
                        ) { item ->

                            Surface(
                                color =
                                    Color(0xFFA7DFFA),

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
                                            horizontal = 16.dp,
                                            vertical = 17.dp
                                        ),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text =
                                            item.title,

                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),

                                        color =
                                            Color.Black
                                    )

                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,

                                        contentDescription =
                                            null,

                                        tint =
                                            Color.Black
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
    val isDark =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDark) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val textColor =
        if (isDark) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            shape =
                RoundedCornerShape(
                    24.dp
                ),

            color =
                dialogColor,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        20.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
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

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            textColor
                    )

                    IconButton(
                        onClick =
                            onDismiss
                    ) {

                        Icon(
                            Icons.Default.Close,
                            contentDescription =
                                "Close",

                            tint =
                                textColor
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )

                Surface(
                    color =
                        Color(0xFFA7DFFA),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                18.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {

                        DetailLine(
                            "Title",
                            item.title
                        )

                        DetailLine(
                            "Description",
                            item.description.ifBlank {
                                "—"
                            }
                        )

                        DetailLine(
                            "Due date",
                            item.dueAt
                        )

                        DetailLine(
                            "Status",
                            item.status
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
    value: String
) {
    Column {

        Text(
            text =
                "$label:",

            fontWeight =
                FontWeight.Bold,

            color =
                Color.Black
        )

        Text(
            text =
                value,

            color =
                Color.Black
        )
    }
}

@Composable
private fun ReminderDialog(
    reminders: List<PlannerReminder>,
    onDismiss: () -> Unit
) {
    val isDark =
        MaterialTheme
            .colorScheme
            .background
            .luminance() < 0.5f

    val dialogColor =
        if (isDark) {
            Color(0xFF1E1E1E)
        } else {
            Color.White
        }

    val textColor =
        if (isDark) {
            Color(0xFFE0E0E0)
        } else {
            Color.Black
        }

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            shape =
                RoundedCornerShape(
                    24.dp
                ),

            color =
                dialogColor,

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
                    Modifier.padding(
                        18.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text =
                            "Deadline reminders",

                        fontSize =
                            24.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            textColor
                    )

                    IconButton(
                        onClick =
                            onDismiss
                    ) {

                        Icon(
                            Icons.Default.Close,
                            contentDescription =
                                "Close",

                            tint =
                                textColor
                        )
                    }
                }

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                if (
                    reminders.isEmpty()
                ) {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                ),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "No upcoming reminders",

                            color =
                                textColor.copy(
                                    alpha =
                                        0.65f
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
                            reminders,
                            key = {
                                it.item.id
                            }
                        ) { reminder ->

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),

                                shape =
                                    RoundedCornerShape(
                                        12.dp
                                    )
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
                                            textColor
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
                                            textColor.copy(
                                                alpha =
                                                    0.8f
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

private fun plannerReminders(
    items: List<PlannerItem>
): List<PlannerReminder> {

    val formatter =
        DateTimeFormatter.ofPattern(
            "dd MMMM yyyy, HH:mm",
            Locale.ENGLISH
        )

    val now =
        System.currentTimeMillis()

    val oneDay =
        24 * 60 * 60 * 1000L

    return items
        .filter { item ->

            !item.status.equals(
                "Completed",
                ignoreCase = true
            ) &&
                    !item.status.equals(
                        "Missed",
                        ignoreCase = true
                    )
        }
        .mapNotNull { item ->

            try {

                val dueMillis =
                    LocalDateTime
                        .parse(
                            item.dueAt,
                            formatter
                        )
                        .atZone(
                            ZoneId.systemDefault()
                        )
                        .toInstant()
                        .toEpochMilli()

                val diff =
                    dueMillis - now

                if (
                    diff in 1..oneDay
                ) {

                    val totalMinutes =
                        (
                                diff + 59_999L
                                ) / 60_000L

                    val hours =
                        totalMinutes / 60

                    val minutes =
                        totalMinutes % 60

                    val message =
                        when {

                            hours > 0 ->
                                "Due in $hours hour(s)"

                            minutes > 0 ->
                                "Due in $minutes minute(s)"

                            else ->
                                "Due in less than 1 minute"
                        }

                    PlannerReminder(
                        item =
                            item,

                        message =
                            message
                    )

                } else {
                    null
                }

            } catch (
                _: Exception
            ) {

                null
            }
        }
        .sortedBy { reminder ->

            try {

                LocalDateTime
                    .parse(
                        reminder.item.dueAt,
                        formatter
                    )
                    .atZone(
                        ZoneId.systemDefault()
                    )
                    .toInstant()
                    .toEpochMilli()

            } catch (
                _: Exception
            ) {

                Long.MAX_VALUE
            }
        }
}

private fun dueDateMillis(
    dueAt: String
): Long {

    return try {

        val formatter =
            DateTimeFormatter.ofPattern(
                "dd MMMM yyyy, HH:mm",
                Locale.ENGLISH
            )

        LocalDateTime
            .parse(
                dueAt,
                formatter
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toInstant()
            .toEpochMilli()

    } catch (
        _: Exception
    ) {

        Long.MAX_VALUE
    }
}