package com.kingsley.wong.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Scaffold
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kingsley.wong.medtrack.data.repository.MedCoachTipRepository
import com.kingsley.wong.medtrack.data.repository.MedicationRepository
import com.kingsley.wong.medtrack.data.repository.PatientRepository
import com.kingsley.wong.medtrack.data.repository.SymptomRepository
import com.kingsley.wong.medtrack.navigation.BottomNavBar
import com.kingsley.wong.medtrack.navigation.Screen
import com.kingsley.wong.medtrack.userinterface.*
import com.kingsley.wong.medtrack.ui.theme.MedTrackTheme
import com.kingsley.wong.medtrack.util.CsvSeeder
import com.kingsley.wong.medtrack.util.NotificationHelper
import com.kingsley.wong.medtrack.viewmodel.MedCoachViewModel
import com.kingsley.wong.medtrack.viewmodel.MedicationViewModel
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import com.kingsley.wong.medtrack.viewmodel.SymptomViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }

        // Create repositories
        val patientRepository = PatientRepository(applicationContext)
        val medicationRepository = MedicationRepository(applicationContext)
        val symptomRepository = SymptomRepository(applicationContext)
        val tipRepository = MedCoachTipRepository(applicationContext)

        // Create ViewModels
        val patientViewModel = PatientViewModel(patientRepository)
        val medicationViewModel = MedicationViewModel(medicationRepository)
        val symptomViewModel = SymptomViewModel(symptomRepository)
        val medCoachViewModel = MedCoachViewModel(tipRepository, applicationContext)

        enableEdgeToEdge()
        setContent {
            MedTrackTheme {
                val scope = rememberCoroutineScope()

                // Seed database on first launch
                LaunchedEffect(Unit) {
                    CsvSeeder.seedDatabase(applicationContext, patientRepository, medicationRepository, symptomRepository)
                }

                // Check session
                val loggedInPatientId = getSession(applicationContext)
                val startDestination = if (loggedInPatientId != null) {
                    // Load patient data
                    LaunchedEffect(loggedInPatientId) {
                        patientViewModel.loadPatient(loggedInPatientId)
                    }
                    Screen.Home.route
                } else {
                    Screen.Welcome.route
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show bottom nav only on main screens
                val showBottomBar = currentRoute in listOf(
                    Screen.Home.route,
                    Screen.Symptoms.route,
                    Screen.MedCoach.route,
                    Screen.Settings.route
                )

                // Get current patient ID from session
                val currentPatientId = getSession(applicationContext) ?: ""

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Welcome.route) {
                            WelcomeScreen(navController)
                        }
                        composable(Screen.Login.route) {
                            LoginScreen(navController, patientViewModel)
                        }
                        composable(Screen.ClaimAccount.route) {
                            ClaimAccountScreen(navController, patientViewModel)
                        }
                        composable(Screen.Home.route) {
                            LaunchedEffect(Unit) {
                                patientViewModel.loadPatient(currentPatientId)
                            }
                            HomeScreen(currentPatientId, patientViewModel, medicationViewModel, navController)
                        }
                        composable(Screen.AddMedication.route) {
                            AddMedicationScreen(navController, currentPatientId, medicationViewModel)
                        }
                        composable(Screen.Symptoms.route) {
                            SymptomsScreen(currentPatientId, symptomViewModel)
                        }
                        composable(Screen.MedCoach.route) {
                            MedCoachScreen(currentPatientId, medCoachViewModel, medicationViewModel, symptomViewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController, patientViewModel)
                        }
                        composable(Screen.ClinicianLogin.route) {
                            ClinicianLoginScreen(navController)
                        }
                        composable(Screen.ClinicianDashboard.route) {
                            ClinicianDashboardScreen(navController, patientViewModel, medicationViewModel, symptomViewModel)
                        }
                        composable(Screen.SignUp.route) {
                            SignUpScreen(navController, patientViewModel)
                        }
                    }
                }
            }
        }
    }
}