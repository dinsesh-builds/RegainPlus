package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wardrobe_items")
data class WardrobeItem(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val color: String,
    val season: String,
    val lastWorn: Long? = null,
    val photoUri: String? = null
)