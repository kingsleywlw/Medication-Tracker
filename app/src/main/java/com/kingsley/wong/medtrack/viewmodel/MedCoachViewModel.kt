package com.kingsley.wong.medtrack.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.kingsley.wong.medtrack.data.api.DrugLabel
import com.kingsley.wong.medtrack.data.api.RetrofitClient
import com.kingsley.wong.medtrack.data.entity.MedCoachTip
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.data.entity.Symptom
import com.kingsley.wong.medtrack.data.repository.MedCoachTipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.kingsley.wong.medtrack.BuildConfig

sealed interface DrugInfoState {
    object Idle : DrugInfoState
    object Loading : DrugInfoState
    data class Success(val drugLabel: DrugLabel) : DrugInfoState
    data class Error(val message: String) : DrugInfoState
}

sealed interface GenAiState {
    object Idle : GenAiState
    object Loading : GenAiState
    data class Success(val tip: String) : GenAiState
    data class Error(val message: String) : GenAiState
}

class MedCoachViewModel(
    private val tipRepository: MedCoachTipRepository,
    private val applicationContext: Context
) : ViewModel() {

    private val _drugInfoState = MutableStateFlow<DrugInfoState>(DrugInfoState.Idle)
    val drugInfoState: StateFlow<DrugInfoState> = _drugInfoState

    private val _genAiState = MutableStateFlow<GenAiState>(GenAiState.Idle)
    val genAiState: StateFlow<GenAiState> = _genAiState

    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    // Band E: Top half - OpenFDA drug lookup
    fun searchDrug(drugName: String) {
        if (drugName.isBlank()) return

        viewModelScope.launch {
            _drugInfoState.value = DrugInfoState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.openFdaService.searchDrug(
                        search = "openfda.brand_name:$drugName"
                    )
                }
                val label = response.results?.firstOrNull()
                if (label != null) {
                    _drugInfoState.value = DrugInfoState.Success(label)
                } else {
                    _drugInfoState.value = DrugInfoState.Error("No information found for \"$drugName\"")
                }
            } catch (e: Exception) {
                _drugInfoState.value = DrugInfoState.Error(
                    "Failed to fetch drug info: ${e.message}"
                )
            }
        }
    }


    fun generateTip(
        patientId: String,
        drugName: String,
        medications: List<Medication>,
        symptoms: List<Symptom>
    ) {
        viewModelScope.launch {
            _genAiState.value = GenAiState.Loading
            try {

                val medList = medications.joinToString(", ") { "${it.medicationName} (${it.dosage})" }
                val symptomList = symptoms.joinToString(", ") { "${it.category} (severity: ${it.severity})" }

                val prompt = """
                    Generate a short encouraging message to help someone stay on top of their medication schedule.
                    
                    This patient is currently taking: $medList.
                    They have reported these symptoms: $symptomList.
                    They are currently looking up: $drugName.
                    
                    Make the tip personalised based on their specific medications and symptoms.
                    Include any relevant advice about $drugName in relation to their other medications.
                    Keep it concise, warm, and encouraging.
                    End with a reminder that this is not a substitute for professional medical advice.
                """.trimIndent()

                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }

                val tipText = response.text ?: "No tip generated."
                _genAiState.value = GenAiState.Success(tipText)

                // Save tip to database
                val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                tipRepository.insert(
                    MedCoachTip(
                        patientId = patientId,
                        tipText = tipText,
                        timestamp = now
                    )
                )
            } catch (e: Exception) {
                _genAiState.value = GenAiState.Error(
                    "Failed to generate tip: ${e.message}"
                )
            }
        }
    }

    fun getTipsByPatient(patientId: String): Flow<List<MedCoachTip>> =
        tipRepository.getTipsByPatient(patientId)


    fun resetStates() {
        _drugInfoState.value = DrugInfoState.Idle
        _genAiState.value = GenAiState.Idle
    }
}