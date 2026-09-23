package com.kingsley.wong.medtrack.userinterface

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kingsley.wong.medtrack.data.entity.Symptom
import com.kingsley.wong.medtrack.viewmodel.SymptomViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@SuppressLint("DefaultLocale")
@Composable
fun SymptomsScreen(
    patientId: String,
    symptomViewModel: SymptomViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var category by remember { mutableStateOf("") }
    var severity by remember { mutableFloatStateOf(1f) }
    var notes by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }

    var categoryError by remember { mutableStateOf("") }
    var dateTimeError by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    val symptomHistory by symptomViewModel.getSymptomsByPatient(patientId).collectAsState(initial = emptyList())

    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val time = String.format("%02d:%02d", hour, minute)
            dateTime = "$dateTime $time"
            dateTimeError = ""
        },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            dateTime = String.format("%04d-%02d-%02d", year, month + 1, day)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun severityColor(value: Float): Color = when {
        value <= 3f -> Color(0xFF4CAF50)
        value <= 6f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    fun severityLabel(value: Float): String = when {
        value <= 3f -> "Mild"
        value <= 6f -> "Moderate"
        else -> "Severe"
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Symptoms", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Button(onClick = { categoryExpanded = true }) {
                    Text(if (category.isBlank()) "Select Symptom Category" else category)
                }
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            category = option; categoryExpanded = false; categoryError = ""
                        })
                    }
                }
                if (categoryError.isNotEmpty()) {
                    Text(categoryError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Severity: ${severity.toInt()} — ${severityLabel(severity)}", color = severityColor(severity))
                Slider(
                    value = severity,
                    onValueChange = { severity = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = severityColor(severity),
                        activeTrackColor = severityColor(severity)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 200) notes = it },
                    label = { Text("Additional Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${notes.length}/200") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { datePickerDialog.show() }) {
                    Text(if (dateTime.isBlank()) "Select Date & Time" else "Date/Time: $dateTime")
                }
                if (dateTimeError.isNotEmpty()) {
                    Text(dateTimeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        var valid = true
                        if (category.isBlank()) {
                            categoryError = "Please select a symptom category"; valid = false
                        }
                        if (dateTime.isBlank()) {
                            dateTimeError = "Please select a date and time"; valid = false
                        }
                        if (valid) {
                            symptomViewModel.insertSymptom(
                                Symptom(
                                    patientId = patientId,
                                    category = category,
                                    severity = severity.toInt(),
                                    notes = notes,
                                    dateTime = dateTime
                                )
                            )
                            scope.launch { snackbarHostState.showSnackbar("Symptom saved successfully!") }
                            category = ""; severity = 1f; notes = ""; dateTime = ""
                        }
                    }) { Text("Save Symptom") }

                    Button(onClick = {
                        category = ""; severity = 1f; notes = ""; dateTime = ""
                        categoryError = ""; dateTimeError = ""
                    }) { Text("Clear") }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Symptom History", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (symptomHistory.isEmpty()) {
                    Text("No symptoms logged yet.")
                } else {
                    symptomHistory.forEach { symptom ->
                        val sColor = severityColor(symptom.severity.toFloat())
                        val sLabel = severityLabel(symptom.severity.toFloat())
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(symptom.category, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Severity: ${symptom.severity} — $sLabel", color = sColor)
                                Text("Date: ${symptom.dateTime}")
                                if (symptom.notes.isNotBlank()) { Text("Notes: ${symptom.notes}") }
                            }
                        }
                    }
                }
            }
        }
    }
}