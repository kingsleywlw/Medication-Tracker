package com.kingsley.wong.medtrack.userinterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.data.entity.Patient
import com.kingsley.wong.medtrack.navigation.Screen
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController, patientViewModel: PatientViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = "" },
                label = { Text("Full Name") },
                isError = nameError.isNotEmpty()
            )
            if (nameError.isNotEmpty()) {
                Text(nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; phoneError = "" },
                label = { Text("Phone Number") },
                isError = phoneError.isNotEmpty()
            )
            if (phoneError.isNotEmpty()) {
                Text(phoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passwordError = "" },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError.isNotEmpty()
            )
            if (passwordError.isNotEmpty()) {
                Text(passwordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError.isNotEmpty()
            )
            if (confirmPasswordError.isNotEmpty()) {
                Text(confirmPasswordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                var valid = true

                if (name.isBlank()) { nameError = "Full name is required"; valid = false }

                if (phone.isBlank()) {
                    phoneError = "Phone number is required"; valid = false
                } else if (!phone.startsWith("04") || phone.length != 10 || !phone.all { it.isDigit() }) {
                    phoneError = "Phone must start with 04 and be 10 digits"; valid = false
                }

                if (password.isBlank()) {
                    passwordError = "Password is required"; valid = false
                } else if (password.length < 8) {
                    passwordError = "Password must be at least 8 characters"; valid = false
                } else if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) {
                    passwordError = "Password must contain at least one letter and one number"; valid = false
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password"; valid = false
                } else if (confirmPassword != password) {
                    confirmPasswordError = "Passwords do not match"; valid = false
                }

                if (valid) {
                    scope.launch {
                        // Check if phone already exists
                        val existing = patientViewModel.getPatientByPhone(phone)
                        if (existing != null) {
                            phoneError = "Phone number already registered"
                        } else {
                            // Generate new patient ID
                            val newId = patientViewModel.generateNewPatientId()
                            val newPatient = Patient(
                                patientId = newId,
                                phoneNumber = phone,
                                name = name,
                                password = password
                            )
                            patientViewModel.insertPatient(newPatient)

                            snackbarHostState.showSnackbar("Account created! Your Patient ID is $newId")

                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        }
                    }
                }
            }) { Text("Sign Up") }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
            }) { Text("Go to Login") }

            TextButton(onClick = {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(Screen.SignUp.route) { inclusive = true }
                }
            }) { Text("Back to Welcome") }
        }
    }
}