package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressRing
import com.example.project.viewmodel.ProgressViewModel
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val cardColor = MaterialTheme.colorScheme.secondaryContainer
    val onCardColor = MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            HeaderCard(
                userName = uiState.userName,
                subtitle = "Student"
            )

            IconButton(
                onClick = {
                    navController.navigate("settings")
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Overall Progress >",
                        fontSize = 12.sp,
                        color = onCardColor.copy(alpha = 0.8f)
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    ProgressRing(
                        progress = uiState.overallProgress,
                        size = 90.dp,
                        strokeWidth = 12.dp,
                        activeColor = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.1f
                        )
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Study Streaks",
                        fontSize = 12.sp,
                        color = onCardColor.copy(alpha = 0.8f)
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "🔥 ${uiState.studyStreak}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Study Consistency",
                    fontSize = 14.sp,
                    color = onCardColor.copy(alpha = 0.8f)
                )

                Text(
                    text = uiState.todayStudyTime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = onCardColor
                )

                Text(
                    text = "Today",
                    fontSize = 12.sp,
                    color = onCardColor.copy(alpha = 0.6f)
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {

                    val days = listOf(
                        "Sun",
                        "Mon",
                        "Tue",
                        "Wed",
                        "Thu",
                        "Fri",
                        "Sat"
                    )

                    val maxHours =
                        uiState.weeklyStudyHours
                            .maxOrNull()
                            ?.coerceAtLeast(4f)
                            ?: 4f

                    val currentDayIndex =
                        LocalDate.now().let {
                            if (it.dayOfWeek == DayOfWeek.SUNDAY) {
                                0
                            } else {
                                it.dayOfWeek.value
                            }
                        }

                    uiState.weeklyStudyHours.forEachIndexed { index, hours ->

                        val heightFraction =
                            if (maxHours > 0) {
                                hours / maxHours
                            } else {
                                0f
                            }

                        val isToday =
                            index == currentDayIndex

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight(
                                        heightFraction.coerceIn(
                                            0f,
                                            1f
                                        )
                                    )
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 4.dp,
                                            topEnd = 4.dp
                                        )
                                    )
                                    .background(
                                        if (isToday) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.2f
                                            )
                                        }
                                    )
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = days[index],
                                fontSize = 10.sp,
                                color = onCardColor.copy(
                                    alpha = 0.7f
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}