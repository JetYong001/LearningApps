package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.project.navigation.Screen
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressRing
import com.example.project.ui.components.StudyConsistencyCard
import com.example.project.viewmodel.ProfileViewModel
import com.example.project.viewmodel.ProgressViewModel

@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel,
    profileViewModel: ProfileViewModel
) {
    val uiState by
    viewModel.uiState.collectAsState()

    val profile by
    profileViewModel.profile.collectAsState()

    var showStreakInfo by
    remember {
        mutableStateOf(false)
    }

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var firstResume by
    remember {
        mutableStateOf(true)
    }

    DisposableEffect(
        lifecycleOwner
    ) {

        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event ==
                    Lifecycle.Event.ON_RESUME
                ) {

                    if (
                        firstResume
                    ) {

                        firstResume =
                            false

                        profileViewModel
                            .loadProfile()

                        viewModel
                            .loadProgressData(
                                forceRefresh = true
                            )

                    } else {

                        profileViewModel
                            .refreshProfileInBackground()

                        viewModel
                            .refreshProgressInBackground()
                    }
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

    val background =
        MaterialTheme
            .colorScheme
            .background

    val surface =
        MaterialTheme
            .colorScheme
            .surface

    val onSurface =
        MaterialTheme
            .colorScheme
            .onSurface

    val onSurfaceVariant =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant

    val onPrimary =
        MaterialTheme
            .colorScheme
            .onPrimary

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    background
                )
    ) {

        val fixedHeight =
            514.dp

        val consistencyHeight =
            (
                    maxHeight -
                            fixedHeight
                    ).coerceAtLeast(
                    180.dp
                )

        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            verticalArrangement =
                Arrangement.spacedBy(
                    14.dp
                ),

            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 24.dp
                )
        ) {

            item {

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {

                                    navController.navigate(
                                        Screen.Profile.route
                                    )
                                }
                    ) {

                        HeaderCard(
                            userName =
                                profile
                                    ?.username
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: uiState.userName,

                            profilePicture =
                                profile
                                    ?.profile_picture
                        )
                    }

                    IconButton(
                        onClick = {

                            navController.navigate(
                                Screen.Settings.route
                            )
                        },

                        modifier =
                            Modifier
                                .align(
                                    Alignment.TopEnd
                                )
                                .padding(
                                    top = 6.dp,
                                    end = 6.dp
                                )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Settings,

                            contentDescription =
                                "Settings",

                            tint =
                                onPrimary
                        )
                    }
                }
            }

            item {

                Text(
                    text =
                        "Your Progress",

                    fontSize =
                        22.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        onSurface
                )
            }

            item {

                OverallCard(
                    progress =
                        uiState.overallProgress,

                    surface =
                        surface,

                    onSurface =
                        onSurface,

                    onSurfaceVariant =
                        onSurfaceVariant
                )
            }

            item {

                StudyStreakCard(
                    streak =
                        uiState.studyStreak,

                    surface =
                        surface,

                    onSurface =
                        onSurface,

                    onSurfaceVariant =
                        onSurfaceVariant,

                    onClick = {

                        showStreakInfo =
                            true
                    }
                )
            }

            item {

                Text(
                    text =
                        "Study Consistency",

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        onSurface
                )
            }

            item {

                StudyConsistencyCard(
                    sessions =
                        uiState.studySessions,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                consistencyHeight
                            )
                )
            }
        }
    }

    if (
        showStreakInfo
    ) {

        StudyStreakDialog(
            onDismiss = {

                showStreakInfo =
                    false
            }
        )
    }
}

@Composable
private fun OverallCard(
    progress: Float,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color
) {
    val percentage =
        (
                progress
                    .coerceIn(
                        0f,
                        1f
                    ) * 100
                ).toInt()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    145.dp
                ),

        shape =
            RoundedCornerShape(
                26.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 18.dp,
                        vertical = 12.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Box(
                modifier =
                    Modifier.size(
                        116.dp
                    ),

                contentAlignment =
                    Alignment.Center
            ) {

                ProgressRing(
                    progress =
                        progress,

                    size =
                        102.dp,

                    strokeWidth =
                        11.dp
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        18.dp
                    )
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    )
            ) {

                Text(
                    text =
                        "Overall Progress",

                    fontSize =
                        13.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        "$percentage%",

                    fontSize =
                        32.sp,

                    fontWeight =
                        FontWeight.ExtraBold,

                    color =
                        onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        progressMessage(
                            percentage
                        ),

                    fontSize =
                        11.sp,

                    lineHeight =
                        15.sp,

                    color =
                        onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StudyStreakCard(
    streak: Int,
    surface: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    80.dp
                )
                .clickable(
                    onClick =
                        onClick
                ),

        shape =
            RoundedCornerShape(
                22.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    surface
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    2.dp
            )
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 18.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    "🔥",

                fontSize =
                    24.sp
            )

            Spacer(
                modifier =
                    Modifier.width(
                        12.dp
                    )
            )

            Text(
                text =
                    "$streak",

                fontSize =
                    28.sp,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    onSurface
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    "days",

                fontSize =
                    18.sp,

                color =
                    onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StudyStreakDialog(
    onDismiss: () -> Unit
) {
    val surface =
        MaterialTheme
            .colorScheme
            .surface

    val onSurface =
        MaterialTheme
            .colorScheme
            .onSurface

    val onSurfaceVariant =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant

    val primary =
        MaterialTheme
            .colorScheme
            .primary

    val onPrimary =
        MaterialTheme
            .colorScheme
            .onPrimary

    Dialog(
        onDismissRequest =
            onDismiss
    ) {

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(
                    28.dp
                ),

            color =
                surface,

            tonalElevation =
                8.dp
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            20.dp
                        )
            ) {

                Text(
                    text =
                        "Study Streak",

                    fontSize =
                        24.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        "Your study streak is based on your daily study time.",

                    fontSize =
                        14.sp,

                    lineHeight =
                        20.sp,

                    color =
                        onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(
                            18.dp
                        ),

                    color =
                        primary
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        Text(
                            text =
                                "Conditions",

                            fontSize =
                                16.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                onPrimary
                        )

                        StreakConditionRow(
                            time =
                                "Less than 5 minutes",

                            result =
                                "Does not count",

                            textColor =
                                onPrimary
                        )

                        StreakConditionRow(
                            time =
                                "5 minutes or more",

                            result =
                                "Counts as 1 day",

                            textColor =
                                onPrimary
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                Text(
                    text =
                        "Study for at least 5 minutes in a day to maintain your streak.",

                    fontSize =
                        13.sp,

                    lineHeight =
                        18.sp,

                    color =
                        onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Button(
                    onClick =
                        onDismiss,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                50.dp
                            ),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                primary,

                            contentColor =
                                onPrimary
                        )
                ) {

                    Text(
                        text =
                            "Got it",

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StreakConditionRow(
    time: String,
    result: String,
    textColor: Color
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
                time,

            fontSize =
                13.sp,

            color =
                textColor
        )

        Text(
            text =
                result,

            fontSize =
                13.sp,

            fontWeight =
                FontWeight.SemiBold,

            color =
                textColor
        )
    }
}

private fun progressMessage(
    percentage: Int
): String {
    return when {

        percentage >= 100 ->
            "Excellent! All tasks are completed."

        percentage >= 75 ->
            "Great progress. You're almost there."

        percentage >= 50 ->
            "Nice work. Keep the momentum going."

        percentage >= 25 ->
            "Good start. Keep building your progress."

        percentage > 0 ->
            "Every completed task moves you forward."

        else ->
            "Complete your first task to start your progress."
    }
}