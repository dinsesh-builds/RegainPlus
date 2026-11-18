package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.GymLog
import kotlinx.coroutines.flow.Flow

@Dao
interface GymDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(g: GymLog)
    @Query("SELECT * FROM gym_logs ORDER BY date DESC")
    fun all(): Flow<List<GymLog>>
    @Query("DELETE FROM gym_logs WHERE id=:id")
    suspend fun delete(id: String)
}