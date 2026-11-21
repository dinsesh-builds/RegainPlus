package com.regainplus.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.regainplus.ui.components.ExpandableSection
import com.regainplus.ui.navigation.Screen
import com.regainplus.ui.viewmodel.HomeViewModel
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    navController: NavHostController? = null
) {
    val ctx = LocalContext.current
    val dates by vm.activeDates.collectAsState()
    val streak = remember(dates) { vm.streak() }
    LaunchedEffect(streak) {
        ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE).edit().putInt("streak", streak).apply()
        try { com.regainplus.widget.StreakWidget().updateAll(ctx) } catch (e: Exception) { }
    }
    var a11yEnabled by remember { mutableStateOf(isA11y(ctx)) }
    var overlayOk by remember { mutableStateOf(android.provider.Settings.canDrawOverlays(ctx)) }
    var alarmKey by remember { mutableStateOf(0) }
    val planPrefs = remember { ctx.getSharedPreferences("random_plan", android.content.Context.MODE_PRIVATE) }
    var dailyPlan by remember { mutableStateOf(planPrefs.getString("plan", "") ?: "") }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                a11yEnabled = isA11y(ctx)
                overlayOk = android.provider.Settings.canDrawOverlays(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val ringProgress = (streak.coerceAtMost(7).toFloat() / 7f)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        // Ring (Regain-style focus ring)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val progressColor = MaterialTheme.colorScheme.primary
            Canvas(Modifier.size(190.dp)) {
                val stroke = 26.dp.toPx()
                val inset = stroke / 2
                drawArc(
                    color = trackColor,
                    startAngle = -90f, sweepAngle = 360f, useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke)
                )
                drawArc(
                    color = progressColor,
                    startAngle = -90f, sweepAngle = 360f * ringProgress, useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - stroke, size.height - stroke)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$streak d", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text("Week streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))
 
        // Today at a glance — quick progress + tap to log
        val todayWater by vm.todayWaterMl.collectAsState()
        val todayFocus by vm.todayFocusMin.collectAsState()
        val todayMood by vm.todayMood.collectAsState()
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TodayStat("Water", "${todayWater}ml", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { navController?.navigate("body") }
                TodayStat("Focus", "${todayFocus}m", MaterialTheme.colorScheme.secondary, Modifier.weight(1f)) { navController?.navigate(Screen.Study.route) }
                TodayStat("Mood", if (todayMood > 0) "$todayMood/5" else "—", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f)) { navController?.navigate(Screen.Mind.route) }
            }
        }
 
        Spacer(Modifier.height(16.dp))
 
        // Streak + blocking cards
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Active days: ${dates.size}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when {
                            !a11yEnabled -> "BLOCKING IS OFF — tap Fix to turn on Accessibility for RegainPlus"
                            !overlayOk -> "BLOCKING IS OFF — tap Fix to allow display over other apps"
                            else -> "App blocking is live"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (a11yEnabled && overlayOk) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                    )
                }
                if (!a11yEnabled || !overlayOk) {
                    FilledTonalButton(onClick = {
                        if (!a11yEnabled) ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        else ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }) { Text("Fix") }
                } else {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick nav — horizontally scrollable (fixes overflow)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            navController?.let { nc ->
                listOf("wardrobe" to "Wardrobe", "skills" to "Skills", "flashcards" to "Flashcards", "weekly" to "Weekly", "settings" to "Settings")
                    .forEach { (route, label) ->
                        SuggestionChip(onClick = { nc.navigate(route) }, label = { Text(label) })
                    }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Suggested schedule — customizable time slots + weekdays
        val schedPrefs = remember { ctx.getSharedPreferences("schedule", android.content.Context.MODE_PRIVATE) }
        val defaultNames = listOf("Morning", "Afternoon", "Evening", "Night")
        val defaultTimes = listOf("06:00-12:00", "12:00-17:00", "17:00-21:00", "21:00-06:00")
        var slotIdx by remember { mutableStateOf(0) }
        var weekdays by remember { mutableStateOf(schedPrefs.getString("weekdays", "1111100") ?: "1111100") }
        var slotNames by remember {
            mutableStateOf(List(4) { i -> schedPrefs.getString("slot_name_$i", defaultNames[i]) ?: defaultNames[i] })
        }
        var slotTimes by remember {
            mutableStateOf(List(4) { i -> schedPrefs.getString("slot_time_$i", defaultTimes[i]) ?: defaultTimes[i] })
        }
        var editSlot by remember { mutableStateOf<Int?>(null) }
        var display24h by remember { mutableStateOf(schedPrefs.getBoolean("time_24h", false)) }

        ExpandableSection("Suggested schedule", subtitle = "Alarms and time slots", initiallyExpanded = true) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            slotNames.forEachIndexed { i, nm ->
                Card(
                    Modifier.clickable { slotIdx = i },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(14.dp).width(160.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(nm, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), maxLines = 1)
                            TextButton(onClick = { editSlot = i }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("Edit") }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(formatDisplayRange(slotTimes[i], display24h), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text("${weekdays.count { it == '1' }} days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        val alarmSet = remember(alarmKey, slotTimes) { com.regainplus.service.FocusAlarmScheduler.all(ctx).any { it.slot == i } }
                        TextButton(
                            onClick = {
                                if (alarmSet) {
                                    com.regainplus.service.FocusAlarmScheduler.remove(ctx, i)
                                } else {
                                    val hm = slotTimes[i].split("-").first().split(":")
                                    val alarm = com.regainplus.service.FocusAlarmScheduler.Alarm(i, hm.getOrNull(0)?.toIntOrNull() ?: 9, hm.getOrNull(1)?.toIntOrNull() ?: 0, weekdays)
                                    com.regainplus.service.FocusAlarmScheduler.add(ctx, alarm)
                                }
                                alarmKey++
                            },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (alarmSet) "Remove alarm" else "Set alarm", maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) }
                    }
                }
            }
        }

        if (editSlot != null) {
            val i = editSlot!!
            val (initStartH, initStartM, initEndH, initEndM) = parseSlotRange(slotTimes[i], defaultTimes[i])
            var editName by remember(i, slotNames) { mutableStateOf(slotNames[i]) }
            var startH by remember(i, slotTimes) { mutableIntStateOf(initStartH) }
            var startM by remember(i, slotTimes) { mutableIntStateOf(initStartM) }
            var endH by remember(i, slotTimes) { mutableIntStateOf(initEndH) }
            var endM by remember(i, slotTimes) { mutableIntStateOf(initEndM) }
            var pickingStart by remember(i) { mutableStateOf(false) }
            var pickingEnd by remember(i) { mutableStateOf(false) }
            var use24Hour by remember { mutableStateOf(schedPrefs.getBoolean("time_24h", false)) }

            AlertDialog(
                onDismissRequest = { editSlot = null },
                title = { Text("Edit schedule") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Label") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Text("Time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            TimePickCard(
                                label = "Start",
                                time = formatDisplayTime(startH, startM, use24Hour),
                                modifier = Modifier.weight(1f),
                                onClick = { pickingStart = true }
                            )
                            TimePickCard(
                                label = "End",
                                time = formatDisplayTime(endH, endM, use24Hour),
                                modifier = Modifier.weight(1f),
                                onClick = { pickingEnd = true }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("24-hour format", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            Switch(checked = use24Hour, onCheckedChange = {
                                use24Hour = it
                                display24h = it
                                schedPrefs.edit().putBoolean("time_24h", it).apply()
                            })
                        }
                        Text(
                            "Alarm uses the start time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val name = editName.trim().ifBlank { defaultNames[i] }
                        val time = "${formatHm(startH, startM)}-${formatHm(endH, endM)}"
                        val newNames = slotNames.toMutableList().also { it[i] = name }
                        val newTimes = slotTimes.toMutableList().also { it[i] = time }
                        slotNames = newNames
                        slotTimes = newTimes
                        schedPrefs.edit()
                            .putString("slot_name_$i", name)
                            .putString("slot_time_$i", time)
                            .apply()
                        if (com.regainplus.service.FocusAlarmScheduler.all(ctx).any { it.slot == i }) {
                            val alarm = com.regainplus.service.FocusAlarmScheduler.Alarm(i, startH, startM, weekdays)
                            com.regainplus.service.FocusAlarmScheduler.add(ctx, alarm)
                        }
                        alarmKey++
                        editSlot = null
                    }, shape = RoundedCornerShape(14.dp)) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { editSlot = null }) { Text("Cancel") }
                },
                shape = RoundedCornerShape(24.dp)
            )

            if (pickingStart) {
                TimePickerDialogWrap(
                    initialHour = startH,
                    initialMinute = startM,
                    is24Hour = use24Hour,
                    onDismiss = { pickingStart = false },
                    onConfirm = { h, m -> startH = h; startM = m; pickingStart = false }
                )
            }
            if (pickingEnd) {
                TimePickerDialogWrap(
                    initialHour = endH,
                    initialMinute = endM,
                    is24Hour = use24Hour,
                    onDismiss = { pickingEnd = false },
                    onConfirm = { h, m -> endH = h; endM = m; pickingEnd = false }
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            dayLabels.forEachIndexed { i, lbl ->
                FilterChip(selected = weekdays.getOrElse(i) { '0' } == '1', onClick = {
                    val arr = weekdays.toCharArray()
                    if (i < arr.size) arr[i] = if (arr[i] == '1') '0' else '1'
                    val nw = String(arr)
                    weekdays = nw
                    schedPrefs.edit().putString("weekdays", nw).apply()
                    // Keep existing slot alarms in sync with selected days
                    com.regainplus.service.FocusAlarmScheduler.all(ctx).forEach { a ->
                        com.regainplus.service.FocusAlarmScheduler.add(ctx, a.copy(days = nw))
                    }
                    alarmKey++
                }, label = { Text(lbl) })
            }
        }
        }

        Spacer(Modifier.height(12.dp))
        // Progress navigation: Today / Week / Month with arrows + summary
        var period by remember { mutableStateOf(0) } // 0 Today, 1 Week, 2 Month
        val periodNames = listOf("Today", "Week", "Month")
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        fun dateDaysAgo(days: Int) = fmt.format(java.util.Date(System.currentTimeMillis() - days * 86400000L))
        val todayDone = dateDaysAgo(0) in dates
        val weekDays = (0..6).count { dateDaysAgo(it) in dates }
        val monthDays = (0..29).count { dateDaysAgo(it) in dates }
        val summary = when (period) {
            0 -> if (todayDone) "Today is a clean day · keep the streak going" else "Not logged yet today · start a focus session"
            1 -> "$weekDays clean days in the last 7 days"
            else -> "$monthDays clean days in the last 30 days · long-term consistency"
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { period = (period + 2) % 3 }) { Text("‹", style = MaterialTheme.typography.titleLarge) }
            Text("${periodNames[period]} progress", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { period = (period + 1) % 3 }) { Text("›", style = MaterialTheme.typography.titleLarge) }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(12.dp)) {
                Text(summary, style = MaterialTheme.typography.bodyMedium)
                Text("Use arrows to switch period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(12.dp))
        var showPlan by remember { mutableStateOf(dailyPlan.isNotBlank()) }
        Surface(
            Modifier.fillMaxWidth().clickable { showPlan = !showPlan },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ideas for today", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(if (showPlan) "Hide" else "Show", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                if (showPlan) {
                    Spacer(Modifier.height(8.dp))
                    if (dailyPlan.isNotBlank()) Text(dailyPlan, style = MaterialTheme.typography.bodySmall)
                    else Text("Generate a simple plan when you want a nudge.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = {
                        val r = kotlin.random.Random
                        val focus = listOf("25m deep work", "50m study sprint", "90m focus block").random(r)
                        val gym = listOf("20m bodyweight circuit", "Push day (chest/shoulders)", "6k steps + mobility").random(r)
                        val glow = listOf("Cold shower + grooming", "10-min declutter", "No junk content evening").random(r)
                        val style = listOf("Plan outfit from closet", "Try a new colour combo", "Pick a Pinterest reference").random(r)
                        dailyPlan = "Focus: $focus\nGym: $gym\nGlow: $glow\nStyle: $style"
                        planPrefs.edit().putString("plan", dailyPlan).apply()
                    }) { Text(if (dailyPlan.isNotBlank()) "Refresh ideas" else "Generate ideas") }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Focus CTA
        Surface(
            Modifier.fillMaxWidth().clickable { navController?.navigate("study") },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp
        ) {
            Row(Modifier.padding(start = 24.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Start focus timer", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(12.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun isA11y(ctx: android.content.Context): Boolean {
    val enabled = android.provider.Settings.Secure.getString(ctx.contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
    return enabled.contains(ctx.packageName)
}

@Composable
private fun TodayStat(label: String, value: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TimePickCard(label: String, time: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            Modifier.padding(12.dp).heightIn(min = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogWrap(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = is24Hour
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select time",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                TimePicker(state = state)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(state.hour, state.minute) }, shape = RoundedCornerShape(12.dp)) { Text("OK") }
                }
            }
        }
    }
}

private fun formatHm(h: Int, m: Int): String = String.format(Locale.US, "%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59))

private fun formatDisplayTime(h: Int, m: Int, is24Hour: Boolean): String {
    val hh = h.coerceIn(0, 23)
    val mm = m.coerceIn(0, 59)
    return if (is24Hour) {
        String.format(Locale.US, "%02d:%02d", hh, mm)
    } else {
        val amPm = if (hh < 12) "AM" else "PM"
        val h12 = when (val x = hh % 12) { 0 -> 12; else -> x }
        String.format(Locale.US, "%d:%02d %s", h12, mm, amPm)
    }
}

private fun formatDisplayRange(range: String, is24Hour: Boolean): String {
    val parts = range.split("-")
    if (parts.size != 2) return range
    val (sh, sm) = parseHm(parts[0], 6, 0)
    val (eh, em) = parseHm(parts[1], 12, 0)
    return "${formatDisplayTime(sh, sm, is24Hour)} - ${formatDisplayTime(eh, em, is24Hour)}"
}

private fun parseHm(value: String, fallbackH: Int, fallbackM: Int): Pair<Int, Int> {
    val parts = value.trim().split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: fallbackH
    val m = parts.getOrNull(1)?.toIntOrNull() ?: fallbackM
    return h.coerceIn(0, 23) to m.coerceIn(0, 59)
}

private fun parseSlotRange(range: String, fallbackRange: String): Quad<Int, Int, Int, Int> {
    val src = if (range.contains("-")) range else fallbackRange
    val parts = src.split("-")
    val (sh, sm) = parseHm(parts.getOrNull(0) ?: "06:00", 6, 0)
    val (eh, em) = parseHm(parts.getOrNull(1) ?: "12:00", 12, 0)
    return Quad(sh, sm, eh, em)
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
