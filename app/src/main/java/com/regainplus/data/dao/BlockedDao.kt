package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.BlockedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(b: BlockedItem)
    @Query("SELECT * FROM blocked_items ORDER BY added DESC")
    fun all(): Flow<List<BlockedItem>>
    @Query("DELETE FROM blocked_items WHERE id=:id")
    suspend fun delete(id: String)
    @Query("DELETE FROM blocked_items")
    suspend fun clearAll()
}