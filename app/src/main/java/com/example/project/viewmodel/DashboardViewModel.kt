package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.supabase
import com.example.project.model.StudySession
import com.example.project.model.StudySessionInsert
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

enum class FocusState {
    IDLE,
    FOCUSING,
    BREAK,
    PAUSED
}

data class DashboardUiState(
    val userName: String = "",
    val focusState: FocusState = FocusState.IDLE,
    val remainingSeconds: Int = 0
)

class DashboardViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            DashboardUiState()
        )

    val uiState: StateFlow<DashboardUiState> =
        _uiState.asStateFlow()

    private val _currentTimeText =
        MutableStateFlow("")

    val currentTimeText: StateFlow<String> =
        _currentTimeText.asStateFlow()

    private var clockJob: Job? = null
    private var timerJob: Job? = null

    private var remainingTotalFocusSeconds = 0
    private var focusBlockSeconds = 0
    private var breakDurationSeconds = 0
    private var breakRemainingSeconds = 0

    private var skipBreaks = false
    private var lastActiveState =
        FocusState.FOCUSING

    private var actualElapsedSeconds = 0

    init {
        startClock()
        loadUserData()
    }

    private fun startClock() {
        clockJob?.cancel()

        clockJob =
            viewModelScope.launch {
                val formatter =
                    DateTimeFormatter
                        .ofPattern("hh : mm a")

                while (true) {
                    _currentTimeText.value =
                        LocalTime
                            .now()
                            .format(formatter)

                    delay(1000L.milliseconds)
                }
            }
    }

    fun loadUserData() {
        val email =
            supabase.auth
                .currentUserOrNull()
                ?.email
                .orEmpty()

        val displayName =
            email
                .substringBefore("@")
                .ifBlank {
                    "User"
                }
                .replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase()
                    } else {
                        it.toString()
                    }
                }

        _uiState.update {
            it.copy(
                userName = displayName
            )
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

        val totalFocusSeconds =
            targetHours * 3600 +
                    targetMinutes * 60

        if (totalFocusSeconds <= 0) {
            return
        }

        remainingTotalFocusSeconds =
            totalFocusSeconds

        focusBlockSeconds =
            breakAfterMinute * 60

        breakDurationSeconds =
            breakDurationMinute * 60

        this.skipBreaks =
            skipBreaks

        breakRemainingSeconds = 0
        actualElapsedSeconds = 0
        lastActiveState =
            FocusState.FOCUSING

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.FOCUSING,
                remainingSeconds =
                    remainingTotalFocusSeconds
            )
        }

        runTimerLoop()
    }

    fun pauseFocusSession() {
        val currentState =
            _uiState.value.focusState

        if (
            currentState !=
            FocusState.FOCUSING &&
            currentState !=
            FocusState.BREAK
        ) {
            return
        }

        lastActiveState =
            currentState

        timerJob?.cancel()
        timerJob = null

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.PAUSED,
                remainingSeconds =
                    remainingTotalFocusSeconds
            )
        }
    }

    fun resumeFocusSession() {
        if (
            _uiState.value.focusState !=
            FocusState.PAUSED
        ) {
            return
        }

        _uiState.update {
            it.copy(
                focusState =
                    lastActiveState,
                remainingSeconds =
                    remainingTotalFocusSeconds
            )
        }

        runTimerLoop()
    }

    private fun runTimerLoop() {
        timerJob?.cancel()

        timerJob =
            viewModelScope.launch {
                while (
                    _uiState.value.focusState ==
                    FocusState.FOCUSING ||
                    _uiState.value.focusState ==
                    FocusState.BREAK
                ) {
                    delay(1000L.milliseconds)

                    when (
                        _uiState.value.focusState
                    ) {
                        FocusState.FOCUSING -> {
                            if (
                                remainingTotalFocusSeconds > 0
                            ) {
                                remainingTotalFocusSeconds--
                                actualElapsedSeconds++

                                _uiState.update {
                                    it.copy(
                                        remainingSeconds =
                                            remainingTotalFocusSeconds
                                    )
                                }
                            }

                            if (
                                remainingTotalFocusSeconds <=
                                0
                            ) {
                                finishFocusSession()
                                break
                            }

                            if (
                                !skipBreaks &&
                                focusBlockSeconds > 0 &&
                                actualElapsedSeconds %
                                focusBlockSeconds == 0
                            ) {
                                startBreak()
                            }
                        }

                        FocusState.BREAK -> {
                            if (
                                breakRemainingSeconds > 0
                            ) {
                                breakRemainingSeconds--
                            }

                            if (
                                breakRemainingSeconds <=
                                0
                            ) {
                                startNextFocusBlock()
                            }
                        }

                        else -> break
                    }
                }
            }
    }

    private fun startBreak() {
        if (
            breakDurationSeconds <= 0 ||
            remainingTotalFocusSeconds <= 0
        ) {
            return
        }

        breakRemainingSeconds =
            breakDurationSeconds

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.BREAK,
                remainingSeconds =
                    remainingTotalFocusSeconds
            )
        }

        lastActiveState =
            FocusState.BREAK
    }

    private fun startNextFocusBlock() {
        if (
            remainingTotalFocusSeconds <= 0
        ) {
            finishFocusSession()
            return
        }

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.FOCUSING,
                remainingSeconds =
                    remainingTotalFocusSeconds
            )
        }

        lastActiveState =
            FocusState.FOCUSING
    }

    private fun finishFocusSession() {
        timerJob?.cancel()
        timerJob = null

        val elapsedToSave =
            actualElapsedSeconds

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.IDLE,
                remainingSeconds = 0
            )
        }

        if (elapsedToSave > 0) {
            saveSessionToDatabase(
                elapsedToSave
            )
        }

        resetFocusValues()
    }

    fun stopFocusSession() {
        timerJob?.cancel()
        timerJob = null

        val elapsedToSave =
            actualElapsedSeconds

        _uiState.update {
            it.copy(
                focusState =
                    FocusState.IDLE,
                remainingSeconds = 0
            )
        }

        if (elapsedToSave > 0) {
            saveSessionToDatabase(
                elapsedToSave
            )
        }

        resetFocusValues()
    }

    fun endAndSaveFocusSession() {
        stopFocusSession()
    }
    fun exitFocusSession() {
        timerJob?.cancel()
        timerJob = null

        _uiState.update {
            it.copy(
                focusState = FocusState.IDLE,
                remainingSeconds = 0
            )
        }

        resetFocusValues()
    }


    private fun resetFocusValues() {
        remainingTotalFocusSeconds = 0
        breakRemainingSeconds = 0
        actualElapsedSeconds = 0
        focusBlockSeconds = 0
        breakDurationSeconds = 0
    }

    private fun saveSessionToDatabase(
        elapsedSeconds: Int
    ) {
        if (elapsedSeconds <= 0) {
            return
        }

        val currentUserId =
            supabase.auth
                .currentUserOrNull()
                ?.id
                ?: return

        viewModelScope.launch {
            try {
                val todayStart =
                    LocalDate
                        .now()
                        .atStartOfDay()
                        .toString() + ":00Z"

                val existingSessions =
                    supabase
                        .from("study_sessions")
                        .select {
                            filter {
                                eq(
                                    "user_id",
                                    currentUserId
                                )
                                gte(
                                    "created_at",
                                    todayStart
                                )
                            }
                        }
                        .decodeList<StudySession>()

                val todaySession =
                    existingSessions.firstOrNull()

                if (
                    todaySession != null &&
                    todaySession.id != null
                ) {
                    val newTotal =
                        todaySession.durationSeconds +
                                elapsedSeconds

                    supabase
                        .from("study_sessions")
                        .update(
                            {
                                set(
                                    "duration_seconds",
                                    newTotal
                                )
                            }
                        ) {
                            filter {
                                eq(
                                    "id",
                                    todaySession.id
                                )
                            }
                        }
                } else {
                    supabase
                        .from("study_sessions")
                        .insert(
                            StudySessionInsert(
                                userId =
                                    currentUserId,
                                durationSeconds =
                                    elapsedSeconds
                            )
                        )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        clockJob?.cancel()
        timerJob?.cancel()
        super.onCleared()
    }
}