package com.regainplus.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BlockedNotification(val pkg: String, val title: String, val text: String, val ts: Long) {
    fun serialize(): String = listOf(pkg, title, text, ts.toString()).joinToString("|") { it.replace("|", "/") }
    companion object {
        fun deserialize(s: String): BlockedNotification? {
            val p = s.split("|")
            if (p.size != 4) return null
            return BlockedNotification(p[0].replace("/", "|"), p[1].replace("/", "|"), p[2].replace("/", "|"), p[3].toLongOrNull() ?: 0L)
        }
    }
}

/** In-memory mirror of notification-blocking prefs, shared by the listener service and UI. */
object NotificationBlockHolder {
    private const val PREFS = "notif_block"
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled
    private val _allowed = MutableStateFlow<Set<String>>(emptySet())
    val allowed: StateFlow<Set<String>> = _allowed
    private val _inbox = MutableStateFlow<List<BlockedNotification>>(emptyList())
    val inbox: StateFlow<List<BlockedNotification>> = _inbox

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        _enabled.value = prefs.getBoolean("enabled", false)
        _blockAll.value = prefs.getBoolean("blockAll", false)
        _allowed.value = prefs.getStringSet("allowed", emptySet()) ?: emptySet()
        _inbox.value = (prefs.getStringSet("inbox", emptySet()) ?: emptySet())
            .mapNotNull { BlockedNotification.deserialize(it) }.sortedByDescending { it.ts }
    }

    fun setEnabled(context: Context, value: Boolean) {
        _enabled.value = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", value).apply()
    }

    fun setAllowed(context: Context, packages: Set<String>) {
        _allowed.value = packages
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putStringSet("allowed", packages).apply()
    }

    fun isNotificationBlocked(pkg: String): Boolean {
        if (!_enabled.value || _allowed.value.contains(pkg)) return false
        val prefsBlockAll = false // read at init below
        return _blockAll.value || BlockHolder.isBlocked(pkg)
    }

    private val _blockAll = MutableStateFlow(false)
    val blockAll: StateFlow<Boolean> = _blockAll

    fun setBlockAll(context: Context, value: Boolean) {
        _blockAll.value = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("blockAll", value).apply()
    }

    fun addBlocked(context: Context, pkg: String, title: String, text: String) {
        val item = BlockedNotification(pkg, title, text, System.currentTimeMillis())
        val updated = (listOf(item) + _inbox.value).take(50)
        _inbox.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putStringSet("inbox", updated.map { it.serialize() }.toSet()).apply()
    }

    fun clearInbox(context: Context) {
        _inbox.value = emptyList()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove("inbox").apply()
    }
}
