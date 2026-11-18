package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_items")
data class BlockedItem(
    @PrimaryKey val id: String,
    val kind: String,
    val name: String,
    val added: Long
)