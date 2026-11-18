package com.regainplus.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.regainplus.util.TimeUtils
import java.util.Calendar

data class BlockSchedule(val pkg: String, val startMin: Int, val endMin: Int, val days: String) {
    fun toPref(): String = "$pkg|$startMin|$endMin|$days"
    companion object {
        fun fromPref(s: String): BlockSchedule? {
            val p = s.split("|")
            if (p.size != 4) return null
            return BlockSchedule(p[0], p[1].toIntOrNull() ?: 0, p[2].toIntOrNull() ?: 0, p[3])
        }
    }
}

object ScheduleHolder {
    private val _schedules = MutableStateFlow<List<BlockSchedule>>(emptyList())
    val schedules: StateFlow<List<BlockSchedule>> = _schedules

    fun init(context: Context) {
        val set = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getStringSet("schedules", emptySet()) ?: emptySet()
        _schedules.value = set.mapNotNull { BlockSchedule.fromPref(it) }
    }

    fun set(schedules: List<BlockSchedule>, context: Context) {
        _schedules.value = schedules
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putStringSet("schedules", schedules.map { it.toPref() }.toSet()).apply()
    }

    fun isBlockedNow(pkg: String): Boolean {
        val now = Calendar.getInstance()
        val idx = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0..Sun=6
        val nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        return _schedules.value.any { s ->
            s.pkg == pkg && s.days.getOrElse(idx) { '0' } == '1' && inWindow(nowMin, s.startMin, s.endMin)
        }
    }

    private fun inWindow(now: Int, start: Int, end: Int): Boolean = TimeUtils.inWindow(now, start, end)
}
