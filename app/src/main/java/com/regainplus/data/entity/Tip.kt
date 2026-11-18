package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class Tip(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val body: String,
    val tier: String,
    val shownDate: String? = null
)