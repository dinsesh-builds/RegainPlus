package com.regainplus.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.regainplus.service.BlockHolder
import com.regainplus.service.ShortFormHolder

/** Review-driven fix: phones revoke accessibility silently — tell the user blocking is off. */
class PermissionWatchdogWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext
        val configured = com.regainplus.RegainPlusApp.isProtectionConfigured(c)
        if (!configured) {
            com.regainplus.RegainPlusApp.syncWatchdog(c)
            return Result.success()
        }
        val enabled = Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        if (enabled.contains(c.packageName)) return Result.success()

        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel("perm_watch", "Blocking permission", NotificationManager.IMPORTANCE_HIGH))
        }
        val pi = android.app.PendingIntent.getActivity(
            c, 4004,
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        nm.notify(4004, NotificationCompat.Builder(c, "perm_watch")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus blocking is off")
            .setContentText("Accessibility was revoked. Tap to re-enable.")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build())
        return Result.success()
    }
}
