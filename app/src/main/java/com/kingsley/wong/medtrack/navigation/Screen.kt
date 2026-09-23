package com.kingsley.wong.medtrack.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object ClaimAccount : Screen("claim_account")
    data object Home : Screen("home")
    data object AddMedication : Screen("add_medication")
    data object Symptoms : Screen("symptoms")
    data object MedCoach : Screen("medcoach")
    data object Settings : Screen("settings")
    data object ClinicianLogin : Screen("clinician_login")
    data object ClinicianDashboard : Screen("clinician_dashboard")
    data object SignUp : Screen("signup")
}