package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class Skill(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val active: Boolean = true
)

@Entity(tableName = "skill_sessions")
data class SkillSession(
    @PrimaryKey val id: String,
    val skillId: String,
    val date: Long,
    val minutes: Int,
    val notes: String
)