package com.kingsley.wong.medtrack.userinterface

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.navigation.Screen
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, patientViewModel: PatientViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var patientId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it; errorMessage = "" },
            label = { Text("Patient ID") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (patientId.isBlank() || password.isBlank()) {
                    errorMessage = "Both fields are required"
                } else {
                    scope.launch {
                        val success = patientViewModel.login(patientId.trim(), password)
                        if (success) {
                            // Save session
                            saveSession(context, patientId.trim())
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            errorMessage = "Invalid Patient ID or password"
                        }
                    }
                }
            }
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate(Screen.ClaimAccount.route) }) {
            Text("Don't have a password? Claim your account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}

fun saveSession(context: Context, patientId: String) {
    val prefs = context.getSharedPreferences("MedTrackPrefs", Context.MODE_PRIVATE)
    prefs.edit().putString("logged_in_patient_id", patientId).apply()
}

fun getSession(context: Context): String? {
    val prefs = context.getSharedPreferences("MedTrackPrefs", Context.MODE_PRIVATE)
    return prefs.getString("logged_in_patient_id", null)
}

fun clearSession(context: Context) {
    val prefs = context.getSharedPreferences("MedTrackPrefs", Context.MODE_PRIVATE)
    prefs.edit().remove("logged_in_patient_id").apply()
}