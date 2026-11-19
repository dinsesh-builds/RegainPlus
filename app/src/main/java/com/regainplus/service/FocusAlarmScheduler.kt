package com.regainplus.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object FocusAlarmScheduler {
    private const val PREFS = "focus_alarms"

    data class Alarm(val slot: Int, val hour: Int, val minute: Int, val days: String) {
        fun serialize() = "$slot|$hour|$minute|$days"
        companion object {
            fun deserialize(s: String): Alarm? {
                val p = s.split("|")
                if (p.size != 4) return null
                return Alarm(p[0].toIntOrNull() ?: 0, p[1].toIntOrNull() ?: 9, p[2].toIntOrNull() ?: 0, p[3])
            }
        }
    }

    fun all(context: Context): List<Alarm> =
        (context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getStringSet("items", emptySet()) ?: emptySet())
            .mapNotNull { Alarm.deserialize(it) }

    fun add(context: Context, alarm: Alarm) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = (prefs.getStringSet("items", emptySet()) ?: emptySet()).toMutableSet()
        set.removeAll { Alarm.deserialize(it)?.slot == alarm.slot }
        set.add(alarm.serialize())
        prefs.edit().putStringSet("items", set).apply()
        scheduleOne(context, alarm)
    }

    fun remove(context: Context, slot: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val set = (prefs.getStringSet("items", emptySet()) ?: emptySet()).filter { Alarm.deserialize(it)?.slot != slot }.toSet()
        prefs.edit().putStringSet("items", set).apply()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, slot))
    }

    fun rescheduleAll(context: Context) = all(context).forEach { scheduleOne(context, it) }

    private fun scheduleOne(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, alarm.slot))

        val next = nextTriggerMillis(alarm) ?: return
        val pi = pendingIntent(context, alarm.slot)
        val showPi = android.app.PendingIntent.getActivity(
            context, 3000 + alarm.slot,
            Intent(context, com.regainplus.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            // setAlarmClock is most reliable for user-visible alarms and can improve background delivery.
            am.setAlarmClock(AlarmManager.AlarmClockInfo(next, showPi), pi)
        } catch (_: Exception) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                }
            } catch (_: SecurityException) {
                try { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi) } catch (_: Exception) { }
            } catch (_: Exception) {
                try { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi) } catch (_: Exception) { }
            }
        }
    }

    private fun nextTriggerMillis(alarm: Alarm): Long? {
        val days = alarm.days
        if (!days.any { it == '1' }) return null
        val now = System.currentTimeMillis()
        var best: Long? = null
        for (i in 0 until 7) {
            if (days.getOrElse(i) { '0' } != '1') continue
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour.coerceIn(0, 23))
                set(Calendar.MINUTE, alarm.minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Calendar dayOfWeek: Sunday=1 -> ours Monday=0
            val targetDow = if (i == 6) Calendar.SUNDAY else i + 2
            // Move to this week's target day (relative to today)
            val currentDow = cal.get(Calendar.DAY_OF_WEEK)
            var delta = targetDow - currentDow
            if (delta < 0) delta += 7
            cal.add(Calendar.DAY_OF_YEAR, delta)
            if (cal.timeInMillis <= now) cal.add(Calendar.WEEK_OF_YEAR, 1)
            val t = cal.timeInMillis
            if (best == null || t < best!!) best = t
        }
        return best
    }

    private fun pendingIntent(context: Context, slot: Int): PendingIntent {
        val i = Intent(context, com.regainplus.receiver.FocusAlarmReceiver::class.java).putExtra("slot", slot)
        return PendingIntent.getBroadcast(context, 1000 + slot, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
