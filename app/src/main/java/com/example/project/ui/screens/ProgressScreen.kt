package com.example.project.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.navigation.Screen
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressRing
import com.example.project.viewmodel.ProfileViewModel
import com.example.project.viewmodel.ProgressViewModel
import java.time.DayOfWeek
import java.time.LocalDate

@SuppressLint("DefaultLocale")
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val profile by profileViewModel.profile.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val primary =
        MaterialTheme.colorScheme.primary

    val onPrimary =
        MaterialTheme.colorScheme.onPrimary

    val background =
        MaterialTheme.colorScheme.background

    val surface =
        MaterialTheme.colorScheme.surface

    val surfaceVariant =
        MaterialTheme.colorScheme.surfaceVariant

    val onSurface =
        MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant =
        MaterialTheme.colorScheme.onSurfaceVariant

    val days = listOf(
        "Sun",
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat"
    )

    val currentDayIndex =
        when (LocalDate.now().dayOfWeek) {

            DayOfWeek.SUNDAY -> 0

            else ->
                LocalDate
                    .now()
                    .dayOfWeek
                    .value
        }

    val maxHours =
        uiState.weeklyStudyHours
            .maxOrNull()
            ?.coerceAtLeast(4f)
            ?: 4f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {

                        navController.navigate(
                            Screen.Profile.route
                        )
                    }
            ) {

                HeaderCard(
                    userName =
                        profile?.username
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
                modifier = Modifier
                    .align(Alignment.TopEnd)
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
                    tint = onPrimary
                )
            }
        }

        Text(
            text = "Your Progress",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp),
                shape =
                    RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = surface
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
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
                            text = "Overall",
                            fontSize = 14.sp,
                            fontWeight =
                                FontWeight.SemiBold,
                            color = onSurface
                        )

                        Text(
                            text =
                                "${(uiState.overallProgress * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = primary
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    ProgressRing(
                        progress =
                            uiState.overallProgress,
                        size = 105.dp,
                        strokeWidth = 13.dp,
                        activeColor = primary,
                        trackColor =
                            surfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp),
                shape =
                    RoundedCornerShape(24.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = primary
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Study Streak",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.SemiBold,
                        color = onPrimary
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    Text(
                        text = "🔥",
                        fontSize = 34.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(2.dp)
                    )

                    Text(
                        text =
                            "${uiState.studyStreak}",
                        fontSize = 42.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        color = onPrimary
                    )

                    Text(
                        text = "days",
                        fontSize = 12.sp,
                        color =
                            onPrimary.copy(
                                alpha = 0.75f
                            )
                    )

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(285.dp),
            shape =
                RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = surface
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column {

                        Text(
                            text =
                                "Study Consistency",
                            fontSize = 16.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = onSurface
                        )

                        Spacer(
                            modifier =
                                Modifier.height(3.dp)
                        )

                        Text(
                            text =
                                "Weekly activity",
                            fontSize = 12.sp,
                            color =
                                onSurfaceVariant
                        )
                    }

                    Surface(
                        shape =
                            RoundedCornerShape(12.dp),
                        color =
                            primary.copy(
                                alpha = 0.12f
                            )
                    ) {

                        Text(
                            text =
                                uiState.todayStudyTime,
                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 7.dp
                                ),
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = primary
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    uiState.weeklyStudyHours
                        .forEachIndexed { index, hours ->

                            val heightFraction =
                                if (maxHours > 0f) {
                                    hours / maxHours
                                } else {
                                    0f
                                }

                            val isToday =
                                index ==
                                        currentDayIndex

                            val hasStudy =
                                hours > 0f

                            Column(
                                modifier =
                                    Modifier.fillMaxHeight(),
                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                                verticalArrangement =
                                    Arrangement.Bottom
                            ) {

                                if (hasStudy) {

                                    Text(
                                        text =
                                            String.format(
                                                "%.1fh",
                                                hours
                                            ),
                                        fontSize = 9.sp,
                                        fontWeight =
                                            FontWeight.SemiBold,
                                        color = primary
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(5.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .fillMaxHeight(
                                            if (hasStudy) {

                                                (
                                                        heightFraction *
                                                                0.72f
                                                        )
                                                    .coerceIn(
                                                        0.04f,
                                                        0.72f
                                                    )

                                            } else {

                                                0.04f
                                            }
                                        )
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 8.dp,
                                                topEnd = 8.dp
                                            )
                                        )
                                        .background(
                                            if (hasStudy) {
                                                primary
                                            } else {
                                                surfaceVariant
                                            }
                                        )
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(7.dp)
                                )

                                Text(
                                    text =
                                        days[index],
                                    fontSize = 10.sp,
                                    fontWeight =
                                        if (isToday) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                    color =
                                        if (isToday) {
                                            primary
                                        } else {
                                            onSurfaceVariant
                                        }
                                )
                            }
                        }
                }
            }
        }
    }
}