package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.HabitDao
import com.regainplus.data.dao.StudyDao
import com.regainplus.data.entity.HabitLog
import com.regainplus.data.entity.StudySession
import com.regainplus.widget.StreakWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val dao: StudyDao,
    private val habitDao: HabitDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val ctx: android.content.Context
) : ViewModel() {
    val sessions: StateFlow<List<StudySession>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalMinutes: StateFlow<Int> = dao.totalMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun save(subject: String, notes: String, start: Long, end: Long) {
        viewModelScope.launch {
            val mins = ((end - start) / 60000).toInt().coerceAtLeast(1)
            dao.insert(StudySession(UUID.randomUUID().toString(), subject.ifBlank { "General" }, notes, start, end, mins))
            // Completing focus counts as a "clean day" — this is what drives streaks,
            // the widget, analytics and badges.
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            habitDao.upsert(HabitLog(today, "focus", true, mins))
            // Keep the home-screen widget in sync with the new focus minutes.
            try { com.regainplus.widget.StreakWidget().updateAll(ctx) } catch (e: Exception) { }
        }
    }
    fun delete(id: String) { viewModelScope.launch { dao.delete(id) } }
}