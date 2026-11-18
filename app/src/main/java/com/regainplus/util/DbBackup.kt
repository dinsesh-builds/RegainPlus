package com.regainplus.util

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import java.io.File

/** Copies the Room DB to Downloads once, before a version change can destructively migrate it. */
object DbBackup {
    fun backupIfVersionChanged(ctx: Context) {
        try {
            val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
            val last = prefs.getInt("last_db_version", version)
            if (last == version) return
            val db = ctx.getDatabasePath("regainplus.db")
            if (!db.exists()) { prefs.edit().putInt("last_db_version", version).apply(); return }
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "regainplus_db_backup_v$last.db")
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = ctx.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return
            ctx.contentResolver.openOutputStream(uri)?.use { out -> db.inputStream().use { it.copyTo(out) } }
            prefs.edit().putInt("last_db_version", version).apply()
        } catch (_: Exception) { /* never block app start */ }
    }
}
