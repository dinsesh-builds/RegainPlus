package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey val id: String,
    val date: String,
    val mood: Int,
    val note: String
)