package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_logs")
data class GymLog(
    @PrimaryKey val id: String,
    val exercise: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val date: Long
)