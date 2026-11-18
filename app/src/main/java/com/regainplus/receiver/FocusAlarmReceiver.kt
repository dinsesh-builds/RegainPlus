package com.regainplus.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getIntExtra("slot", 0)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel("focus_alarm", "Focus session alarms", NotificationManager.IMPORTANCE_HIGH))
        }
        val open = android.app.PendingIntent.getActivity(
            context, 2000 + slot,
            Intent(context, com.regainplus.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(context, "focus_alarm")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus — time to focus")
            .setContentText("Your scheduled focus session is ready.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setFullScreenIntent(open, true)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(3000 + slot, n)
        // Re-arm for next week occurrence
        com.regainplus.service.FocusAlarmScheduler.rescheduleAll(context)
    }
}
