package com.regainplus.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(@ApplicationContext private val ctx: Context) : ViewModel() {
    init { com.regainplus.ui.theme.ThemePrefs.init(ctx) }
    val darkMode get() = com.regainplus.ui.theme.ThemePrefs.theme.value
    val accent get() = com.regainplus.ui.theme.ThemePrefs.accent.value
    val fontScale get() = com.regainplus.ui.theme.ThemePrefs.fontScale.value
    val corners get() = com.regainplus.ui.theme.ThemePrefs.corners.value

    fun updateTheme(v: String) = com.regainplus.ui.theme.ThemePrefs.setTheme(ctx, v)
    fun updateAccent(v: String) = com.regainplus.ui.theme.ThemePrefs.setAccent(ctx, v)
    fun updateFontScale(v: Float) = com.regainplus.ui.theme.ThemePrefs.setFontScale(ctx, v)
    fun updateCorners(v: String) = com.regainplus.ui.theme.ThemePrefs.setCorners(ctx, v)
}
