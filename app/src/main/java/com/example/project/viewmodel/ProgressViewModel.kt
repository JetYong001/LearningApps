package com.example.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.ProgressRepository
import com.example.project.model.UserProfile
import com.example.project.model.StudySession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val userProfile: UserProfile? = null,
    val overallProgress: Float = 0f,
    val studySessions: List<StudySession> = emptyList(),
    val isLoading: Boolean = false
)

class ProgressViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        fetchProgressData()
    }

    fun fetchProgressData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val profile = ProgressRepository.fetchUserProfile()
            val progress = ProgressRepository.fetchOverallProgress()
            val sessions = ProgressRepository.fetchStudySessions()

            _uiState.update {
                it.copy(
                    userProfile = profile,
                    overallProgress = progress,
                    studySessions = sessions,
                    isLoading = false
                )
            }
        }
    }
}