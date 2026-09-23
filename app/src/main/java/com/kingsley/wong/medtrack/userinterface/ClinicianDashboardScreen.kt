package com.kingsley.wong.medtrack.userinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.ai.client.generativeai.GenerativeModel
import com.kingsley.wong.medtrack.viewmodel.MedicationViewModel
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import com.kingsley.wong.medtrack.viewmodel.SymptomViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.kingsley.wong.medtrack.BuildConfig

@Composable
fun ClinicianDashboardScreen(
    navController: NavController,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    symptomViewModel: SymptomViewModel
) {
    val scope = rememberCoroutineScope()

    var patientCount by remember { mutableStateOf(0) }
    var medCount by remember { mutableStateOf(0) }
    var avgMeds by remember { mutableStateOf(0.0) }
    var avgSeverity by remember { mutableStateOf(0.0) }
    var mostCommonSymptom by remember { mutableStateOf("N/A") }

    var aiInsight by remember { mutableStateOf("") }
    var aiLoading by remember { mutableStateOf(false) }

    val allMedications by medicationViewModel.getAllMedications().collectAsState(initial = emptyList())
    val allSymptoms by symptomViewModel.getAllSymptoms().collectAsState(initial = emptyList())

    // Load stats
    LaunchedEffect(Unit) {
        patientCount = patientViewModel.getPatientCount()
        medCount = medicationViewModel.getMedicationCount()
        avgMeds = medicationViewModel.getAverageMedsPerPatient()
        avgSeverity = symptomViewModel.getAverageSeverity()
        mostCommonSymptom = symptomViewModel.getMostCommonCategory() ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Clinician Dashboard", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // 4 aggregate stats
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Patients: $patientCount", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Medications: $medCount", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Avg Medications per Patient: ${"%.1f".format(avgMeds)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Avg Symptom Severity: ${"%.1f".format(avgSeverity)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Most Common Symptom: $mostCommonSymptom", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GenAI Patterns
        Button(
            onClick = {
                aiLoading = true
                scope.launch {
                    try {
                        val model = GenerativeModel(
                            modelName = "gemini-3-flash-preview",
                            apiKey = BuildConfig.GEMINI_API_KEY
                        )

                        val medSummary = allMedications.groupBy { it.patientId }
                            .map { "${it.key}: ${it.value.joinToString(", ") { m -> m.medicationName }}" }
                            .joinToString("\n")

                        val symptomSummary = allSymptoms.groupBy { it.patientId }
                            .map { "${it.key}: ${it.value.joinToString(", ") { s -> "${s.category}(${s.severity})" }}" }
                            .joinToString("\n")

                        val prompt = """
                            You are a clinical data analyst. Analyse these patient records and identify exactly 3 patterns.
                            
                            Medications by patient:
                            $medSummary
                            
                            Symptoms by patient:
                            $symptomSummary
                            
                            Return exactly 3 interesting patterns or observations about this data.
                            Format each pattern as a numbered item (1. 2. 3.).
                            Focus on medication usage patterns, symptom correlations, and notable trends.
                            Keep each observation to 1-2 sentences. Be specific and data-driven.
                        """.trimIndent()

                        val response = withContext(Dispatchers.IO) {
                            model.generateContent(prompt)
                        }
                        aiInsight = response.text ?: "No insights generated."
                    } catch (e: Exception) {
                        aiInsight = "Error generating insights: ${e.message}"
                    }
                    aiLoading = false
                }
            },
            enabled = !aiLoading
        ) {
            Text(if (aiLoading) "Analysing..." else "Generate AI Insights")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (aiInsight.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(aiInsight, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}