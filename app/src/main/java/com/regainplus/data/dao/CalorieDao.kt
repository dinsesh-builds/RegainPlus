package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.CalorieLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: CalorieLog)
    @Query("SELECT * FROM calorie_logs ORDER BY date DESC")
    fun all(): Flow<List<CalorieLog>>
    @Query("SELECT COALESCE(SUM(calories),0) FROM calorie_logs WHERE date >= :start AND date < :end")
    fun totalBetween(start: Long, end: Long): Flow<Int>
    @Query("DELETE FROM calorie_logs WHERE id=:id")
    suspend fun delete(id: String)
}