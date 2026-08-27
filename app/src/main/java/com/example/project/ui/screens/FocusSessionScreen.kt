package com.example.project.ui.screens

import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.viewmodel.DashboardViewModel
import com.example.project.viewmodel.FocusState
import kotlin.math.abs

@Composable
fun FocusSessionScreen(
    navController: NavController,
    viewModel: DashboardViewModel,
    progressViewModel: com.example.project.viewmodel.ProgressViewModel =
        viewModel()
) {
    val uiState by
    viewModel.uiState.collectAsState()

    var showExitDialog by remember {
        mutableStateOf(false)
    }

    var selectedHour by remember {
        mutableIntStateOf(0)
    }

    var selectedMinute by remember {
        mutableIntStateOf(30)
    }

    var breakAfterMinute by remember {
        mutableIntStateOf(25)
    }

    var breakDurationMinute by remember {
        mutableIntStateOf(5)
    }

    var skipBreaks by remember {
        mutableStateOf(false)
    }

    var isAfterExpanded by remember {
        mutableStateOf(false)
    }

    var isDurationExpanded by remember {
        mutableStateOf(false)
    }

    val breakAfterOptions = remember {

        listOf(
            15 to "15 min",
            25 to "25 min",
            30 to "30 min",
            45 to "45 min",
            60 to "1 hr",
            90 to "1.5 hr",
            120 to "2 hr"
        )
    }

    val breakDurationOptions = remember {

        listOf(
            5 to "5 min",
            10 to "10 min",
            15 to "15 min",
            20 to "20 min",
            30 to "30 min"
        )
    }

    val selectedAfterText =
        remember(breakAfterMinute) {

            breakAfterOptions
                .find {
                    it.first ==
                            breakAfterMinute
                }
                ?.second
                ?: "$breakAfterMinute min"
        }

    val selectedDurationText =
        remember(breakDurationMinute) {

            breakDurationOptions
                .find {
                    it.first ==
                            breakDurationMinute
                }
                ?.second
                ?: "$breakDurationMinute min"
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
                .padding(24.dp)
    ) {

        IconButton(
            onClick = {

                if (
                    uiState.focusState ==
                    FocusState.IDLE
                ) {

                    navController.popBackStack()

                } else {

                    showExitDialog = true
                }
            },

            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.6f
                            ),
                        shape =
                            CircleShape
                    )
        ) {

            Icon(
                imageVector =
                    Icons.Default.Close,

                contentDescription =
                    "Close",

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                if (
                    uiState.focusState ==
                    FocusState.IDLE
                ) {

                    Text(
                        text =
                            "Focus Duration",

                        fontSize =
                            15.sp,

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
                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.Center,

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                    ) {

                        WheelPicker(
                            count = 24,

                            initialValue =
                                selectedHour,

                            onValueChange = {
                                selectedHour = it
                            },

                            unitText = "hr"
                        )

                        Text(
                            text = ":",

                            fontSize = 32.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onBackground,

                            modifier =
                                Modifier.padding(
                                    horizontal = 12.dp
                                )
                        )

                        WheelPicker(
                            count = 60,

                            initialValue =
                                selectedMinute,

                            onValueChange = {
                                selectedMinute = it
                            },

                            unitText = "min"
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )

                    AnimatedVisibility(
                        visible =
                            !skipBreaks,

                        enter =
                            fadeIn() +
                                    expandVertically(),

                        exit =
                            fadeOut() +
                                    shrinkVertically()
                    ) {

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.92f)
                                    .clip(
                                        RoundedCornerShape(
                                            16.dp
                                        )
                                    ),

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .surface,

                            tonalElevation =
                                2.dp
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(
                                        16.dp
                                    ),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                DropdownSelectorRow(
                                    label =
                                        "Break after",

                                    selectedValueText =
                                        selectedAfterText,

                                    isExpanded =
                                        isAfterExpanded,

                                    onToggle = {

                                        isAfterExpanded =
                                            !isAfterExpanded

                                        if (
                                            isAfterExpanded
                                        ) {

                                            isDurationExpanded =
                                                false
                                        }
                                    },

                                    options =
                                        breakAfterOptions,

                                    onOptionSelected = {
                                            minutes ->

                                        breakAfterMinute =
                                            minutes

                                        isAfterExpanded =
                                            false
                                    }
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            12.dp
                                        )
                                )

                                DropdownSelectorRow(
                                    label =
                                        "Break for",

                                    selectedValueText =
                                        selectedDurationText,

                                    isExpanded =
                                        isDurationExpanded,

                                    onToggle = {

                                        isDurationExpanded =
                                            !isDurationExpanded

                                        if (
                                            isDurationExpanded
                                        ) {

                                            isAfterExpanded =
                                                false
                                        }
                                    },

                                    options =
                                        breakDurationOptions,

                                    onOptionSelected = {
                                            minutes ->

                                        breakDurationMinute =
                                            minutes

                                        isDurationExpanded =
                                            false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,

                        modifier =
                            Modifier.padding(
                                vertical = 4.dp
                            )
                    ) {

                        Checkbox(
                            checked =
                                skipBreaks,

                            onCheckedChange = {
                                skipBreaks = it
                            },

                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor =
                                        MaterialTheme
                                            .colorScheme
                                            .primary,

                                    uncheckedColor =
                                        MaterialTheme
                                            .colorScheme
                                            .onBackground
                                            .copy(
                                                alpha = 0.6f
                                            )
                                )
                        )

                        Text(
                            text =
                                "Skip breaks",

                            fontSize =
                                14.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onBackground
                                    .copy(
                                        alpha = 0.7f
                                    )
                        )
                    }

                } else {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        modifier =
                            Modifier.padding(
                                vertical = 20.dp
                            )
                    ) {

                        val titleText =
                            when (
                                uiState.focusState
                            ) {

                                FocusState.FOCUSING ->
                                    "Focus Time"

                                FocusState.BREAK ->
                                    "Break Time"

                                FocusState.PAUSED ->
                                    "Paused"

                                else ->
                                    "In Progress"
                            }

                        val titleColor =
                            when (
                                uiState.focusState
                            ) {

                                FocusState.FOCUSING ->
                                    MaterialTheme
                                        .colorScheme
                                        .primary

                                FocusState.BREAK ->
                                    MaterialTheme
                                        .colorScheme
                                        .tertiary

                                FocusState.PAUSED ->
                                    MaterialTheme
                                        .colorScheme
                                        .secondary

                                else ->
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                            }

                        Text(
                            text =
                                titleText,

                            fontSize =
                                20.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                titleColor
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        val hrs =
                            uiState
                                .remainingSeconds /
                                    3600

                        val mins =
                            (
                                    uiState
                                        .remainingSeconds %
                                            3600
                                    ) / 60

                        val secs =
                            uiState
                                .remainingSeconds %
                                    60

                        Text(
                            text =
                                String.format(
                                    Locale.getDefault(),
                                    "%02d:%02d:%02d",
                                    hrs,
                                    mins,
                                    secs
                                ),

                            fontSize =
                                44.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onBackground
                        )

                        if (
                            uiState.focusState ==
                            FocusState.BREAK
                        ) {

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        8.dp
                                    )
                            )

                            Text(
                                text =
                                    "Your study timer is paused during break",

                                fontSize =
                                    13.sp,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onBackground
                                        .copy(
                                            alpha = 0.6f
                                        )
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                when (
                    uiState.focusState
                ) {

                    FocusState.IDLE -> {

                        Button(
                            onClick = {

                                viewModel.startFocusSession(
                                    selectedHour,
                                    selectedMinute,
                                    breakAfterMinute,
                                    breakDurationMinute,
                                    skipBreaks
                                )
                            },

                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .primary,

                                        contentColor =
                                            MaterialTheme
                                                .colorScheme
                                                .onPrimary
                                    ),

                            shape =
                                RoundedCornerShape(
                                    24.dp
                                ),

                            modifier =
                                Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp)
                        ) {

                            Text(
                                text = "Start",

                                fontSize =
                                    18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    FocusState.FOCUSING,
                    FocusState.BREAK -> {

                        Button(
                            onClick = {

                                viewModel
                                    .pauseFocusSession()
                            },

                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .error,

                                        contentColor =
                                            MaterialTheme
                                                .colorScheme
                                                .onError
                                    ),

                            shape =
                                RoundedCornerShape(
                                    24.dp
                                ),

                            modifier =
                                Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(48.dp)
                        ) {

                            Text(
                                text = "Stop",

                                fontSize =
                                    18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    FocusState.PAUSED -> {

                        Row(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    16.dp
                                ),

                            modifier =
                                Modifier
                                    .fillMaxWidth(
                                        0.85f
                                    )
                        ) {

                            Button(
                                onClick = {

                                    viewModel
                                        .resumeFocusSession()
                                },

                                colors =
                                    ButtonDefaults
                                        .buttonColors(
                                            containerColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .primary,

                                            contentColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onPrimary
                                        ),

                                shape =
                                    RoundedCornerShape(
                                        24.dp
                                    ),

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(48.dp)
                            ) {

                                Text(
                                    text =
                                        "Continue",

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {

                                    viewModel
                                        .endAndSaveFocusSession()
                                },

                                shape =
                                    RoundedCornerShape(
                                        24.dp
                                    ),

                                colors =
                                    ButtonDefaults
                                        .outlinedButtonColors(
                                            contentColor =
                                                MaterialTheme
                                                    .colorScheme
                                                    .error
                                        ),

                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .height(48.dp)
                            ) {

                                Text(
                                    text =
                                        "End",

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {

        AlertDialog(
            onDismissRequest = {
                showExitDialog = false
            },

            shape =
                RoundedCornerShape(24.dp),

            title = {

                Text(
                    text =
                        "Exit Focus Session?",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "Your current study session will be discarded and will not be saved."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        showExitDialog = false

                        viewModel.exitFocusSession()
                    }
                ) {

                    Text(
                        text = "Exit",
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
                        showExitDialog = false
                    }
                ) {

                    Text(
                        text = "Cancel"
                    )
                }
            }
        )
    }
}

@Composable
fun DropdownSelectorRow(
    label: String,
    selectedValueText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    options: List<Pair<Int, String>>,
    onOptionSelected: (Int) -> Unit
) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onToggle()
                    },

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text =
                    label,

                fontSize =
                    14.sp,

                fontWeight =
                    FontWeight.Medium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                            .copy(
                                alpha = 0.12f
                            ),

                    shape =
                        RoundedCornerShape(
                            8.dp
                        )
                ) {

                    Text(
                        text =
                            selectedValueText,

                        fontSize =
                            13.sp,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,

                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            )
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(4.dp)
                )

                Icon(
                    imageVector =
                        Icons.Default
                            .KeyboardArrowDown,

                    contentDescription =
                        null,

                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                            .copy(
                                alpha = 0.6f
                            )
                )
            }
        }

        AnimatedVisibility(
            visible =
                isExpanded,

            enter =
                fadeIn() +
                        expandVertically(),

            exit =
                fadeOut() +
                        shrinkVertically()
        ) {

            FlowRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 10.dp
                        ),

                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                options.forEach {
                        (minutes, text) ->

                    Surface(
                        modifier =
                            Modifier.clickable {

                                onOptionSelected(
                                    minutes
                                )
                            },

                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant,

                        shape =
                            RoundedCornerShape(
                                8.dp
                            )
                    ) {

                        Text(
                            text =
                                text,

                            fontSize =
                                12.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    count: Int,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    unitText: String
) {

    val itemHeightPx =
        with(
            LocalDensity.current
        ) {
            50.dp.toPx()
        }

    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                initialValue
        )

    val snapFlingBehavior =
        rememberSnapFlingBehavior(
            lazyListState =
                listState
        )

    val currentFocusedIndex by
    remember {

        derivedStateOf {

            val layoutInfo =
                listState.layoutInfo

            val visibleItems =
                layoutInfo
                    .visibleItemsInfo

            if (
                visibleItems.isEmpty()
            ) {

                0

            } else {

                val viewportCenter =
                    (
                            layoutInfo
                                .viewportStartOffset +
                                    layoutInfo
                                        .viewportEndOffset
                            ) / 2f

                visibleItems.minByOrNull {

                    abs(
                        (
                                it.offset +
                                        it.size / 2f
                                ) -
                                viewportCenter
                    )

                }?.index ?: 0
            }
        }
    }

    LaunchedEffect(
        currentFocusedIndex
    ) {

        onValueChange(
            currentFocusedIndex % count
        )
    }

    Row(
        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.Center
    ) {

        Box(
            modifier =
                Modifier
                    .width(65.dp)
                    .height(150.dp),

            contentAlignment =
                Alignment.Center
        ) {

            LazyColumn(
                state =
                    listState,

                flingBehavior =
                    snapFlingBehavior,

                contentPadding =
                    PaddingValues(
                        vertical = 50.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier =
                    Modifier.fillMaxSize()
            ) {

                items(count) { index ->

                    val isSelected =
                        index ==
                                currentFocusedIndex

                    val distanceFromCenter =
                        remember {

                            derivedStateOf {

                                val itemInfo =
                                    listState
                                        .layoutInfo
                                        .visibleItemsInfo
                                        .firstOrNull {

                                            it.index ==
                                                    index
                                        }

                                if (
                                    itemInfo != null
                                ) {

                                    val viewportCenter =
                                        (
                                                listState
                                                    .layoutInfo
                                                    .viewportStartOffset +
                                                        listState
                                                            .layoutInfo
                                                            .viewportEndOffset
                                                ) / 2f

                                    val itemCenter =
                                        itemInfo.offset +
                                                itemInfo.size /
                                                2f

                                    abs(
                                        itemCenter -
                                                viewportCenter
                                    ) /
                                            itemHeightPx

                                } else {

                                    1f
                                }
                            }
                        }

                    val fontSize =
                        (
                                22 +
                                        (34 - 22) *
                                        (
                                                1f -
                                                        distanceFromCenter
                                                            .value
                                                            .coerceIn(
                                                                0f,
                                                                1f
                                                            )
                                                )
                                ).sp

                    Box(
                        modifier =
                            Modifier
                                .height(50.dp)
                                .fillMaxWidth(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                String.format(
                                    Locale.getDefault(),
                                    "%02d",
                                    index
                                ),

                            fontSize =
                                fontSize,

                            fontWeight =
                                if (
                                    isSelected
                                ) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },

                            color =
                                if (
                                    isSelected
                                ) {
                                    MaterialTheme
                                        .colorScheme
                                        .onBackground
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .onBackground
                                        .copy(
                                            alpha = 0.3f
                                        )
                                }
                        )
                    }
                }
            }
        }

        Text(
            text =
                unitText,

            fontSize =
                13.sp,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
                    .copy(
                        alpha = 0.6f
                    ),

            modifier =
                Modifier.padding(
                    start = 2.dp
                )
        )
    }
}