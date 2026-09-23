package com.kingsley.wong.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingsley.wong.medtrack.data.entity.Symptom
import com.kingsley.wong.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SymptomViewModel(private val repository: SymptomRepository) : ViewModel() {

    fun getSymptomsByPatient(patientId: String): Flow<List<Symptom>> =
        repository.getSymptomsByPatient(patientId)

    fun getAllSymptoms(): Flow<List<Symptom>> = repository.getAllSymptoms()

    fun insertSymptom(symptom: Symptom) {
        viewModelScope.launch {
            repository.insert(symptom)
        }
    }

    suspend fun getMostCommonCategory(): String? = repository.getMostCommonCategory()

    suspend fun getAverageSeverity(): Double = repository.getAverageSeverity()
}