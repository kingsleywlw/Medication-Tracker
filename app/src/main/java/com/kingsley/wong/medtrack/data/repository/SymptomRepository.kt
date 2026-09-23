package com.kingsley.wong.medtrack.data.repository

import android.content.Context
import com.kingsley.wong.medtrack.data.AppDatabase
import com.kingsley.wong.medtrack.data.entity.Symptom
import kotlinx.coroutines.flow.Flow

class SymptomRepository(context: Context) {

    private val symptomDao = AppDatabase.getDatabase(context).symptomDao()

    suspend fun insert(symptom: Symptom) = symptomDao.insert(symptom)

    suspend fun insertAll(symptoms: List<Symptom>) = symptomDao.insertAll(symptoms)

    fun getSymptomsByPatient(patientId: String): Flow<List<Symptom>> =
        symptomDao.getSymptomsByPatient(patientId)

    fun getAllSymptoms(): Flow<List<Symptom>> = symptomDao.getAllSymptoms()

    suspend fun getMostCommonCategory(): String? = symptomDao.getMostCommonCategory()

    suspend fun getAverageSeverity(): Double = symptomDao.getAverageSeverity()
}


