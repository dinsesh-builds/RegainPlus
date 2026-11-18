package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.WardrobeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WardrobeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(w: WardrobeItem)
    @Query("SELECT * FROM wardrobe_items ORDER BY name")
    fun all(): Flow<List<WardrobeItem>>
    @Query("DELETE FROM wardrobe_items WHERE id=:id")
    suspend fun delete(id: String)
    @Query("UPDATE wardrobe_items SET lastWorn=:ts WHERE id=:id")
    suspend fun markWorn(id: String, ts: Long)
}