package com.regainplus

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.regainplus.service.BlockHolder
import com.regainplus.service.BlockMonitorService
import com.regainplus.service.NotificationBlockHolder
import com.regainplus.service.ScheduleHolder
import com.regainplus.service.ShortFormHolder
import com.regainplus.worker.DailyReminderWorker
import com.regainplus.worker.RestDayReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class RegainPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val appContext = applicationContext
        // Best-effort backup before migrations/version changes; run off main thread.
        Thread({ com.regainplus.util.DbBackup.backupIfVersionChanged(appContext) }, "db-backup").start()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        BlockHolder.init(this)
        ShortFormHolder.init(this)
        ScheduleHolder.init(this)
        NotificationBlockHolder.init(this)
        BlockMonitorService.sync(this)

        // Non-UI-critical scheduling can be deferred to reduce startup work on main thread.
        Thread({
            try { com.regainplus.service.FocusAlarmScheduler.rescheduleAll(appContext) } catch (_: Exception) {}
            try { scheduleDailyReminder(appContext, prefs.getBoolean("notif_daily", true)) } catch (_: Exception) {}
            try { scheduleRestDayReminder(appContext, prefs.getBoolean("notif_rest", false)) } catch (_: Exception) {}
            try { syncWatchdog(appContext) } catch (_: Exception) {}
        }, "startup-scheduling").start()
    }

    companion object {
        /**
         * True when any protection that depends on accessibility/monitoring is configured.
         * Used to avoid scheduling background watchdog work when nothing needs protection.
         */
        fun isProtectionConfigured(ctx: android.content.Context): Boolean {
            BlockHolder.init(ctx)
            ShortFormHolder.init(ctx)
            ScheduleHolder.init(ctx)
            val prefs = ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            return BlockHolder.blocked.value.isNotEmpty() ||
                BlockHolder.sites.value.isNotEmpty() ||
                BlockHolder.isStudyModeActive() ||
                ShortFormHolder.enabled.value ||
                ScheduleHolder.schedules.value.isNotEmpty() ||
                prefs.getBoolean("adult_block", false)
        }

        /** Periodic daily reminder, gated by the user's notification preference. */
        fun scheduleDailyReminder(ctx: android.content.Context, enabled: Boolean) {
            val wm = WorkManager.getInstance(ctx)
            if (enabled) {
                val req = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS).build()
                wm.enqueueUniquePeriodicWork("daily_reminder", ExistingPeriodicWorkPolicy.UPDATE, req)
            } else {
                wm.cancelUniqueWork("daily_reminder")
            }
        }

        /** Only runs when blocking/protection is configured; otherwise no background wakeups. */
        fun syncWatchdog(ctx: android.content.Context) {
            val wm = WorkManager.getInstance(ctx)
            if (isProtectionConfigured(ctx)) {
                val req = PeriodicWorkRequestBuilder<com.regainplus.worker.PermissionWatchdogWorker>(6, TimeUnit.HOURS).build()
                wm.enqueueUniquePeriodicWork("perm_watchdog", ExistingPeriodicWorkPolicy.UPDATE, req)
            } else {
                wm.cancelUniqueWork("perm_watchdog")
            }
        }

        fun scheduleRestDayReminder(ctx: android.content.Context, enabled: Boolean) {
            val wm = WorkManager.getInstance(ctx)
            if (enabled) {
                val req = PeriodicWorkRequestBuilder<RestDayReminderWorker>(24, TimeUnit.HOURS).build()
                wm.enqueueUniquePeriodicWork("rest_reminder", ExistingPeriodicWorkPolicy.UPDATE, req)
            } else {
                wm.cancelUniqueWork("rest_reminder")
            }
        }
    }
}
