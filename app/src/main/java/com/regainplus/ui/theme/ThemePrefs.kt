package com.regainplus.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Single source of truth for visual settings — shared by MainActivity and SettingsScreen. */
object ThemePrefs {
    private const val P = "settings"
    private val _theme = MutableStateFlow("system")
    val theme: StateFlow<String> = _theme
    private val _accent = MutableStateFlow("green")
    val accent: StateFlow<String> = _accent
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale
    private val _corners = MutableStateFlow("rounded")
    val corners: StateFlow<String> = _corners

    fun init(ctx: Context) {
        val p = ctx.getSharedPreferences(P, Context.MODE_PRIVATE)
        _theme.value = p.getString("theme", "system") ?: "system"
        _accent.value = p.getString("accent", "green") ?: "green"
        _fontScale.value = p.getFloat("font_scale", 1.0f)
        _corners.value = p.getString("corners", "rounded") ?: "rounded"
    }

    fun setTheme(ctx: Context, v: String) = set(ctx, "theme", v) { _theme.value = v }
    fun setAccent(ctx: Context, v: String) = set(ctx, "accent", v) { _accent.value = v }
    fun setFontScale(ctx: Context, v: Float) = set(ctx, "font_scale", v) { _fontScale.value = v }
    fun setCorners(ctx: Context, v: String) = set(ctx, "corners", v) { _corners.value = v }

    private fun set(ctx: Context, key: String, value: Any, apply: () -> Unit) {
        val e = ctx.getSharedPreferences(P, Context.MODE_PRIVATE).edit()
        when (value) {
            is String -> e.putString(key, value)
            is Float -> e.putFloat(key, value)
        }
        e.apply(); apply()
    }
}
