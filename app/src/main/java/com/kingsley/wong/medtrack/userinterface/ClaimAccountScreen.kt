package com.kingsley.wong.medtrack.userinterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.navigation.Screen
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import kotlinx.coroutines.launch

@Composable
fun ClaimAccountScreen(navController: NavController, patientViewModel: PatientViewModel) {
    val scope = rememberCoroutineScope()

    var patientId by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Claim Your Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Enter your Patient ID and phone number to set a password.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = patientId,
            onValueChange = { patientId = it; message = "" },
            label = { Text("Patient ID (e.g. P1001)") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it; message = "" },
            label = { Text("Phone Number") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; message = "" },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; message = "" },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                when {
                    patientId.isBlank() || phoneNumber.isBlank() ||
                            password.isBlank() || confirmPassword.isBlank() -> {
                        message = "All fields are required"
                        isError = true
                    }
                    password.length < 8 -> {
                        message = "Password must be at least 8 characters"
                        isError = true
                    }
                    !password.any { it.isLetter() } || !password.any { it.isDigit() } -> {
                        message = "Password must contain at least one letter and one number"
                        isError = true
                    }
                    password != confirmPassword -> {
                        message = "Passwords do not match"
                        isError = true
                    }
                    else -> {
                        scope.launch {
                            val result = patientViewModel.claimAccount(
                                patientId.trim(), phoneNumber.trim(), password
                            )
                            message = result
                            isError = result != "Account claimed successfully!"
                            if (!isError) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.ClaimAccount.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Text("Claim Account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
            Text("Already have a password? Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}