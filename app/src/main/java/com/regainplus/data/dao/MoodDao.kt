package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(m: MoodLog)
    @Query("SELECT * FROM mood_logs ORDER BY date DESC")
    fun all(): Flow<List<MoodLog>>
}