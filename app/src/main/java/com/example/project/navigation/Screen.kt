package com.example.project.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login_page")
    object SignUp : Screen("sign_up_page")
    object Loading : Screen("loading_screen")

    object Dashboard : Screen("home_dashboard_page")
    object Planner : Screen("planner_page")
    object Notes : Screen("notes_page")
    object Progress : Screen("progress_page")

    object NoteDetail : Screen("note_detail_page") {
        fun createRoute(noteId: String) = "note_detail_page/$noteId"
    }

    object NoteView : Screen("note_view_page") {
        fun createRoute(noteId: String) = "note_view_page/$noteId"
    }

    object FocusSession : Screen("focus_session_page")
    object Settings : Screen("settings_page")

    object Profile : Screen("profile_page")
    object EditProfile : Screen("edit_profile_page")
    object ChangePassword : Screen("change_password")
}