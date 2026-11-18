package com.regainplus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            com.regainplus.service.BlockMonitorService.rescheduleBoot(context)
            com.regainplus.service.FocusAlarmScheduler.rescheduleAll(context)
            com.regainplus.service.BlockHolder.init(context)
            com.regainplus.service.ShortFormHolder.init(context)
            com.regainplus.RegainPlusApp.syncWatchdog(context)
        }
    }
}