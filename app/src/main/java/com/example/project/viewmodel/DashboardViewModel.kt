package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.model.TaskItem
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
    val userName: String = "Yong",
    val currentTimeText: String = "",
    val focusState: FocusState = FocusState.IDLE,
    val remainingSeconds: Int = 0,
    val tasks: List<TaskItem> = listOf(
        TaskItem(1, "Python", "2.30pm", isCompleted = true),
        TaskItem(2, "MAD Practical 5", "4.00pm"),
        TaskItem(3, "DSA Tutorial 4", "6.00pm"),
        TaskItem(4, "SPC Tutorial 4", "8.00pm"),
        TaskItem(5, "AI Practical 3", "10.00pm")
    )
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