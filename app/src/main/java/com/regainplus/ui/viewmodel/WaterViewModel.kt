package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.WaterDao
import com.regainplus.data.entity.WaterLog
import com.regainplus.widget.StreakWidget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import java.util.UUID
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@HiltViewModel
class WaterViewModel @Inject constructor(private val dao: WaterDao, @dagger.hilt.android.qualifiers.ApplicationContext private val ctx: android.content.Context) : ViewModel() {
    private val prefs = ctx.getSharedPreferences("water", android.content.Context.MODE_PRIVATE)
    private val dayStart: Long get() {
        val c = java.util.Calendar.getInstance(); c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0); c.set(java.util.Calendar.SECOND, 0); c.set(java.util.Calendar.MILLISECOND, 0); return c.timeInMillis
    }
    val todayMl: StateFlow<Int> = dao.totalBetween(dayStart, dayStart + 86400000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    var goalMl by mutableStateOf(prefs.getInt("goal", 2000))
        private set
    fun setGoal(v: Int) { goalMl = v; prefs.edit().putInt("goal", v).apply() }
    fun add(ml: Int) { viewModelScope.launch { dao.insert(WaterLog(UUID.randomUUID().toString(), System.currentTimeMillis(), ml)); try { com.regainplus.widget.StreakWidget().updateAll(ctx) } catch (e: Exception) { } } }
}