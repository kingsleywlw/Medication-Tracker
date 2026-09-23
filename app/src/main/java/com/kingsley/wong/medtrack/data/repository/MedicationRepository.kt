package com.kingsley.wong.medtrack.data.repository

import android.content.Context
import com.kingsley.wong.medtrack.data.AppDatabase
import com.kingsley.wong.medtrack.data.entity.Medication
import kotlinx.coroutines.flow.Flow

class MedicationRepository(context: Context) {

    private val medicationDao = AppDatabase.getDatabase(context).medicationDao()

    suspend fun insert(medication: Medication) = medicationDao.insert(medication)

    suspend fun insertAll(medications: List<Medication>) = medicationDao.insertAll(medications)

    fun getMedicationsByPatient(patientId: String): Flow<List<Medication>> =
        medicationDao.getMedicationsByPatient(patientId)

    fun getAllMedications(): Flow<List<Medication>> = medicationDao.getAllMedications()

    suspend fun updateTakenStatus(medId: Int, taken: Boolean, date: String) =
        medicationDao.updateTakenStatus(medId, taken, date)

    suspend fun resetTakenForNewDay(today: String) =
        medicationDao.resetTakenForNewDay(today)

    suspend fun getMedicationCount(): Int = medicationDao.getMedicationCount()

    suspend fun getAverageMedsPerPatient(): Double = medicationDao.getAverageMedsPerPatient()
}