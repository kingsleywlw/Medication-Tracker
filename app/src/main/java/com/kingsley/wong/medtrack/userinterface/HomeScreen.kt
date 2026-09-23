package com.kingsley.wong.medtrack.userinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.navigation.Screen
import com.kingsley.wong.medtrack.util.NotificationHelper
import com.kingsley.wong.medtrack.viewmodel.MedicationViewModel
import com.kingsley.wong.medtrack.viewmodel.PatientViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    patientId: String,
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val patient by patientViewModel.currentPatient.collectAsState()
    val medications by medicationViewModel.getMedicationsByPatient(patientId).collectAsState(initial = emptyList())

    val today = LocalDate.now()
    val todayStr = today.toString()


    LaunchedEffect(todayStr) {
        medicationViewModel.resetTakenForNewDay()
    }


    LaunchedEffect(medications) {
        if (medications.isNotEmpty()) {
            NotificationHelper.scheduleMedicationReminders(context, medications)
        }
    }

    val takenCount = medications.count { it.isTaken && it.takenDate == todayStr }
    val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hello, ${patient?.name ?: "User"}",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Patient ID: ${patient?.patientId ?: ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "$takenCount of ${medications.size} medications taken today",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate(Screen.AddMedication.route) }
        ) {
            Text("Add Medication")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (medications.isEmpty()) {
            Text("No medications scheduled.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medications, key = { it.id }) { med ->
                    val isTaken = med.isTaken && med.takenDate == todayStr

                    MedicationCard(
                        medication = med,
                        isTaken = isTaken,
                        onToggleTaken = { checked ->
                            medicationViewModel.toggleTaken(med.id, checked)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(medication: Medication, isTaken: Boolean, onToggleTaken: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medication.medicationName,
                    style = if (isTaken)
                        TextStyle(textDecoration = TextDecoration.LineThrough)
                    else MaterialTheme.typography.titleSmall,
                    color = if (isTaken)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${medication.dosage} • ${medication.frequency}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Time: ${medication.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (medication.medicationType.isNotBlank()) {
                    Text(
                        text = "Type: ${medication.medicationType}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Checkbox(
                checked = isTaken,
                onCheckedChange = onToggleTaken
            )
        }
    }
}