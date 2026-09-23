package com.kingsley.wong.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingsley.wong.medtrack.data.entity.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    suspend fun getPatientById(patientId: String): Patient?

    @Query("SELECT * FROM patients WHERE phoneNumber = :phone")
    suspend fun getPatientByPhone(phone: String): Patient?

    @Query("SELECT * FROM patients WHERE patientId = :patientId AND phoneNumber = :phone")
    suspend fun getPatientByIdAndPhone(patientId: String, phone: String): Patient?

    @Query("UPDATE patients SET password = :password WHERE patientId = :patientId")
    suspend fun setPassword(patientId: String, password: String)

    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
}