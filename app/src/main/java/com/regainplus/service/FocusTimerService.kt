package com.regainplus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Keeps the focus timer visible and alive while the app is in the background. */
class FocusTimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("focus_timer", "Focus timer", NotificationManager.IMPORTANCE_LOW)
            )
        }
        startForeground(2002, notification("Focus timer running"))
        scope.launch {
            while (isActive && FocusTimerHolder.running.value) {
                FocusTimerHolder.syncRemaining(this@FocusTimerService)
                if (FocusTimerHolder.remaining.value <= 0) FocusTimerHolder.syncComplete(this@FocusTimerService)
                val r = FocusTimerHolder.remaining.value
                val label = "%02d:%02d".format(r / 60, r % 60)
                getSystemService(NotificationManager::class.java).notify(2002, notification(label))
                delay(1000)
            }
            stopSelf()
        }
        return START_STICKY
    }

    private fun notification(text: String): android.app.Notification {
        val pi = PendingIntent.getActivity(
            this, 3003, Intent(this, com.regainplus.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "focus_timer")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
