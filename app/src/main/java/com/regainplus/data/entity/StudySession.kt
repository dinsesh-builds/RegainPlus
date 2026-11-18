package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String,
    val subject: String,
    val notes: String,
    val start: Long,
    val end: Long,
    val minutes: Int
)