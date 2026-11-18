package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.MoodDao
import com.regainplus.data.entity.MoodLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import androidx.glance.appwidget.updateAll

@HiltViewModel
class MindViewModel @Inject constructor(
    private val dao: MoodDao,
    @dagger.hilt.android.qualifiers.ApplicationContext private val ctx: android.content.Context
) : ViewModel() {
    val logs: StateFlow<List<MoodLog>> = dao.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun log(mood: Int, note: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            dao.insert(MoodLog(UUID.randomUUID().toString(), date, mood, note))
            try { com.regainplus.widget.StreakWidget().updateAll(ctx) } catch (e: Exception) { }
        }
    }
}