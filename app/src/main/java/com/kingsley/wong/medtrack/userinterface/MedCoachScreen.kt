package com.kingsley.wong.medtrack.userinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kingsley.wong.medtrack.viewmodel.DrugInfoState
import com.kingsley.wong.medtrack.viewmodel.GenAiState
import com.kingsley.wong.medtrack.viewmodel.MedCoachViewModel
import com.kingsley.wong.medtrack.viewmodel.MedicationViewModel
import com.kingsley.wong.medtrack.viewmodel.SymptomViewModel
import androidx.compose.runtime.LaunchedEffect
@Composable
fun MedCoachScreen(
    patientId: String,
    medCoachViewModel: MedCoachViewModel,
    medicationViewModel: MedicationViewModel,
    symptomViewModel: SymptomViewModel
) {
    var drugName by remember { mutableStateOf("") }
    var showAllTips by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val drugInfoState by medCoachViewModel.drugInfoState.collectAsState()
    val genAiState by medCoachViewModel.genAiState.collectAsState()
    val medications by medicationViewModel.getMedicationsByPatient(patientId).collectAsState(initial = emptyList())
    val symptoms by symptomViewModel.getSymptomsByPatient(patientId).collectAsState(initial = emptyList())
    val allTips by medCoachViewModel.getTipsByPatient(patientId).collectAsState(initial = emptyList())

    // Reset states when entering MedCoach with a different user
    LaunchedEffect(patientId) {
        medCoachViewModel.resetStates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("MedCoach", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Drug name input
        OutlinedTextField(
            value = drugName,
            onValueChange = { drugName = it },
            label = { Text("Enter medication name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pre-populated dropdown with patient's medications (Band B requirement)
        if (medications.isNotEmpty()) {
            Button(onClick = { dropdownExpanded = true }) {
                Text("Select from your medications")
            }
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                medications.forEach { med ->
                    DropdownMenuItem(
                        text = { Text(med.medicationName) },
                        onClick = {
                            drugName = med.medicationName
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { medCoachViewModel.searchDrug(drugName) }) {
                Text("Look Up Drug")
            }
            Button(onClick = {
                medCoachViewModel.generateTip(patientId, drugName, medications, symptoms)
            }) {
                Text("Get AI Tips")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TOP HALF: OpenFDA Drug Info
        Text("Drug Information", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (drugInfoState) {
            is DrugInfoState.Idle -> Text("Search for a medication above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            is DrugInfoState.Loading -> CircularProgressIndicator()
            is DrugInfoState.Success -> {
                val label = (drugInfoState as DrugInfoState.Success).drugLabel
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Display at least 3 fields with "Not available" fallback
                        Text(
                            "Purpose: ${label.purpose?.firstOrNull() ?: "Not available"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Usage: ${label.indications_and_usage?.firstOrNull()?.take(300) ?: "Not available"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Warnings: ${label.warnings?.firstOrNull()?.take(300) ?: "Not available"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Active Ingredient: ${label.active_ingredient?.firstOrNull() ?: "Not available"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            is DrugInfoState.Error -> {
                Text((drugInfoState as DrugInfoState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BOTTOM HALF: GenAI Tips
        Text("AI Health Tips", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (genAiState) {
            is GenAiState.Idle -> Text("Tap 'Get AI Tips' for personalised advice.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            is GenAiState.Loading -> CircularProgressIndicator()
            is GenAiState.Success -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        (genAiState as GenAiState.Success).tip,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is GenAiState.Error -> {
                Text((genAiState as GenAiState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show All Tips button
        Button(onClick = { showAllTips = true }) {
            Text("Show All Tips")
        }

        // All Tips Dialog
        if (showAllTips) {
            AlertDialog(
                onDismissRequest = { showAllTips = false },
                title = { Text("All Saved Tips") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        if (allTips.isEmpty()) {
                            Text("No tips saved yet.")
                        } else {
                            allTips.forEach { tip ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(tip.tipText, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            tip.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAllTips = false }) { Text("Close") }
                }
            )
        }
    }
}