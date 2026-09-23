package com.kingsley.wong.medtrack.userinterface

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.viewmodel.MedicationViewModel
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@Composable
fun AddMedicationScreen(
    navController: NavController,
    patientId: String,
    medicationViewModel: MedicationViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var scheduledTime by remember { mutableStateOf("") }
    var medicationType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var medicationNameError by remember { mutableStateOf("") }
    var dosageError by remember { mutableStateOf("") }
    var frequencyError by remember { mutableStateOf("") }
    var scheduledTimeError by remember { mutableStateOf("") }
    var medicationTypeError by remember { mutableStateOf("") }

    var frequencyExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val frequencyOptions = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    val typeOptions = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            scheduledTime = String.format("%02d:%02d", hour, minute)
            scheduledTimeError = ""
        },
        8, 0, true
    )

    val dosagePattern = Regex("""^\d+(\.\d+)?(mg|ml|g)$""")

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Add Medication", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it; medicationNameError = "" },
                label = { Text("Medication Name") },
                isError = medicationNameError.isNotEmpty()
            )
            if (medicationNameError.isNotEmpty()) {
                Text(medicationNameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it; dosageError = "" },
                label = { Text("Dosage (e.g. 500mg, 10ml, 2g)") },
                isError = dosageError.isNotEmpty()
            )
            if (dosageError.isNotEmpty()) {
                Text(dosageError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { frequencyExpanded = true }) {
                Text(frequency.ifBlank { "Select Frequency" })
            }
            DropdownMenu(expanded = frequencyExpanded, onDismissRequest = { frequencyExpanded = false }) {
                frequencyOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        frequency = option; frequencyExpanded = false; frequencyError = ""
                    })
                }
            }
            if (frequencyError.isNotEmpty()) {
                Text(frequencyError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { timePickerDialog.show() }) {
                Text(if (scheduledTime.isBlank()) "Select Time" else "Time: $scheduledTime")
            }
            if (scheduledTimeError.isNotEmpty()) {
                Text(scheduledTimeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { typeExpanded = true }) {
                Text(medicationType.ifBlank { "Select Medication Type" })
            }
            DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                typeOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        medicationType = option; typeExpanded = false; medicationTypeError = ""
                    })
                }
            }
            if (medicationTypeError.isNotEmpty()) {
                Text(medicationTypeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                var valid = true
                if (medicationName.isBlank()) { medicationNameError = "Medication name is required"; valid = false }
                if (dosage.isBlank()) { dosageError = "Dosage is required"; valid = false }
                else if (!dosagePattern.matches(dosage)) { dosageError = "Dosage must be a number followed by mg, ml, or g"; valid = false }
                if (frequency.isBlank()) { frequencyError = "Please select a frequency"; valid = false }
                if (scheduledTime.isBlank()) { scheduledTimeError = "Please select a time"; valid = false }
                if (medicationType.isBlank()) { medicationTypeError = "Please select a medication type"; valid = false }

                if (valid) {
                    medicationViewModel.insertMedication(
                        Medication(
                            patientId = patientId,
                            medicationName = medicationName,
                            dosage = dosage,
                            frequency = frequency,
                            scheduledTime = scheduledTime,
                            medicationType = medicationType,
                            notes = notes
                        )
                    )
                    scope.launch { snackbarHostState.showSnackbar("Medication saved successfully!") }
                    medicationName = ""; dosage = ""; frequency = ""; scheduledTime = ""; medicationType = ""; notes = ""
                }
            }) { Text("Save") }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = {
                medicationName = ""; dosage = ""; frequency = ""; scheduledTime = ""
                medicationType = ""; notes = ""; medicationNameError = ""; dosageError = ""
                frequencyError = ""; scheduledTimeError = ""; medicationTypeError = ""
            }) { Text("Clear") }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = { navController.popBackStack() }) { Text("Back") }
        }
    }
}