package com.regainplus.service

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// In-memory mirror of the short-form blocker preference.
// Persisted in SharedPreferences ("settings"/"shorts_block") so the
// accessibility service survives process death.
object ShortFormHolder {
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled

    fun set(context: Context, value: Boolean) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit().putBoolean("shorts_block", value).apply()
        _enabled.value = value
    }

    fun init(context: Context) {
        _enabled.value = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("shorts_block", false)
    }
}
