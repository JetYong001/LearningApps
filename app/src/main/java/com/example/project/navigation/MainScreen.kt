package com.example.project.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.R
import com.example.project.data.supabase
import com.example.project.ui.components.BottomNavigation
import com.example.project.ui.components.NavItem
import com.example.project.ui.screens.ChangePasswordScreen
import com.example.project.ui.screens.DashboardScreen
import com.example.project.ui.screens.EditProfileScreen
import com.example.project.ui.screens.FlashcardsScreen
import com.example.project.ui.screens.FocusSessionScreen
import com.example.project.ui.screens.LoginScreen
import com.example.project.ui.screens.NoteDetailScreen
import com.example.project.ui.screens.NoteViewScreen
import com.example.project.ui.screens.NotesScreen
import com.example.project.ui.screens.PlannerScreen
import com.example.project.ui.screens.ProfileScreen
import com.example.project.ui.screens.ProgressScreen
import com.example.project.ui.screens.SettingsScreen
import com.example.project.viewmodel.DashboardViewModel
import com.example.project.viewmodel.FlashcardsViewModel
import com.example.project.viewmodel.LoginViewModel
import com.example.project.viewmodel.NotesViewModel
import com.example.project.viewmodel.PlannerViewModel
import com.example.project.viewmodel.ProfileViewModel
import com.example.project.viewmodel.ProgressViewModel
import com.example.project.viewmodel.ThemeViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private val mainTabRoutes =
    listOf(
        Screen.Dashboard.route,
        Screen.Planner.route,
        Screen.Notes.route,
        Screen.Progress.route
    )

private val customTransitionRoutes =
    mainTabRoutes +
            listOf(
                Screen.Settings.route,
                Screen.Profile.route
            )

private fun shouldUseCustomTransition(
    initialRoute: String?,
    targetRoute: String?
): Boolean {

    val initial =
        initialRoute?.substringBefore("/")

    val target =
        targetRoute?.substringBefore("/")

    return initial in customTransitionRoutes &&
            target in customTransitionRoutes
}

private fun getTabIndex(
    route: String?
): Int {

    val cleanRoute =
        route?.substringBefore("/")

    return when (cleanRoute) {

        Screen.Dashboard.route ->
            0

        Screen.Planner.route ->
            1

        Screen.Notes.route,
        Screen.NoteDetail.route,
        Screen.NoteView.route,
        "flashcards_screen" ->
            2

        Screen.Progress.route,
        Screen.Profile.route,
        Screen.EditProfile.route,
        Screen.Settings.route,
        Screen.ChangePassword.route ->
            3

        else ->
            -1
    }
}

