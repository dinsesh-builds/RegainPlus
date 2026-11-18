package com.regainplus.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val deck: String,
    val front: String,
    val back: String,
    val ease: Float = 2.5f,
    val interval: Int = 1,
    val nextReview: Long = System.currentTimeMillis(),
    val reviews: Int = 0
)