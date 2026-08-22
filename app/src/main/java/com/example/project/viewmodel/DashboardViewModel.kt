package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.TaskItem
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

enum class FocusState {
    IDLE, FOCUSING, BREAK
}

data class DashboardUiState(
    val userName: String = "",
    val currentTimeText: String = "",
    val focusState: FocusState = FocusState.IDLE,
    val remainingSeconds: Int = 0,
    val tasks: List<TaskItem> = emptyList()
) {
    val completedCount: Int get() = tasks.count { it.isCompleted }
    val totalCount: Int get() = tasks.size
    val nextTask: TaskItem? get() = tasks.firstOrNull { !it.isCompleted }
}

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startClock()
        loadUserData()
        fetchTasks()
    }

    private fun startClock() {
        viewModelScope.launch {
            val formatter = DateTimeFormatter.ofPattern("hh : mm a")
            while (true) {
                val nowFormatted = LocalTime.now().format(formatter)
                _uiState.update { it.copy(currentTimeText = nowFormatted) }
                delay(1000L.milliseconds)
            }
        }
    }

    fun loadUserData() {
        val user = supabase.auth.currentUserOrNull()
        val email = user?.email.orEmpty()
        val displayName = email.substringBefore("@")
            .ifBlank { "User" }
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        _uiState.update {
            it.copy(userName = displayName)
        }
    }

    fun fetchTasks() {
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            try {
                val result = supabase.from("tasks")
                    .select {
                        filter {
                            eq("user_id", currentUserId)
                        }
                    }
                    .decodeList<TaskItem>()

                _uiState.update { it.copy(tasks = result) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startFocusSession(
        targetHours: Int,
        targetMinutes: Int,
        breakAfterMinute: Int,
        breakDurationMinute: Int,
        skipBreaks: Boolean
    ) {
        timerJob?.cancel()

        val totalFocusSeconds = targetHours * 3600 + targetMinutes * 60
        if (totalFocusSeconds <= 0) return

        timerJob = viewModelScope.launch {

            var remainingFocus = totalFocusSeconds
            val focusBlockSeconds = breakAfterMinute * 60
            val breakSeconds = breakDurationMinute * 60

            while (true) {

                val currentFocusSeconds =
                    if (skipBreaks || breakAfterMinute <= 0)
                        remainingFocus
                    else
                        minOf(remainingFocus, focusBlockSeconds)

                _uiState.update {
                    it.copy(
                        focusState = FocusState.FOCUSING,
                        remainingSeconds = currentFocusSeconds
                    )
                }

                while (_uiState.value.remainingSeconds > 0) {
                    delay(1000L)

                    _uiState.update {
                        it.copy(
                            remainingSeconds = it.remainingSeconds - 1
                        )
                    }
                }

                remainingFocus -= currentFocusSeconds

                if (remainingFocus <= 0)
                    break

                _uiState.update {
                    it.copy(
                        focusState = FocusState.BREAK,
                        remainingSeconds = breakSeconds
                    )
                }

                while (_uiState.value.remainingSeconds > 0) {
                    delay(1000L.milliseconds)

                    _uiState.update {
                        it.copy(
                            remainingSeconds = it.remainingSeconds - 1
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    focusState = FocusState.IDLE,
                    remainingSeconds = 0
                )
            }
        }
    }

    fun stopFocusSession() {
        timerJob?.cancel()
        _uiState.update { it.copy(focusState = FocusState.IDLE, remainingSeconds = 0) }
    }
}