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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val uiState by viewModel.uiState.collectAsState()
    val profile by profileViewModel.profile.collectAsState()

    var showStreakInfo by remember {
        mutableStateOf(false)
    }

    val lifecycleOwner =
        LocalLifecycleOwner.current

    var firstResume by remember {
        mutableStateOf(true)
    }

    DisposableEffect(lifecycleOwner) {

        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event == Lifecycle.Event.ON_RESUME
                ) {

                    if (firstResume) {

                        firstResume = false

                        profileViewModel.loadProfile()

                        viewModel.loadProgressData(
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

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val background =
        MaterialTheme.colorScheme.background

    val surface =
        MaterialTheme.colorScheme.surface

    val onSurface =
        MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .background(background),
        verticalArrangement =
            Arrangement.spacedBy(14.dp),
        contentPadding =
            PaddingValues(
                horizontal = 16.dp,
                vertical = 10.dp
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
                            profile?.profile_picture
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
                text = "Your Progress",
                fontSize = 22.sp,
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
                    showStreakInfo = true
                }
            )
        }

        item {

            Text(
                text = "Study Consistency",
                fontSize = 18.sp,
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
                        .height(430.dp)
            )
        }
    }

    if (showStreakInfo) {

        AlertDialog(
            onDismissRequest = {
                showStreakInfo = false
            },
            shape =
                RoundedCornerShape(24.dp),
            title = {

                Text(
                    text = "Study Streak",
                    fontWeight =
                        FontWeight.Bold
                )
            },
            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        text =
                            "Your study streak increases when you study for at least 5 minutes in a day."
                    )

                    Text(
                        text =
                            "4 minutes → does not count\n" +
                                    "5 minutes → counts as 1 day\n" +
                                    "30 minutes → counts as 1 day"
                    )

                    Text(
                        text =
                            "Study at least 5 minutes each day to keep your streak going."
                    )
                }
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        showStreakInfo = false
                    }
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun OverallCard(
    progress: Float,
    surface: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color,
    onSurfaceVariant: androidx.compose.ui.graphics.Color
) {
    val percentage =
        (progress.coerceIn(0f, 1f) * 100).toInt()

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(145.dp),
        shape =
            RoundedCornerShape(26.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp
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
                    Modifier.size(116.dp),
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
                    Modifier.width(18.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "Overall Progress",
                    fontSize = 13.sp,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text =
                        "$percentage%",
                    fontSize = 32.sp,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color =
                        onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text =
                        progressMessage(
                            percentage
                        ),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
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
    surface: androidx.compose.ui.graphics.Color,
    onSurface: androidx.compose.ui.graphics.Color,
    onSurfaceVariant: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clickable(
                    onClick = onClick
                ),
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
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
                text = "🔥",
                fontSize = 34.sp
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Text(
                text =
                    "$streak",
                fontSize = 34.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    onSurface
            )

            Spacer(
                modifier =
                    Modifier.width(7.dp)
            )

            Text(
                text = "days",
                fontSize = 12.sp,
                color =
                    onSurfaceVariant
            )
        }
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