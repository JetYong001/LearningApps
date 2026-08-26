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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StudyConsistencyCard(
    sessions: List<StudySession> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val dateFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "E, MMM d",
                Locale.ENGLISH
            )
        }

    val dayFormatter =
        remember {
            DateTimeFormatter.ofPattern(
                "EEEE",
                Locale.ENGLISH
            )
        }

    val dayOfWeekValue =
        selectedDate.dayOfWeek.value

    val sunOffset =
        if (dayOfWeekValue == 7) {
            0
        } else {
            dayOfWeekValue
        }

    val startOfWeek =
        selectedDate.minusDays(
            sunOffset.toLong()
        )

    val weekDays =
        remember(startOfWeek) {
            (0..6).map {
                startOfWeek.plusDays(
                    it.toLong()
                )
            }
        }

    val dayLabels =
        listOf(
            "Sun",
            "Mon",
            "Tue",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
        )

    val sessionsByDate =
        remember(sessions) {

            sessions.mapNotNull { session ->

                try {

                    val createdAt =
                        session.createdAt
                            ?: return@mapNotNull null

                    val date =
                        ZonedDateTime
                            .parse(createdAt)
                            .toLocalDate()

                    date to
                            session.durationSeconds

                } catch (_: Exception) {

                    null
                }

            }.groupBy(
                { it.first },
                { it.second }
            )
        }

    val dailySeconds =
        weekDays.map { date ->
            sessionsByDate[date]
                ?.sum()
                ?: 0
        }

    val selectedSeconds =
        sessionsByDate[selectedDate]
            ?.sum()
            ?: 0

    val hours =
        selectedSeconds / 3600

    val minutes =
        (selectedSeconds % 3600) / 60

    val seconds =
        selectedSeconds % 60

    val timeText =
        when {

            hours > 0 ->
                "$hours hr $minutes min"

            minutes > 0 ->
                "$minutes min"

            else ->
                "$seconds sec"
        }

    val maxSeconds =
        dailySeconds.maxOrNull()
            ?: 0

    val barHeights =
        dailySeconds.map { secondsValue ->

            if (
                maxSeconds > 0 &&
                secondsValue > 0
            ) {

                (
                        secondsValue.toFloat() /
                                maxSeconds.toFloat()
                        ).coerceIn(
                        0.05f,
                        1f
                    )

            } else {

                0f
            }
        }

    val today =
        LocalDate.now()

    val canGoForward =
        selectedDate.isBefore(today)

    Card(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text =
                        "Study Consistency",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    timeText,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Text(
                text =
                    if (
                        selectedDate == today
                    ) {
                        "Today"
                    } else {
                        selectedDate.format(
                            dayFormatter
                        )
                    },
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            ) {

                Column(
                    modifier =
                        Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    repeat(4) {

                        HorizontalDivider(
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .outlineVariant
                                    .copy(
                                        alpha = 0.3f
                                    ),
                            thickness = 1.dp
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 4.dp
                            ),
                    horizontalArrangement =
                        Arrangement.SpaceAround,
                    verticalAlignment =
                        Alignment.Bottom
                ) {

                    weekDays.forEachIndexed { index, date ->

                        val heightFraction =
                            barHeights[index]

                        val isHighlighted =
                            date == selectedDate

                        val isFuture =
                            date.isAfter(today)

                        val interactionSource =
                            remember(date) {
                                MutableInteractionSource()
                            }

                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .then(
                                        if (!isFuture) {
                                            Modifier.clickable(
                                                indication =
                                                    null,
                                                interactionSource =
                                                    interactionSource
                                            ) {
                                                selectedDate =
                                                    date
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.Bottom
                        ) {

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                contentAlignment =
                                    Alignment.BottomCenter
                            ) {

                                if (
                                    !isFuture &&
                                    heightFraction > 0f
                                ) {

                                    Box(
                                        modifier =
                                            Modifier
                                                .width(22.dp)
                                                .fillMaxHeight(
                                                    heightFraction
                                                )
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 6.dp,
                                                        topEnd = 6.dp
                                                    )
                                                )
                                                .background(
                                                    if (
                                                        isHighlighted
                                                    ) {
                                                        MaterialTheme
                                                            .colorScheme
                                                            .primary
                                                    } else {
                                                        MaterialTheme
                                                            .colorScheme
                                                            .surfaceVariant
                                                    }
                                                )
                                    )
                                }
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    dayLabels[index],
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelMedium,
                                color =
                                    if (isHighlighted) {
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                    } else {
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                    },
                                fontWeight =
                                    if (isHighlighted) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        selectedDate =
                            selectedDate.minusDays(1)
                    }
                ) {

                    Icon(
                        imageVector =
                            Icons.Default
                                .KeyboardArrowLeft,
                        contentDescription =
                            "Previous Day",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    text =
                        selectedDate.format(
                            dateFormatter
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                if (canGoForward) {

                    IconButton(
                        onClick = {
                            selectedDate =
                                selectedDate.plusDays(1)
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default
                                    .KeyboardArrowRight,
                            contentDescription =
                                "Next Day",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                } else {

                    Spacer(
                        modifier =
                            Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}