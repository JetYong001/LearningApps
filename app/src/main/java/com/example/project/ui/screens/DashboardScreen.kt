package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.project.navigation.Screen
import com.example.project.ui.components.FocusSessionCard
import com.example.project.ui.components.HeaderCard
import com.example.project.ui.components.ProgressSummaryCard
import com.example.project.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HeaderCard(
            userName = uiState.userName,
            onNotificationClick = { }
        )

        ProgressSummaryCard(
            completedTasks = uiState.completedCount,
            totalTasks = uiState.totalCount,
            nextTaskName = uiState.nextTask?.title ?: "No tasks left",
            nextTaskTime = uiState.nextTask?.time ?: ""
        )

        FocusSessionCard(
            currentTimeText = uiState.currentTimeText,
            onStartClick = {
                navController.navigate(Screen.FocusSession.route)
            }
        )
    }
}