package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.SkillDao
import com.regainplus.data.entity.Skill
import com.regainplus.data.entity.SkillSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SkillsViewModel @Inject constructor(private val dao: SkillDao) : ViewModel() {
    val skills: StateFlow<List<Skill>> = dao.skills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val starter = listOf("Languages", "Singing", "Music", "Chess", "Calisthenics", "Cooking", "Dance", "Puzzles")
    @Volatile private var cleaned = false

    init {
        viewModelScope.launch { dedupeIfNeeded() }
    }

    private suspend fun dedupeIfNeeded() {
        if (cleaned) return
        val all = dao.skillsOnce()
        if (all.isEmpty()) return
        val groups = all.groupBy { it.name.trim().lowercase() }
        var changed = false
        groups.values.forEach { group ->
            if (group.size <= 1) return@forEach
            val canonical = group.firstOrNull { it.active } ?: group.first()
            group.filter { it.id != canonical.id }.forEach { dup ->
                dao.reassignSessions(dup.id, canonical.id)
                dao.deleteSkill(dup.id)
                changed = true
            }
        }
        cleaned = true
        // if dedupe changed nothing, still fine; flag prevents repeated work
        if (changed) {
            // allow active count cap to remain sensible; no-op beyond DB cleanup
        }
    }

    fun seed() {
        viewModelScope.launch {
            val existing = dao.skillsOnce().map { it.name.trim().lowercase() }.toSet()
            var activeCount = dao.skillsOnce().count { it.active }
            starter.forEach { name ->
                if (name.lowercase() !in existing) {
                    val makeActive = activeCount < 3
                    dao.insertSkill(Skill(UUID.randomUUID().toString(), name, name, makeActive))
                    if (makeActive) activeCount++
                }
            }
        }
    }

    fun addSession(skillId: String, minutes: Int, notes: String) {
        viewModelScope.launch { dao.insertSession(SkillSession(UUID.randomUUID().toString(), skillId, System.currentTimeMillis(), minutes, notes)) }
    }

    fun totalMinutes(skillId: String) = dao.totalMinutes(skillId)

    fun setActive(skill: Skill, active: Boolean) {
        viewModelScope.launch {
            val activeCount = skills.value.count { it.active && it.id != skill.id }
            if (active && activeCount >= 3) return@launch // cap: max 3 active
            dao.setActive(skill.id, active)
        }
    }
}