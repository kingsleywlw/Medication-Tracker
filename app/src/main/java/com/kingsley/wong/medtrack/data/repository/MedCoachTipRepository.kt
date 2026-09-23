package com.kingsley.wong.medtrack.data.repository

import android.content.Context
import com.kingsley.wong.medtrack.data.AppDatabase
import com.kingsley.wong.medtrack.data.entity.MedCoachTip
import kotlinx.coroutines.flow.Flow

class MedCoachTipRepository(context: Context) {

    private val tipDao = AppDatabase.getDatabase(context).medCoachTipDao()

    suspend fun insert(tip: MedCoachTip) = tipDao.insert(tip)

    fun getTipsByPatient(patientId: String): Flow<List<MedCoachTip>> =
        tipDao.getTipsByPatient(patientId)
}