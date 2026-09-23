package com.kingsley.wong.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingsley.wong.medtrack.data.entity.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptom: Symptom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(symptoms: List<Symptom>)

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
    fun getSymptomsByPatient(patientId: String): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms")
    fun getAllSymptoms(): Flow<List<Symptom>>

    @Query("SELECT category FROM symptoms GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostCommonCategory(): String?

    @Query("SELECT AVG(severity) FROM symptoms")
    suspend fun getAverageSeverity(): Double
}