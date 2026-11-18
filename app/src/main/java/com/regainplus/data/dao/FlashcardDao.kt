package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.regainplus.data.entity.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(f: Flashcard)
    @Update
    suspend fun update(f: Flashcard)
    @Query("SELECT * FROM flashcards ORDER BY nextReview ASC")
    fun all(): Flow<List<Flashcard>>
    @Query("SELECT * FROM flashcards WHERE nextReview <= :now ORDER BY nextReview ASC")
    fun due(now: Long): Flow<List<Flashcard>>
    @Query("DELETE FROM flashcards WHERE id=:id")
    suspend fun delete(id: String)
}