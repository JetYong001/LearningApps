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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.ui.screens.DashboardScreen
import com.example.project.ui.screens.FlashcardsScreen
import com.example.project.ui.screens.FocusSessionScreen
import com.example.project.ui.screens.NotesScreen
import com.example.project.ui.screens.NoteDetailScreen
import com.example.project.ui.screens.NoteViewScreen
import com.example.project.ui.screens.PlannerScreen
import com.example.project.ui.screens.ProgressScreen
import com.example.project.ui.screens.SettingsScreen
import com.example.project.viewmodel.ThemeViewModel
import com.example.project.viewmodel.NotesViewModel
import com.example.project.viewmodel.ProgressViewModel

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val notesViewModel: NotesViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.GridView, contentDescription = "Dashboard") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Planner.route,
                    onClick = {
                        navController.navigate(Screen.Planner.route) {
                            popUpTo(Screen.Planner.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Task, contentDescription = "Planner") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Notes.route,
                    onClick = {

                        navController.navigate(Screen.Notes.route) {
                            popUpTo(Screen.Notes.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Notes") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Progress.route,
                    onClick = {
                        navController.navigate(Screen.Progress.route) {
                            popUpTo(Screen.Progress.route) { inclusive = false }
                            launchSingleTop = true
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

            composable(Screen.Notes.route) {
                NotesScreen(navController = navController, viewModel = notesViewModel)
            }

            composable(Screen.NoteDetail.route) {
                NoteDetailScreen(
                    navController = navController,
                    viewModel = notesViewModel,
                    noteId = null
                )
            }

            composable("${Screen.NoteDetail.route}/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                NoteDetailScreen(
                    navController = navController,
                    viewModel = notesViewModel,
                    noteId = noteId
                )
            }

            composable(Screen.NoteView.route + "/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                NoteViewScreen(
                    navController = navController,
                    viewModel = notesViewModel,
                    noteId = noteId
                )
            }

            composable(Screen.Progress.route) {
                val progressViewModel: ProgressViewModel = viewModel()
                ProgressScreen(
                    navController = navController,
                    viewModel = progressViewModel
                )
            }

            composable(Screen.FocusSession.route) {
                FocusSessionScreen(navController = navController, viewModel = viewModel())
            }
            composable(Screen.Flashcards.route) { FlashcardsScreen() }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController, themeViewModel = themeViewModel)
            }
        }
    }
}