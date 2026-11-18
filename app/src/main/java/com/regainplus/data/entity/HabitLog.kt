package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_logs", primaryKeys = ["date", "module"])
data class HabitLog(
    val date: String,
    val module: String,
    val done: Boolean = false,
    val minutes: Int = 0
)