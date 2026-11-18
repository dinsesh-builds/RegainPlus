package com.regainplus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = RegainAccent,
    onPrimary = Color(0xFF0F1509),
    primaryContainer = RegainDarkCardHi,
    secondary = RegainAccent2,
    tertiary = RegainTeal,
    background = RegainDarkBg,
    onBackground = RegainText,
    surface = RegainDarkSurface,
    onSurface = RegainText,
    surfaceVariant = RegainDarkCard,
    onSurfaceVariant = RegainMuted,
    surfaceContainer = RegainDarkSurface,
    surfaceContainerHigh = RegainDarkCard,
    surfaceContainerHighest = RegainDarkCardHi,
    outline = RegainBorder,
    outlineVariant = RegainBorder,
    error = RegainDanger
)

private val LightScheme = lightColorScheme(
    primary = RegainAccent,
    onPrimary = Color(0xFF0F1509),
    primaryContainer = Color(0xFFDCEFD0),
    secondary = RegainAccent2,
    onSecondary = Color.White,
    tertiary = RegainTeal,
    background = Color(0xFFF5F7F4),
    onBackground = Color(0xFF111412),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111412),
    surfaceVariant = Color(0xFFE6EBE4),
    onSurfaceVariant = Color(0xFF4A5248),
    surfaceContainer = Color(0xFFF0F3EE),
    surfaceContainerHigh = Color(0xFFE8EDE6),
    outline = Color(0xFFB8C0B5),
    outlineVariant = Color(0xFFD8DED6),
    error = RegainDanger
)

@Composable
fun RegainPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: String = "green",
    fontScale: Float = 1.0f,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val accentColor = when (accent.lowercase()) {
        "teal" -> RegainTeal
        "blue" -> RegainAccent2
        "orange" -> RegainOrange
        "auto" -> {
            // Innovative: energy-based adaptive accent — morning green, afternoon
            // blue, evening orange, night teal. No user input needed, fully offline.
            val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            when {
                h in 5..11 -> RegainAccent
                h in 12..16 -> RegainAccent2
                h in 17..20 -> RegainOrange
                else -> RegainTeal
            }
        }
        else -> RegainAccent
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkScheme.copy(
            primary = accentColor,
            secondary = accentColor,
            primaryContainer = accentColor.copy(alpha = 0.25f)
        )
        else -> LightScheme.copy(
            primary = accentColor,
            secondary = accentColor,
            primaryContainer = accentColor.copy(alpha = 0.18f)
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    val scaledTypography = Typography.let { base ->
        if (fontScale == 1.0f) base else androidx.compose.material3.Typography(
            displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * fontScale),
            displayMedium = base.displayMedium.copy(fontSize = base.displayMedium.fontSize * fontScale),
            displaySmall = base.displaySmall.copy(fontSize = base.displaySmall.fontSize * fontScale),
            headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * fontScale),
            headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * fontScale),
            headlineSmall = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize * fontScale),
            titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * fontScale),
            titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * fontScale),
            titleSmall = base.titleSmall.copy(fontSize = base.titleSmall.fontSize * fontScale),
            bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * fontScale),
            bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * fontScale),
            bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * fontScale),
            labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * fontScale),
            labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * fontScale),
            labelSmall = base.labelSmall.copy(fontSize = base.labelSmall.fontSize * fontScale)
        )
    }
    MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
}