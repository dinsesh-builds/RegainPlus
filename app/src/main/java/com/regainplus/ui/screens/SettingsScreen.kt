package com.regainplus.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.RegainPlusApp
import com.regainplus.receiver.UninstallAdminReceiver
import com.regainplus.ui.components.ExpandableSection
import com.regainplus.ui.viewmodel.ExportViewModel
import com.regainplus.ui.viewmodel.SettingsViewModel
import com.regainplus.widget.StreakWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: SettingsViewModel = hiltViewModel(), exportVm: ExportViewModel = hiltViewModel()) {
    val curMode by com.regainplus.ui.theme.ThemePrefs.theme.collectAsState()
    val curAccent by com.regainplus.ui.theme.ThemePrefs.accent.collectAsState()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current
    val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(ctx, UninstallAdminReceiver::class.java)
    var isAdmin by remember { mutableStateOf(dpm.isAdminActive(adminComponent)) }
    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isAdmin = dpm.isAdminActive(adminComponent)
    }
    val prefs = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)
    var notifDaily by remember { mutableStateOf(prefs.getBoolean("notif_daily", true)) }
    var notifRest by remember { mutableStateOf(prefs.getBoolean("notif_rest", true)) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) exportVm.importData(uri)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Text("Manage appearance, reminders, and data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        ExpandableSection("Appearance", subtitle = "Theme, palette, font size", initiallyExpanded = true) {
            Text("Theme mode", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("sys" to "System", "light" to "Light", "dark" to "Dark").forEach { (v, l) ->
                    FilterChip(selected = curMode == v, onClick = { vm.updateTheme(v) }, label = { Text(l) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Palette", style = MaterialTheme.typography.labelLarge)
            val palettes = listOf("default", "ocean", "sunset", "forest", "rose", "midnight", "amoled", "amoled_cyan", "amoled_red", "amoled_lime")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                palettes.take(5).forEach { p -> FilterChip(selected = curAccent == p, onClick = { vm.updateAccent(p) }, label = { Text(p.replaceFirstChar { it.uppercase() }) }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("green", "teal", "blue", "orange", "auto").forEach { a ->
                    FilterChip(selected = curAccent == a, onClick = { vm.updateAccent(a) }, label = { Text(a) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Font size", style = MaterialTheme.typography.labelLarge)
            val font by com.regainplus.ui.theme.ThemePrefs.fontScale.collectAsState()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Small" to 0.85f, "Normal" to 1.0f, "Large" to 1.15f, "Huge" to 1.3f).forEach { (l, v) ->
                    FilterChip(selected = font == v, onClick = { vm.updateFontScale(v) }, label = { Text(l) })
                }
            }
        }

        ExpandableSection("Notifications", subtitle = "Reminders and quiet hours") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Daily reminder")
                    Text("Keep-your-streak notification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notifDaily, onCheckedChange = {
                    notifDaily = it
                    prefs.edit().putBoolean("notif_daily", it).apply()
                    RegainPlusApp.scheduleDailyReminder(ctx, it)
                })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Rest-day reminder")
                    Text("Recovery day notification", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = notifRest, onCheckedChange = {
                    notifRest = it
                    prefs.edit().putBoolean("notif_rest", it).apply()
                    RegainPlusApp.scheduleRestDayReminder(ctx, it)
                })
            }
            Spacer(Modifier.height(8.dp))
            Text("Quiet hours (no reminders)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = prefs.getInt("quiet_start", 22).toString(), onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) prefs.edit().putInt("quiet_start", it).apply() } }, label = { Text("From") }, modifier = Modifier.width(70.dp), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                OutlinedTextField(value = prefs.getInt("quiet_end", 7).toString(), onValueChange = { v -> v.toIntOrNull()?.let { if (it in 0..23) prefs.edit().putInt("quiet_end", it).apply() } }, label = { Text("To") }, modifier = Modifier.width(70.dp), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }
        }

        ExpandableSection("Home screen widget", subtitle = "Choose what appears on the widget") {
            val legacy = prefs.getString("widget_stat", "streak") ?: "streak"
            var wStreak by remember { mutableStateOf(prefs.getBoolean("widget_show_streak", legacy == "streak" || !prefs.contains("widget_stat"))) }
            var wWater by remember { mutableStateOf(prefs.getBoolean("widget_show_water", legacy == "water")) }
            var wFocus by remember { mutableStateOf(prefs.getBoolean("widget_show_focus", legacy == "focus")) }
            var wMood by remember { mutableStateOf(prefs.getBoolean("widget_show_mood", legacy == "mood")) }
            fun refreshWidget() { scope.launch { try { StreakWidget().updateAll(ctx) } catch (_: Exception) { } } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = wStreak, onClick = { wStreak = !wStreak; prefs.edit().putBoolean("widget_show_streak", wStreak).commit(); refreshWidget() }, label = { Text("Streak") })
                FilterChip(selected = wWater, onClick = { wWater = !wWater; prefs.edit().putBoolean("widget_show_water", wWater).commit(); refreshWidget() }, label = { Text("Water") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = wFocus, onClick = { wFocus = !wFocus; prefs.edit().putBoolean("widget_show_focus", wFocus).commit(); refreshWidget() }, label = { Text("Focus") })
                FilterChip(selected = wMood, onClick = { wMood = !wMood; prefs.edit().putBoolean("widget_show_mood", wMood).commit(); refreshWidget() }, label = { Text("Mood") })
            }
            Spacer(Modifier.height(6.dp))
            var showHeader by remember { mutableStateOf(prefs.getBoolean("widget_show_header", true)) }
            var showProgress by remember { mutableStateOf(prefs.getBoolean("widget_show_sub", true)) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show app header", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = showHeader, onCheckedChange = { showHeader = it; prefs.edit().putBoolean("widget_show_header", it).commit(); refreshWidget() })
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show progress bar", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = showProgress, onCheckedChange = { showProgress = it; prefs.edit().putBoolean("widget_show_sub", it).commit(); refreshWidget() })
            }
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { refreshWidget() }) { Text("Refresh widget now") }
        }

        ExpandableSection("Security & privacy", subtitle = if (isAdmin) "Protected" else "Protection and privacy controls") {
            Text(
                if (isAdmin) "Active — RegainPlus is protected from uninstall" else "Opt-in: device admin makes uninstalling harder during streaks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isAdmin) "Protected" else "Not protected", modifier = Modifier.weight(1f))
                Switch(checked = isAdmin, onCheckedChange = { want ->
                    if (want) {
                        adminLauncher.launch(
                            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Protects RegainPlus from being uninstalled while you keep your streak.")
                        )
                    } else {
                        ctx.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                })
            }
            val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current
            DisposableEffect(lifecycle) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, e ->
                    if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) isAdmin = dpm.isAdminActive(adminComponent)
                }
                lifecycle.lifecycle.addObserver(observer)
                onDispose { lifecycle.lifecycle.removeObserver(observer) }
            }
            Spacer(Modifier.height(8.dp))
            val rooted = remember { com.regainplus.util.SecurityUtil.isRooted() }
            Text(if (rooted) "⚠ Device appears rooted — data at risk" else "✓ No root detected", style = MaterialTheme.typography.bodyMedium, color = if (rooted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Text("Backups and cloud transfer disabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            var secure by remember { mutableStateOf(prefs.getBoolean("secure_screen", false)) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Block screenshots & screen recording", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = secure, onCheckedChange = { secure = it; prefs.edit().putBoolean("secure_screen", it).apply() })
            }
            val view = androidx.compose.ui.platform.LocalView.current
            LaunchedEffect(secure) {
                if (!view.isInEditMode) {
                    val w = (view.context as android.app.Activity).window
                    if (secure) w.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                    else w.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
        }

        ExpandableSection("Data", subtitle = "Export or import your data") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { exportVm.export() }) { Text("Export (JSON)") }
                OutlinedButton(onClick = { picker.launch(arrayOf("application/json")) }) { Text("Import") }
            }
            if (exportVm.status.isNotEmpty()) Text(exportVm.status, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
    }
}
