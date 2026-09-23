package com.kingsley.wong.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kingsley.wong.medtrack.data.dao.MedCoachTipDao
import com.kingsley.wong.medtrack.data.dao.MedicationDao
import com.kingsley.wong.medtrack.data.dao.PatientDao
import com.kingsley.wong.medtrack.data.dao.SymptomDao
import com.kingsley.wong.medtrack.data.entity.MedCoachTip
import com.kingsley.wong.medtrack.data.entity.Medication
import com.kingsley.wong.medtrack.data.entity.Patient
import com.kingsley.wong.medtrack.data.entity.Symptom

@Database(
    entities = [Patient::class, Medication::class, Symptom::class, MedCoachTip::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun medCoachTipDao(): MedCoachTipDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medtrack_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}