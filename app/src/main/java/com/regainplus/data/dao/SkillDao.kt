package com.regainplus.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.regainplus.data.entity.Skill
import com.regainplus.data.entity.SkillSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(s: Skill)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(s: SkillSession)
    @Query("SELECT * FROM skills ORDER BY name")
    fun skills(): Flow<List<Skill>>
    @Query("SELECT * FROM skills")
    suspend fun skillsOnce(): List<Skill>
    @Query("SELECT * FROM skill_sessions WHERE skillId=:id ORDER BY date DESC")
    fun sessions(id: String): Flow<List<SkillSession>>
    @Query("SELECT COALESCE(SUM(minutes),0) FROM skill_sessions WHERE skillId=:id")
    fun totalMinutes(id: String): Flow<Int>
    @Query("UPDATE skills SET active=:active WHERE id=:id")
    suspend fun setActive(id: String, active: Boolean)
    @Query("SELECT COUNT(*) FROM skills WHERE active=1")
    fun activeCount(): Flow<Int>
    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun deleteSkill(id: String)
    @Query("UPDATE skill_sessions SET skillId = :toId WHERE skillId = :fromId")
    suspend fun reassignSessions(fromId: String, toId: String)
}