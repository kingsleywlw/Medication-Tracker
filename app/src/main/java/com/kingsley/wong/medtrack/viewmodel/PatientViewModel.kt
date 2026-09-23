package com.kingsley.wong.medtrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kingsley.wong.medtrack.data.entity.Patient
import com.kingsley.wong.medtrack.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _currentPatient = MutableStateFlow<Patient?>(null)
    val currentPatient: StateFlow<Patient?> = _currentPatient

    // Login: validate patientId + password
    suspend fun login(patientId: String, password: String): Boolean {
        val patient = repository.getPatientById(patientId)
        return if (patient != null && patient.password == password) {
            _currentPatient.value = patient
            true
        } else {
            false
        }
    }

    // Claim account: validate patientId + phoneNumber, then set password
    suspend fun claimAccount(patientId: String, phone: String, password: String): String {
        val patient = repository.getPatientByIdAndPhone(patientId, phone)
        return when {
            patient == null -> "No patient found with that ID and phone number."
            patient.password != null -> "This account has already been claimed."
            else -> {
                repository.setPassword(patientId, password)
                "Account claimed successfully!"
            }
        }
    }

    fun loadPatient(patientId: String) {
        viewModelScope.launch {
            _currentPatient.value = repository.getPatientById(patientId)
        }
    }

    fun logout() {
        _currentPatient.value = null
    }

    suspend fun getPatientCount(): Int = repository.getPatientCount()

    suspend fun getPatientByPhone(phone: String): Patient? =
        repository.getPatientByPhone(phone)

    suspend fun generateNewPatientId(): String {
        val count = repository.getPatientCount()
        return "P${1001 + count}"
    }

    suspend fun insertPatient(patient: Patient) {
        repository.insert(patient)
    }
}

