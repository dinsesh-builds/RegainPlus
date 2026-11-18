package com.regainplus.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RestDayReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel("recovery", "Recovery Reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val n = NotificationCompat.Builder(applicationContext, "recovery")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus")
            .setContentText("Recovery day: hydrate, sleep 8h, light walk. Muscles grow at rest.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        nm.notify(2, n)
        return Result.success()
    }
}
