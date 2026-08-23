package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project.navigation.Screen
import com.example.project.ui.components.OverallProgressRingCard
import com.example.project.ui.components.StudyConsistencyCard
import com.example.project.ui.components.StudyStreaksCard
import com.example.project.ui.components.UserProfileHeaderCard
import com.example.project.viewmodel.ProgressViewModel

@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val userName = uiState.userProfile?.fullName ?: "Yong Jet Hong"
    val userRole = uiState.userProfile?.role ?: "Student"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserProfileHeaderCard(
            name = userName,
            role = userRole,
            onSettingsClick = {
                navController.navigate(Screen.Settings.route)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                OverallProgressRingCard(
                    progress = uiState.overallProgress,
                    onClick = {
                    }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                StudyStreaksCard(
                    streakCount = 5
                )
            }
        }

        StudyConsistencyCard(
            sessions = uiState.studySessions
        )
    }
}