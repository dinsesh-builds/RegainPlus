package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metrics")
data class BodyMetric(
    @PrimaryKey val id: String,
    val date: Long,
    val weightKg: Double,
    val note: String = "",
    val photoUri: String? = null
)