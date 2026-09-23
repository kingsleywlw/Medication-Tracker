package com.kingsley.wong.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptoms",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: String,
    val category: String,
    val severity: Int,
    val notes: String = "",
    val dateTime: String
)