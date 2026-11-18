package com.regainplus.service

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
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

/**
 * Independent blocking monitor: detects the foreground app via UsageStats
 * (usage access is granted) and shows the block screen. Does not depend on
 * AccessibilityService events, which MIUI can suppress.
 */
class BlockMonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastBlocked = ""
    private var lastBlockAt = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        BlockHolder.init(this)
        if (!isActiveNow()) {
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(3005, notification())
        scope.launch {
            while (isActive) {
                delay(1500)
                if (!isActiveNow()) {
                    stopSelf()
                    return@launch
                }
                try { checkForeground() } catch (_: Exception) {}
            }
        }
        return START_STICKY
    }

    private fun isActiveNow(): Boolean =
        BlockHolder.blocked.value.isNotEmpty() || BlockHolder.sites.value.isNotEmpty() || BlockHolder.isStudyModeActive()

    private fun hasUsage(): Boolean {
        val ops = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName) ==
            AppOpsManager.MODE_ALLOWED
    }

    private fun checkForeground() {
        BlockHolder.init(this)
        if (!isActiveNow()) return
        if (!hasUsage()) return
        if (BlockOverlayWindow.isShowing()) return
        val um = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = um.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 5000, now)
        val top = stats?.maxByOrNull { it.lastTimeUsed }?.packageName ?: return
        if (top == packageName || top == "com.android.systemui") return
        if (!BlockHolder.isBlocked(top)) { lastBlocked = ""; return }
        if (top == lastBlocked && System.currentTimeMillis() - lastBlockAt < 4000) return
        lastBlocked = top
        lastBlockAt = System.currentTimeMillis()
        // Push the blocked app to background immediately
        val home = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME); flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        try { startActivity(home) } catch (_: Exception) {}
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            BlockOverlayWindow.show(this, top, "app", "")
            if (!BlockOverlayWindow.isShowing()) {
                val i = Intent(this, BlockOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("pkg", top); putExtra("shorts", false)
                }
                try { startActivity(i) } catch (_: Exception) {}
            }
        }
    }

    private fun notification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("block_monitor", "App blocking", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val pi = PendingIntent.getActivity(this, 3006,
            Intent(this, com.regainplus.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "block_monitor")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("RegainPlus")
            .setContentText("App blocking is active")
            .setOngoing(true).setContentIntent(pi).build()
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }

    companion object {
        fun sync(context: Context) {
            BlockHolder.init(context)
            val active = BlockHolder.blocked.value.isNotEmpty() || BlockHolder.sites.value.isNotEmpty() || BlockHolder.isStudyModeActive()
            if (active) {
                try { context.startForegroundService(Intent(context, BlockMonitorService::class.java)) }
                catch (_: Exception) { try { context.startService(Intent(context, BlockMonitorService::class.java)) } catch (_: Exception) {} }
            } else {
                try { context.stopService(Intent(context, BlockMonitorService::class.java)) } catch (_: Exception) {}
            }
        }
        fun rescheduleBoot(context: Context) = sync(context)
    }
}
