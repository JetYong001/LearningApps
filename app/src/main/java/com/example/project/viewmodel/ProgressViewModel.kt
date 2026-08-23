package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.PlannerItem
import com.example.project.model.StudySession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

data class ProgressUiState(
    val userName: String = "",
    val overallProgress: Float = 0f,
    val studyStreak: Int = 0,
    val weeklyStudyHours: List<Float> = List(7) { 0f },
    val todayStudyTime: String = "0 hr 0 min"
)

class ProgressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            val userId = user?.id ?: return@launch

            // 处理 Username：如果 Display Name 是空的，直接截取 Email 的前缀 (比如 jetyong001@gmail.com -> jetyong001)
            val emailName = user.email?.substringBefore("@") ?: "Student"
            val metaName = user.userMetadata?.get("name")?.toString()?.replace("\"", "")
            val finalUserName = if (!metaName.isNullOrBlank() && metaName != "null") metaName else emailName

            try {
                val tasks = supabase.from("planner_items").select {
                    filter { eq("user_id", userId) }
                }.decodeList<PlannerItem>()

                val completedTasks = tasks.count { it.status.equals("Completed", ignoreCase = true) }
                val progress = if (tasks.isNotEmpty()) completedTasks.toFloat() / tasks.size else 0f

                val sessions = supabase.from("study_sessions").select {
                    filter { eq("user_id", userId) }
                }.decodeList<StudySession>()

                val today = LocalDate.now()
                var todayMinutes = 0
                val weeklyHours = MutableList(7) { 0f }
                val startOfWeek = today.with(DayOfWeek.SUNDAY).minusWeeks(if (today.dayOfWeek == DayOfWeek.SUNDAY) 0 else 1)

                val activeDates = mutableSetOf<String>()

                sessions.forEach { session ->
                    try {
                        val sessionDate = ZonedDateTime.parse(session.createdAt).toLocalDate()
                        activeDates.add(sessionDate.format(DateTimeFormatter.ISO_LOCAL_DATE))

                        if (sessionDate.isEqual(today)) {
                            todayMinutes += session.durationMinutes
                        }

                        if (!sessionDate.isBefore(startOfWeek) && sessionDate.isBefore(startOfWeek.plusDays(7))) {
                            val dayIndex = if (sessionDate.dayOfWeek == DayOfWeek.SUNDAY) 0 else sessionDate.dayOfWeek.value
                            weeklyHours[dayIndex] += session.durationMinutes / 60f
                        }
                    } catch (e: Exception) { }
                }

                val sortedDates = activeDates.sortedDescending()
                var streak = 0
                var checkDate = today
                for (dateStr in sortedDates) {
                    val date = LocalDate.parse(dateStr)
                    if (date.isEqual(checkDate)) {
                        streak++
                        checkDate = checkDate.minusDays(1)
                    } else if (date.isEqual(today.minusDays(1)) && streak == 0) {
                        streak++
                        checkDate = today.minusDays(2)
                    } else {
                        break
                    }
                }

                _uiState.value = ProgressUiState(
                    userName = finalUserName,
                    overallProgress = progress,
                    studyStreak = streak,
                    weeklyStudyHours = weeklyHours,
                    todayStudyTime = "${todayMinutes / 60} hr ${todayMinutes % 60} min"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(userName = finalUserName)
            }
        }
    }
}