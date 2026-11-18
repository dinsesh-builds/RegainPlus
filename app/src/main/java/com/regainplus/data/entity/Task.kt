package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val title: String,
    val done: Boolean = false,
    val dueDate: Long? = null,
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)