package com.kingsley.wong.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingsley.wong.medtrack.data.entity.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<Medication>)

    @Query("SELECT * FROM medications WHERE patientId = :patientId")
    fun getMedicationsByPatient(patientId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("UPDATE medications SET isTaken = :taken, takenDate = :date WHERE id = :medId")
    suspend fun updateTakenStatus(medId: Int, taken: Boolean, date: String)

    @Query("UPDATE medications SET isTaken = 0, takenDate = null WHERE takenDate != :today")
    suspend fun resetTakenForNewDay(today: String)

    @Query("SELECT COUNT(*) FROM medications")
    suspend fun getMedicationCount(): Int

    @Query("SELECT AVG(medCount) FROM (SELECT COUNT(*) as medCount FROM medications GROUP BY patientId)")
    suspend fun getAverageMedsPerPatient(): Double

    @Query("SELECT * FROM medications WHERE id = :medId")
    suspend fun getMedicationById(medId: Int): Medication?
}