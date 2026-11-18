package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.Tip
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: Tip)
    @Query("SELECT * FROM tips ORDER BY category, title")
    fun all(): Flow<List<Tip>>
    @Query("SELECT * FROM tips WHERE category=:cat")
    fun byCategory(cat: String): Flow<List<Tip>>
}