package com.kingsley.wong.medtrack.util

import android.content.Context
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.data.entity.Patient
import com.kingsley.wong.medtrack.data.entity.Symptom
import com.kingsley.wong.medtrack.data.repository.MedicationRepository
import com.kingsley.wong.medtrack.data.repository.PatientRepository
import com.kingsley.wong.medtrack.data.repository.SymptomRepository
import java.io.BufferedReader
import java.io.InputStreamReader

object CsvSeeder {

    private const val PREFS_NAME = "MedTrackPrefs"
    private const val KEY_SEEDED = "database_seeded"

    fun isSeeded(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SEEDED, false)
    }

    private fun markSeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    suspend fun seedDatabase(
        context: Context,
        patientRepository: PatientRepository,
        medicationRepository: MedicationRepository,
        symptomRepository: SymptomRepository
    ) {
        if (isSeeded(context)) return

        // Seed patients
        val patients = readPatientsFromCsv(context)
        patientRepository.insertAll(patients)

        // Seed medications
        val medications = readMedicationsFromCsv(context)
        medicationRepository.insertAll(medications)

        // Seed symptoms
        val symptoms = readSymptomsFromCsv(context)
        symptomRepository.insertAll(symptoms)

        markSeeded(context)
    }

    private fun readPatientsFromCsv(context: Context): List<Patient> {
        val patients = mutableListOf<Patient>()
        val inputStream = context.assets.open("patients.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.readLine() // skip header

        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 3) {
                patients.add(
                    Patient(
                        patientId = parts[0].trim(),
                        phoneNumber = parts[1].trim(),
                        name = parts[2].trim(),
                        password = null  // passwords NOT imported from CSV
                    )
                )
            }
        }
        reader.close()
        return patients
    }

    private fun readMedicationsFromCsv(context: Context): List<Medication> {
        val medications = mutableListOf<Medication>()
        val inputStream = context.assets.open("medications.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.readLine() // skip header

        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 7) {
                medications.add(
                    Medication(
                        patientId = parts[0].trim(),
                        medicationName = parts[1].trim(),
                        dosage = parts[2].trim(),
                        frequency = parts[3].trim(),
                        scheduledTime = parts[4].trim(),
                        medicationType = parts[5].trim(),
                        notes = parts[6].trim()
                    )
                )
            }
        }
        reader.close()
        return medications
    }

    private fun readSymptomsFromCsv(context: Context): List<Symptom> {
        val symptoms = mutableListOf<Symptom>()
        val inputStream = context.assets.open("symptoms.csv")
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.readLine() // skip header

        reader.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 5) {
                symptoms.add(
                    Symptom(
                        patientId = parts[0].trim(),
                        category = parts[1].trim(),
                        severity = parts[2].trim().toIntOrNull() ?: 0,
                        notes = parts[3].trim(),
                        dateTime = parts[4].trim()
                    )
                )
            }
        }
        reader.close()
        return symptoms
    }
}