package com.regainplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.regainplus.service.AlarmRingingService

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getIntExtra("slot", 0)
        val svc = Intent(context, AlarmRingingService::class.java).putExtra("slot", slot)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                androidx.core.content.ContextCompat.startForegroundService(context, svc)
            } else {
                context.startService(svc)
            }
        } catch (_: Exception) {
            try { context.startService(svc) } catch (_: Exception) { }
        }
        // Re-arm for next week occurrence
        com.regainplus.service.FocusAlarmScheduler.rescheduleAll(context)
    }
}
