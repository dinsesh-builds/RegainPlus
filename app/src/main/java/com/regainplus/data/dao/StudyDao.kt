package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(s: StudySession)
    @Query("SELECT * FROM study_sessions ORDER BY start DESC")
    fun all(): Flow<List<StudySession>>
    @Query("SELECT COALESCE(SUM(minutes),0) FROM study_sessions")
    fun totalMinutes(): Flow<Int>
    @Query("SELECT COALESCE(SUM(minutes),0) FROM study_sessions WHERE start >= :start AND start < :end")
    fun focusBetween(start: Long, end: Long): Flow<Int>
    @Query("DELETE FROM study_sessions WHERE id=:id")
    suspend fun delete(id: String)
}