package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.Outfit
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(o: Outfit)
    @Query("SELECT * FROM outfits ORDER BY name")
    fun all(): Flow<List<Outfit>>
    @Query("DELETE FROM outfits WHERE id=:id")
    suspend fun delete(id: String)
    @Query("UPDATE outfits SET lastWorn=:ts WHERE id=:id")
    suspend fun markWorn(id: String, ts: Long)
}