package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.BodyMetric
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(b: BodyMetric)
    @Query("SELECT * FROM body_metrics ORDER BY date DESC")
    fun all(): Flow<List<BodyMetric>>
}