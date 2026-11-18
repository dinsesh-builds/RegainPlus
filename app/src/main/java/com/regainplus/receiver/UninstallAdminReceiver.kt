package com.regainplus.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/** Opt-in device admin: makes RegainPlus harder to uninstall during focus streaks. */
class UninstallAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putBoolean("admin_active", true).apply()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putBoolean("admin_active", false).apply()
    }
}
