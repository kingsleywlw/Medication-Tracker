package com.kingsley.wong.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kingsley.wong.medtrack.data.entity.MedCoachTip
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tip: MedCoachTip)

    @Query("SELECT * FROM medcoach_tips WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getTipsByPatient(patientId: String): Flow<List<MedCoachTip>>
}