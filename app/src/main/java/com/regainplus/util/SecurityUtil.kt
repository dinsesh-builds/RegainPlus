package com.regainplus.util

import android.os.Build
import java.io.File

object SecurityUtil {
    fun isRooted(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
            "/su/bin/su", "/vendor/bin/su", "/magisk/.core"
        )
        if (paths.any { File(it).exists() }) return true
        return Build.TAGS?.contains("test-keys") == true
    }
}
