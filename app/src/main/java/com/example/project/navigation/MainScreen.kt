package com.example.project.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.screens.DashboardScreen
import com.example.project.ui.screens.FlashcardsScreen
import com.example.project.ui.screens.FocusSessionScreen
import com.example.project.ui.screens.NotesScreen
import com.example.project.ui.screens.PlannerScreen
import com.example.project.ui.screens.ProgressScreen
import com.example.project.ui.screens.SettingsScreen
import com.example.project.viewmodel.ThemeViewModel

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel = viewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Planner.route,
                    onClick = {
                        navController.navigate(Screen.Planner.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Task, contentDescription = "Planner") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Notes.route,
                    onClick = {
                        navController.navigate(Screen.Notes.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Notes") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Progress.route,
                    onClick = {
                        navController.navigate(Screen.Progress.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController, viewModel = viewModel())
            }
            composable(Screen.Planner.route) { PlannerScreen() }
            composable(Screen.Notes.route) { NotesScreen() }
            composable(Screen.Progress.route) {
                ProgressScreen(navController = navController)
            }

            composable(Screen.FocusSession.route) {
                FocusSessionScreen(navController = navController, viewModel = viewModel())
            }
            composable(Screen.Flashcards.route) { FlashcardsScreen() }

            composable("settings") {
                SettingsScreen(navController = navController, themeViewModel = themeViewModel)
            }
        }
    }
}