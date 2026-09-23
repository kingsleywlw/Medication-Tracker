package com.kingsley.wong.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.data.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {

    fun getMedicationsByPatient(patientId: String): Flow<List<Medication>> =
        repository.getMedicationsByPatient(patientId)

    fun getAllMedications(): Flow<List<Medication>> = repository.getAllMedications()

    fun insertMedication(medication: Medication) {
        viewModelScope.launch {
            repository.insert(medication)
        }
    }

    fun toggleTaken(medId: Int, taken: Boolean) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            repository.updateTakenStatus(medId, taken, today)
        }
    }

    fun resetTakenForNewDay() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            repository.resetTakenForNewDay(today)
        }
    }

    suspend fun getMedicationCount(): Int = repository.getMedicationCount()

    suspend fun getAverageMedsPerPatient(): Double = repository.getAverageMedsPerPatient()
}