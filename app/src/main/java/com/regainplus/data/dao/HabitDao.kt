package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.HabitLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(h: HabitLog)
    @Query("SELECT * FROM habit_logs WHERE date=:date")
    fun forDate(date: String): Flow<List<HabitLog>>
    @Query("SELECT DISTINCT date FROM habit_logs WHERE done=1 ORDER BY date DESC")
    fun activeDates(): Flow<List<String>>
}