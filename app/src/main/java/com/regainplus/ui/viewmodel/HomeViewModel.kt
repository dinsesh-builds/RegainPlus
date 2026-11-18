package com.regainplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regainplus.data.dao.HabitDao
import com.regainplus.data.dao.MoodDao
import com.regainplus.data.dao.StudyDao
import com.regainplus.data.dao.WaterDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.util.Calendar

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dao: HabitDao,
    private val waterDao: WaterDao,
    private val studyDao: StudyDao,
    private val moodDao: MoodDao
) : ViewModel() {
    val activeDates: StateFlow<List<String>> = dao.activeDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWaterMl: StateFlow<Int> = waterDao.totalBetween(dayStart(), dayStart() + 86400000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayFocusMin: StateFlow<Int> = studyDao.focusBetween(dayStart(), dayStart() + 86400000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayMood: StateFlow<Int> = moodDao.all()
        .map { it.firstOrNull()?.mood ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun streak(): Int {
        val days = activeDates.value.toSet()
        var s = 0
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val cal = java.util.Calendar.getInstance()
        for (i in 0..365) {
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            if (days.contains(fmt.format(cal.time))) s++ else if (i > 0) break
        }
        return s
    }

    private fun dayStart(): Long {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
