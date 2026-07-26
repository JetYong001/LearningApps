package com.example.project.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login_page")
    object SignUp : Screen("sign_up_page")
    object Loading : Screen("loading_screen")


    object Dashboard : Screen("home_dashboard_page")
    object Planner : Screen("planner_page")
    object Notes : Screen("notes_page")
    object Progress : Screen("progress_page")


    object RemainingTask : Screen("remaining_task_page")
    object DeadlineTask : Screen("deadline_task_page")
    object CompletedTask : Screen("completed_task_page")
    object FocusSession : Screen("focus_session_page")


    object EditTask : Screen("edit_task_page")
    object EditProject : Screen("edit_project_page")


    object Material : Screen("material_page")
    object StartStudying : Screen("start_studying_page")
    object Flashcards : Screen("flashcards_page")


    object OverallProgress : Screen("overall_progress_page")
    object Settings : Screen("settings_page")
    object AccountCredentials : Screen("account_credentials_page")
    object DailyDigest : Screen("daily_digest_page")
    object DndMode : Screen("dnd_mode_page")
    object About : Screen("about_page")
    object EditProfile : Screen("edit_profile_page")
    object StudyReminder : Screen("study_reminder_page")
    object StreaksAlerts : Screen("streaks_alerts_page")
    object ThemeSelection : Screen("theme_selection_page")
    object LogoutDeleteAccount : Screen("logout_delete_account_page")
}