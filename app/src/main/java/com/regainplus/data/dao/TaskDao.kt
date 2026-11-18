package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.regainplus.data.entity.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: Task)
    @Update
    suspend fun update(t: Task)
    @Query("SELECT * FROM tasks ORDER BY done ASC, priority DESC, createdAt DESC")
    fun all(): Flow<List<Task>>
    @Query("DELETE FROM tasks WHERE id=:id")
    suspend fun delete(id: String)
}