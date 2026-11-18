package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfits")
data class Outfit(
    @PrimaryKey val id: String,
    val name: String,
    val itemIds: String,
    val occasion: String = "",
    val season: String = "",
    val lastWorn: Long? = null
)