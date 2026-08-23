package com.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project.model.StudySession
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StudyConsistencyCard(
    sessions: List<StudySession> = emptyList()
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = DateTimeFormatter.ofPattern("E, MMM d", Locale.ENGLISH)

    // Calculate the 7 days of the week (Sunday to Saturday) containing the selectedDate
    val sunOffset = (selectedDate.dayOfWeek.value % 7)
    val startOfWeek = selectedDate.minusDays(sunOffset.toLong())
    val weekDays = remember(selectedDate) { (0..6).map { startOfWeek.plusDays(it.toLong()) } }
    val daysOfWeekLabel = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    // Map sessions to each day of the week, defaulting to 0 min if empty
    val dailyMinutes = remember(sessions, weekDays) {
        weekDays.map { date ->
            sessions.filter { session ->
                try {
                    LocalDate.parse(session.sessionDate.take(10)) == date
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.durationMinutes }
        }
    }

    // Calculate bar height fractions relative to the week's peak day
    val maxMinutes = dailyMinutes.maxOrNull() ?: 0
    val barHeights = dailyMinutes.map { mins ->
        if (maxMinutes > 0) (mins.toFloat() / maxMinutes.toFloat()).coerceIn(0.12f, 1f)
        else 0.05f
    }

    val selectedIndex = weekDays.indexOf(selectedDate)

    // Total study time for the selected day
    val selectedMinutes = if (selectedIndex in dailyMinutes.indices) dailyMinutes[selectedIndex] else 0
    val hours = selectedMinutes / 60
    val minutes = selectedMinutes % 60
    val timeText = if (hours > 0) "$hours hr $minutes min" else "$minutes min"

    // Check if we can go forward (i.e. selectedDate is before today)
    val canGoForward = selectedDate.isBefore(LocalDate.now())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Study Consistency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Total Duration & Selected Day Text
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (selectedDate == LocalDate.now()) "Today" else selectedDate.format(DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Background reference lines
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(4) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )
                    }
                }

                // Bar columns layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekDays.forEachIndexed { index, date ->
                        val heightFraction = barHeights[index]
                        val isHighlighted = index == selectedIndex
                        val dayLabel = daysOfWeekLabel[index]
                        val isFuture = date.isAfter(LocalDate.now())

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .fillMaxHeight()
                                .then(
                                    if (!isFuture) {
                                        Modifier.clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            selectedDate = date
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(horizontal = 4.dp)
                        ) {
                            if (!isFuture) {
                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isHighlighted) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.width(18.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isHighlighted) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Previous Day",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (canGoForward) {
                    IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}