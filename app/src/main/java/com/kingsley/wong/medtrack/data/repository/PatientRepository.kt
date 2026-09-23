package com.kingsley.wong.medtrack.data.repository

import android.content.Context
import com.kingsley.wong.medtrack.data.AppDatabase
import com.kingsley.wong.medtrack.data.entity.Patient
import kotlinx.coroutines.flow.Flow

class PatientRepository(context: Context) {

    private val patientDao = AppDatabase.getDatabase(context).patientDao()

    suspend fun insert(patient: Patient) = patientDao.insert(patient)

    suspend fun insertAll(patients: List<Patient>) = patientDao.insertAll(patients)

    suspend fun getPatientById(patientId: String): Patient? = patientDao.getPatientById(patientId)

    suspend fun getPatientByPhone(phone: String): Patient? = patientDao.getPatientByPhone(phone)

    suspend fun getPatientByIdAndPhone(patientId: String, phone: String): Patient? =
        patientDao.getPatientByIdAndPhone(patientId, phone)

    suspend fun setPassword(patientId: String, password: String) =
        patientDao.setPassword(patientId, password)

    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAllPatients()

    suspend fun getPatientCount(): Int = patientDao.getPatientCount()
}