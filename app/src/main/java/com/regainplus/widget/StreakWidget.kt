package com.regainplus.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.regainplus.MainActivity
import com.regainplus.data.db.AppDatabase
import java.util.Calendar
import kotlinx.coroutines.flow.first

class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val showHeader = prefs.getBoolean("widget_show_header", true)
        val showProgress = prefs.getBoolean("widget_show_sub", true) // repurpose as "show progress"

        // Back-compat: old single-stat pref; new multi-select prefs default based on it
        val legacy = prefs.getString("widget_stat", "streak") ?: "streak"
        val showStreak = prefs.getBoolean("widget_show_streak", legacy == "streak" || !prefs.contains("widget_stat"))
        val showWater = prefs.getBoolean("widget_show_water", legacy == "water")
        val showFocus = prefs.getBoolean("widget_show_focus", legacy == "focus")
        val showMood = prefs.getBoolean("widget_show_mood", legacy == "mood")

        val db = AppDatabase.getInstance(context)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        val dayEnd = dayStart + 86_400_000L

        val streak = prefs.getInt("streak", 0)
        val waterMl = db.waterDao().totalBetween(dayStart, dayEnd).first()
        val waterGoal = context.getSharedPreferences("water", Context.MODE_PRIVATE).getInt("goal", 2000)
        val focusMin = db.studyDao().focusBetween(dayStart, dayEnd).first()
        val mood = db.moodDao().all().first().firstOrNull()?.mood ?: 0

        data class Stat(val key: String, val value: String, val label: String)
        val stats = buildList {
            if (showStreak) add(Stat("streak", "$streak", "Streak"))
            if (showWater) add(Stat("water", "${waterMl}ml", "Water"))
            if (showFocus) add(Stat("focus", "${focusMin}m", "Focus"))
            if (showMood) add(Stat("mood", if (mood > 0) "$mood/5" else "—", "Mood"))
        }.ifEmpty { listOf(Stat("streak", "$streak", "Streak")) }

        // Progress bar: prefer streak, else water, else focus
        val (progress, progressLabel) = when {
            showStreak || (!showWater && !showFocus) -> (streak.coerceIn(0, 7) / 7f) to "$streak / 7 day streak"
            showWater -> ((waterMl.toFloat() / waterGoal.coerceAtLeast(1)).coerceIn(0f, 1f)) to "$waterMl / ${waterGoal}ml water"
            else -> ((focusMin.toFloat() / 60f).coerceIn(0f, 1f)) to "$focusMin / 60 min focus"
        }

        val brandGreen = androidx.glance.color.ColorProvider(
            androidx.compose.ui.graphics.Color(0xFF83D65C),
            androidx.compose.ui.graphics.Color(0xFF83D65C)
        )

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .cornerRadius(28.dp)
                        .background(GlanceTheme.colors.widgetBackground)
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(14.dp)
                ) {
                    if (showHeader) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = GlanceModifier
                                    .size(28.dp)
                                    .cornerRadius(8.dp)
                                    .background(brandGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = androidx.glance.ImageProvider(com.regainplus.R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = GlanceModifier.size(18.dp)
                                )
                            }
                            Spacer(GlanceModifier.width(8.dp))
                            Column {
                                Text(
                                    "RegainPlus",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.onSurface
                                    )
                                )
                                Text(
                                    "Today",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        color = GlanceTheme.colors.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Spacer(GlanceModifier.height(12.dp))
                    }

                    // Mini stat cards — miniature of home "Today at a glance"
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        stats.take(3).forEachIndexed { i, s ->
                            if (i > 0) Spacer(GlanceModifier.width(8.dp))
                            MiniStatCard(
                                modifier = GlanceModifier.defaultWeight(),
                                value = s.value,
                                label = s.label,
                                accent = accentFor(s.key),
                                container = GlanceTheme.colors.secondaryContainer
                            )
                        }
                    }

                    if (showProgress) {
                        Spacer(GlanceModifier.height(10.dp))
                        Text(
                            progressLabel,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = GlanceModifier.fillMaxWidth().height(6.dp).cornerRadius(3.dp),
                            color = brandGreen,
                            backgroundColor = GlanceTheme.colors.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    private fun accentFor(key: String): ColorProvider = when (key) {
        "water" -> androidx.glance.color.ColorProvider(
            androidx.compose.ui.graphics.Color(0xFF3593E0),
            androidx.compose.ui.graphics.Color(0xFF3593E0)
        )
        "focus" -> androidx.glance.color.ColorProvider(
            androidx.compose.ui.graphics.Color(0xFF83D65C),
            androidx.compose.ui.graphics.Color(0xFF83D65C)
        )
        "mood" -> androidx.glance.color.ColorProvider(
            androidx.compose.ui.graphics.Color(0xFF4FB3A8),
            androidx.compose.ui.graphics.Color(0xFF4FB3A8)
        )
        else -> androidx.glance.color.ColorProvider(
            androidx.compose.ui.graphics.Color(0xFF83D65C),
            androidx.compose.ui.graphics.Color(0xFF83D65C)
        )
    }
}

@Composable
private fun MiniStatCard(
    modifier: GlanceModifier,
    value: String,
    label: String,
    accent: ColorProvider,
    container: ColorProvider
) {
    Box(
        modifier = modifier
            .cornerRadius(14.dp)
            .background(container)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                value,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                ),
                maxLines = 1
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                label,
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 1
            )
        }
    }
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}
