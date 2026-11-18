package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calorie_logs")
data class CalorieLog(
    @PrimaryKey val id: String,
    val meal: String,
    val calories: Int,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val date: Long
)