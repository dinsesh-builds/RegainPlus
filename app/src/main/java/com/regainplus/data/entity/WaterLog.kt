package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey val id: String,
    val date: Long,
    val ml: Int
)