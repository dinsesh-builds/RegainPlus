package com.regainplus.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val qs = prefs.getInt("quiet_start", 22); val qe = prefs.getInt("quiet_end", 7)
        val quiet = if (qs > qe) hour >= qs || hour < qe else hour in qs until qe
        if (quiet) return Result.success()
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel("reminders", "Daily Reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val n = NotificationCompat.Builder(applicationContext, "reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus")
            .setContentText("Time for your daily habits. Keep your streak alive!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(1, n)
        return Result.success()
    }
}