@Composable
fun MainScreen(
    themeViewModel: ThemeViewModel = viewModel()
) {
    val navController =
        rememberNavController()

    val dashboardViewModel:
            DashboardViewModel =
        viewModel()

    val plannerViewModel:
            PlannerViewModel =
        viewModel()

    val profileViewModel:
            ProfileViewModel =
        viewModel()

    val notesViewModel:
            NotesViewModel =
        viewModel()

    val progressViewModel:
            ProgressViewModel =
        viewModel()

    val navBackStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry
            ?.destination
            ?.route

    val showBottomBar =
        currentRoute !=
                Screen.Login.route &&
                currentRoute !=
                Screen.SignUp.route &&
                currentRoute !=
                Screen.Loading.route &&
                currentRoute !=
                Screen.FocusSession.route

    val navItems =
        remember {
            listOf(
                NavItem(
                    route =
                        Screen.Dashboard.route,
                    iconRes =
                        R.drawable.ic_nav_dashboard,
                    contentDescription =
                        "Dashboard"
                ),
                NavItem(
                    route =
                        Screen.Planner.route,
                    iconRes =
                        R.drawable.ic_nav_tasks,
                    contentDescription =
                        "Planner"
                ),
                NavItem(
                    route =
                        Screen.Notes.route,
                    iconRes =
                        R.drawable.ic_nav_notes,
                    contentDescription =
                        "Notes"
                ),
                NavItem(
                    route =
                        Screen.Progress.route,
                    iconRes =
                        R.drawable.ic_nav_progess,
                    contentDescription =
                        "Progress"
                )
            )
        }

    var selectedIndex by
    rememberSaveable {
        mutableIntStateOf(0)
    }

    LaunchedEffect(currentRoute) {

        val routeIndex =
            getTabIndex(
                currentRoute
            )

        if (
            routeIndex >= 0 &&
            routeIndex != selectedIndex
        ) {
            selectedIndex =
                routeIndex
        }
    }

    Scaffold(
        bottomBar = {

            if (showBottomBar) {

                BottomNavigation(
                    items =
                        navItems,
                    selectedIndex =
                        selectedIndex,
                    onTabSelected = { index ->

                        if (
                            index ==
                            selectedIndex
                        ) {
                            return@BottomNavigation
                        }

                        selectedIndex =
                            index

                        navController.navigate(
                            navItems[index].route
                        ) {

                            popUpTo(
                                navController
                                    .graph
                                    .findStartDestination()
                                    .id
                            ) {
                                saveState =
                                    true
                            }

                            launchSingleTop =
                                true

                            restoreState =
                                true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController =
                navController,
            startDestination =
                Screen.Loading.route,
            modifier =
                Modifier.padding(
                    innerPadding
                ),

            enterTransition = {

                val initialRoute =
                    initialState
                        .destination
                        .route

                val targetRoute =
                    targetState
                        .destination
                        .route

                if (
                    shouldUseCustomTransition(
                        initialRoute,
                        targetRoute
                    )
                ) {

                    val initialIndex =
                        getTabIndex(
                            initialRoute
                        )

                    val targetIndex =
                        getTabIndex(
                            targetRoute
                        )

                    if (
                        targetIndex >
                        initialIndex
                    ) {

                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                it
                            }
                        )

                    } else {

                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                -it
                            }
                        )
                    }

                } else {

                    EnterTransition.None
                }
            },

            exitTransition = {

                val initialRoute =
                    initialState
                        .destination
                        .route

                val targetRoute =
                    targetState
                        .destination
                        .route

                if (
                    shouldUseCustomTransition(
                        initialRoute,
                        targetRoute
                    )
                ) {

                    val initialIndex =
                        getTabIndex(
                            initialRoute
                        )

                    val targetIndex =
                        getTabIndex(
                            targetRoute
                        )

                    if (
                        targetIndex >
                        initialIndex
                    ) {

                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                -it
                            }
                        )

                    } else {

                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                it
                            }
                        )
                    }

                } else {

                    ExitTransition.None
                }
            },

            popEnterTransition = {

                val initialRoute =
                    initialState
                        .destination
                        .route

                val targetRoute =
                    targetState
                        .destination
                        .route

                if (
                    shouldUseCustomTransition(
                        initialRoute,
                        targetRoute
                    )
                ) {

                    val initialIndex =
                        getTabIndex(
                            initialRoute
                        )

                    val targetIndex =
                        getTabIndex(
                            targetRoute
                        )

                    if (
                        targetIndex <
                        initialIndex
                    ) {

                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                -it
                            }
                        )

                    } else {

                        slideInHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            initialOffsetX = {
                                it
                            }
                        )
                    }

                } else {

                    EnterTransition.None
                }
            },

            popExitTransition = {

                val initialRoute =
                    initialState
                        .destination
                        .route

                val targetRoute =
                    targetState
                        .destination
                        .route

                if (
                    shouldUseCustomTransition(
                        initialRoute,
                        targetRoute
                    )
                ) {

                    val initialIndex =
                        getTabIndex(
                            initialRoute
                        )

                    val targetIndex =
                        getTabIndex(
                            targetRoute
                        )

                    if (
                        targetIndex <
                        initialIndex
                    ) {

                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                it
                            }
                        )

                    } else {

                        slideOutHorizontally(
                            animationSpec =
                                tween(
                                    durationMillis = 200,
                                    easing =
                                        FastOutSlowInEasing
                                ),
                            targetOffsetX = {
                                -it
                            }
                        )
                    }

                } else {

                    ExitTransition.None
                }
            }
        ) {

            composable(
                Screen.Loading.route
            ) {

                LaunchedEffect(Unit) {

                    supabase
                        .auth
                        .sessionStatus
                        .collect { status ->

                            when (status) {

                                is SessionStatus.Authenticated -> {

                                    coroutineScope {

                                        val plannerJob =
                                            async {
                                                plannerViewModel
                                                    .loadItemsAwait()
                                            }

                                        val profileJob =
                                            async {
                                                profileViewModel
                                                    .loadProfileAwait(
                                                        forceRefresh = true
                                                    )
                                            }

                                        val progressJob =
                                            async {
                                                progressViewModel
                                                    .loadProgressDataAwait(
                                                        forceRefresh = true
                                                    )
                                            }

                                        plannerJob.await()
                                        profileJob.await()
                                        progressJob.await()
                                    }

                                    navController.navigate(
                                        Screen.Dashboard.route
                                    ) {

                                        popUpTo(
                                            Screen.Loading.route
                                        ) {
                                            inclusive =
                                                true
                                        }
                                    }
                                }

                                is SessionStatus.NotAuthenticated -> {

                                    profileViewModel
                                        .clearProfile()

                                    navController.navigate(
                                        Screen.Login.route
                                    ) {

                                        popUpTo(
                                            Screen.Loading.route
                                        ) {
                                            inclusive =
                                                true
                                        }
                                    }
                                }

                                else -> Unit
                            }
                        }
                }

                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            composable(
                Screen.Login.route
            ) {

                val loginViewModel:
                        LoginViewModel =
                    viewModel()

                LoginScreen(
                    viewModel =
                        loginViewModel,
                    onAuthSuccess = {

                        navController.navigate(
                            Screen.Loading.route
                        ) {

                            popUpTo(
                                Screen.Login.route
                            ) {
                                inclusive =
                                    true
                            }
                        }
                    }
                )
            }

            composable(
                Screen.Dashboard.route
            ) {

                DashboardScreen(
                    navController =
                        navController,
                    viewModel =
                        dashboardViewModel,
                    plannerViewModel =
                        plannerViewModel,
                    profileViewModel =
                        profileViewModel
                )
            }

            composable(
                Screen.Planner.route
            ) {

                PlannerScreen(
                    viewModel =
                        plannerViewModel
                )
            }

            composable(
                Screen.Notes.route
            ) {

                NotesScreen(
                    navController =
                        navController,
                    viewModel =
                        notesViewModel
                )
            }

            composable(
                Screen.NoteDetail.route
            ) {

                NoteDetailScreen(
                    navController =
                        navController,
                    viewModel =
                        notesViewModel,
                    noteId =
                        null
                )
            }

            composable(
                "${Screen.NoteDetail.route}/{noteId}"
            ) { backStackEntry ->

                val noteId =
                    backStackEntry
                        .arguments
                        ?.getString(
                            "noteId"
                        )

                NoteDetailScreen(
                    navController =
                        navController,
                    viewModel =
                        notesViewModel,
                    noteId =
                        noteId
                )
            }

            composable(
                "${Screen.NoteView.route}/{noteId}"
            ) { backStackEntry ->

                val noteId =
                    backStackEntry
                        .arguments
                        ?.getString(
                            "noteId"
                        )
                        ?: ""

                NoteViewScreen(
                    navController =
                        navController,
                    viewModel =
                        notesViewModel,
                    noteId =
                        noteId
                )
            }

            composable(
                Screen.Progress.route
            ) {

                ProgressScreen(
                    navController =
                        navController,
                    viewModel =
                        progressViewModel,
                    profileViewModel =
                        profileViewModel
                )
            }

            composable(
                Screen.Profile.route
            ) {

                ProfileScreen(
                    navController =
                        navController,
                    viewModel =
                        profileViewModel
                )
            }

            composable(
                Screen.EditProfile.route
            ) {

                EditProfileScreen(
                    navController =
                        navController,
                    viewModel =
                        profileViewModel
                )
            }

            composable(
                route =
                    Screen.FocusSession.route,

                enterTransition = {

                    slideInVertically(
                        animationSpec =
                            tween(220),
                        initialOffsetY = {
                            it
                        }
                    )
                },

                exitTransition = {

                    slideOutVertically(
                        animationSpec =
                            tween(220),
                        targetOffsetY = {
                            it
                        }
                    )
                },

                popEnterTransition = {

                    slideInVertically(
                        animationSpec =
                            tween(220),
                        initialOffsetY = {
                            it
                        }
                    )
                },

                popExitTransition = {

                    slideOutVertically(
                        animationSpec =
                            tween(220),
                        targetOffsetY = {
                            it
                        }
                    )
                }
            ) {

                FocusSessionScreen(
                    navController =
                        navController,
                    viewModel =
                        dashboardViewModel
                )
            }

            composable(
                "flashcards_screen/{subjectName}"
            ) { backStackEntry ->

                val subjectName =
                    backStackEntry
                        .arguments
                        ?.getString(
                            "subjectName"
                        )
                        ?: ""

                val flashcardsViewModel:
                        FlashcardsViewModel =
                    viewModel()

                FlashcardsScreen(
                    navController =
                        navController,
                    subjectName =
                        subjectName,
                    notesViewModel =
                        notesViewModel,
                    flashcardsViewModel =
                        flashcardsViewModel
                )
            }

            composable(
                Screen.Settings.route
            ) {

                SettingsScreen(
                    navController =
                        navController,
                    themeViewModel =
                        themeViewModel
                )
            }

            composable(
                Screen.ChangePassword.route
            ) {

                ChangePasswordScreen(
                    navController =
                        navController
                )
            }
        }
    }
}