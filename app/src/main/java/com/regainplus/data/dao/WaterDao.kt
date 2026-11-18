package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs ORDER BY date DESC")
    fun all(): Flow<List<WaterLog>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: WaterLog)
    @Query("SELECT COALESCE(SUM(ml),0) FROM water_logs WHERE date >= :start AND date < :end")
    fun totalBetween(start: Long, end: Long): Flow<Int>
    @Query("DELETE FROM water_logs WHERE id=:id")
    suspend fun delete(id: String)
}