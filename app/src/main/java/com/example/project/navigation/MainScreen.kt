package com.example.project.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.data.supabase
import com.example.project.ui.screens.DashboardScreen
import com.example.project.ui.screens.FlashcardsScreen
import com.example.project.ui.screens.FocusSessionScreen
import com.example.project.ui.screens.LoginScreen
import com.example.project.ui.screens.NotesScreen
import com.example.project.ui.screens.NoteDetailScreen
import com.example.project.ui.screens.NoteViewScreen
import com.example.project.ui.screens.PlannerScreen
import com.example.project.ui.screens.ProgressScreen
import com.example.project.ui.screens.SettingsScreen
import com.example.project.viewmodel.FlashcardsViewModel
import com.example.project.viewmodel.LoginViewModel
import com.example.project.viewmodel.ThemeViewModel
import com.example.project.viewmodel.NotesViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val notesViewModel: NotesViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Screen.Login.route && currentRoute != Screen.SignUp.route && currentRoute != Screen.Loading.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Loading.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Loading.route) {
                LaunchedEffect(Unit) {
                    supabase.auth.sessionStatus.collect { status ->
                        when (status) {
                            is SessionStatus.Authenticated -> {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Loading.route) { inclusive = true }
                                }
                            }
                            is SessionStatus.NotAuthenticated -> {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Loading.route) { inclusive = true }
                                }
                            }
                            else -> {}
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            composable(Screen.Login.route) {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

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
                ProgressScreen(navController = navController)
            }

            composable(Screen.FocusSession.route) {
                FocusSessionScreen(navController = navController, viewModel = viewModel())
            }

            composable("flashcards_screen/{subjectName}") { backStackEntry ->
                val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                val previousEntry = remember(backStackEntry) { navController.previousBackStackEntry }
                val notesViewModel: NotesViewModel = if (previousEntry != null) {
                    viewModel(previousEntry)
                } else {
                    viewModel()
                }
                val flashcardsViewModel: FlashcardsViewModel = viewModel()

                FlashcardsScreen(
                    navController = navController,
                    subjectName = subjectName,
                    notesViewModel = notesViewModel,
                    flashcardsViewModel = flashcardsViewModel
                )
            }

            composable("settings") {
                SettingsScreen(navController = navController, themeViewModel = themeViewModel)
            }
        }
    }
}