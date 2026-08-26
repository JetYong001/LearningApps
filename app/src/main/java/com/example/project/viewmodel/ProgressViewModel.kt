package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import com.example.project.model.StudySession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime

data class ProgressUiState(
    val userName: String = "",
    val overallProgress: Float = 0f,
    val studyStreak: Int = 0,
    val weeklyStudyHours: List<Float> = List(7) { 0f },
    val todayStudyTime: String = "0 sec",
    val studySessions: List<StudySession> = emptyList()
)

class ProgressViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(ProgressUiState())

    val uiState: StateFlow<ProgressUiState> =
        _uiState.asStateFlow()

    private var loadedUserId: String? = null
    private var isLoading = false

    fun loadProgressData(
        forceRefresh: Boolean = false
    ) {
        val userId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        if (isLoading) {
            return
        }

        if (
            !forceRefresh &&
            loadedUserId == userId
        ) {
            return
        }

        viewModelScope.launch {
            loadProgressDataAwait(
                forceRefresh = forceRefresh
            )
        }
    }

    suspend fun loadProgressDataAwait(
        forceRefresh: Boolean = false
    ) {
        val userId =
            supabase.auth.currentUserOrNull()?.id
                ?: return

        if (isLoading) {
            return
        }

        if (
            !forceRefresh &&
            loadedUserId == userId
        ) {
            return
        }

        isLoading = true

        try {
            coroutineScope {

                val tasksDeferred =
                    async(Dispatchers.IO) {
                        supabase
                            .from("planner_items")
                            .select {
                                filter {
                                    eq(
                                        "user_id",
                                        userId
                                    )
                                }
                            }
                            .decodeList<PlannerItem>()
                    }

                val sessionsDeferred =
                    async(Dispatchers.IO) {
                        supabase
                            .from("study_sessions")
                            .select {
                                filter {
                                    eq(
                                        "user_id",
                                        userId
                                    )
                                }
                            }
                            .decodeList<StudySession>()
                    }

                val tasks =
                    tasksDeferred.await()

                val sessions =
                    sessionsDeferred.await()

                updateProgress(
                    userId = userId,
                    tasks = tasks,
                    sessions = sessions
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    fun refreshProgressInBackground() {
        viewModelScope.launch {
            loadProgressDataAwait(
                forceRefresh = true
            )
        }
    }

    private fun updateProgress(
        userId: String,
        tasks: List<PlannerItem>,
        sessions: List<StudySession>
    ) {
        val completedTasks =
            tasks.count {
                it.status.equals(
                    "Completed",
                    ignoreCase = true
                )
            }

        val overallProgress =
            if (tasks.isEmpty()) {
                0f
            } else {
                completedTasks.toFloat() /
                        tasks.size.toFloat()
            }

        val today =
            LocalDate.now()

        val startOfWeek =
            today.with(
                DayOfWeek.SUNDAY
            )

        val weeklyHours =
            MutableList(7) { 0f }

        var todaySeconds =
            0

        val streakDates =
            mutableSetOf<LocalDate>()

        sessions.forEach { session ->

            try {
                val createdAt =
                    session.createdAt
                        ?: return@forEach

                val sessionDate =
                    ZonedDateTime
                        .parse(createdAt)
                        .toLocalDate()

                if (
                    sessionDate == today
                ) {
                    todaySeconds +=
                        session.durationSeconds
                }

                if (
                    !sessionDate.isBefore(
                        startOfWeek
                    ) &&
                    sessionDate.isBefore(
                        startOfWeek.plusDays(7)
                    )
                ) {
                    val dayIndex =
                        if (
                            sessionDate.dayOfWeek ==
                            DayOfWeek.SUNDAY
                        ) {
                            0
                        } else {
                            sessionDate
                                .dayOfWeek
                                .value
                        }

                    weeklyHours[dayIndex] +=
                        session.durationSeconds /
                                3600f
                }

                if (
                    session.durationSeconds >= 300
                ) {
                    streakDates.add(
                        sessionDate
                    )
                }

            } catch (_: Exception) {
            }
        }

        val streak =
            calculateStreak(
                activeDates = streakDates,
                today = today
            )

        val hours =
            todaySeconds / 3600

        val minutes =
            (todaySeconds % 3600) / 60

        val seconds =
            todaySeconds % 60

        val formattedTime =
            when {
                hours > 0 ->
                    "$hours hr $minutes min"

                minutes > 0 ->
                    "$minutes min"

                else ->
                    "$seconds sec"
            }

        _uiState.value =
            _uiState.value.copy(
                overallProgress =
                    overallProgress,

                studyStreak =
                    streak,

                weeklyStudyHours =
                    weeklyHours,

                todayStudyTime =
                    formattedTime,

                studySessions =
                    sessions
                        .sortedByDescending {
                            it.createdAt
                        }
            )

        loadedUserId =
            userId
    }

    private fun calculateStreak(
        activeDates: Set<LocalDate>,
        today: LocalDate
    ): Int {

        if (
            activeDates.isEmpty()
        ) {
            return 0
        }

        var currentDate =
            when {
                activeDates.contains(today) ->
                    today

                activeDates.contains(
                    today.minusDays(1)
                ) ->
                    today.minusDays(1)

                else ->
                    return 0
            }

        var streak =
            0

        while (
            activeDates.contains(
                currentDate
            )
        ) {
            streak++

            currentDate =
                currentDate.minusDays(1)
        }

        return streak
    }
}