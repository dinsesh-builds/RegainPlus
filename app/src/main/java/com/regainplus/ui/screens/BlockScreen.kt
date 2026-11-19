package com.regainplus.ui.screens

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.service.BlockHolder
import com.regainplus.service.BlockSchedule
import com.regainplus.service.NotificationBlockHolder
import com.regainplus.service.ScheduleHolder
import com.regainplus.service.ShortFormHolder
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.BlockViewModel

@Composable
fun BlockScreen(vm: BlockViewModel = hiltViewModel()) {
    val blockedItems by vm.items.collectAsState()
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var name by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf("app") }
    LaunchedEffect(Unit) { ShortFormHolder.init(ctx); ScheduleHolder.init(ctx) }
    val shortsEnabled by ShortFormHolder.enabled.collectAsState()
    val schedules by ScheduleHolder.schedules.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    val pm = ctx.packageManager
    val launchers = remember {
        pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo }
            .distinctBy { it.packageName }
            .sortedBy { pm.getApplicationLabel(it.applicationInfo).toString() }
    }
    var schedPkg by remember { mutableStateOf("") }
    var schedStart by remember { mutableStateOf("09:00") }
    var schedEnd by remember { mutableStateOf("11:00") }
    var schedDays by remember { mutableStateOf("1111100") }

    // P0: notification blocking + site/adult + battery
    val notifEnabled by NotificationBlockHolder.enabled.collectAsState()
    val notifAllowed by NotificationBlockHolder.allowed.collectAsState()
    val notifInbox by NotificationBlockHolder.inbox.collectAsState()
    var adultOn by remember { mutableStateOf(prefs.getBoolean("adult_block", false)) }
    var listenerOn by remember { mutableStateOf(isListenerEnabled(ctx)) }
    val pmPower = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
    var batteryOn by remember { mutableStateOf(pmPower.isIgnoringBatteryOptimizations(ctx.packageName)) }
    LaunchedEffect(Unit) { NotificationBlockHolder.init(ctx) }
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                listenerOn = isListenerEnabled(ctx)
                batteryOn = pmPower.isIgnoringBatteryOptimizations(ctx.packageName)
                adultOn = prefs.getBoolean("adult_block", false)
                NotificationBlockHolder.init(ctx)
            }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Text("Block", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // Short-form blocker
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Short-form blocker", style = MaterialTheme.typography.titleMedium)
                    Text("Block YouTube Shorts, Reels, TikTok", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = shortsEnabled, onCheckedChange = {
                    ShortFormHolder.set(ctx, it)
                    com.regainplus.RegainPlusApp.syncWatchdog(ctx)
                })
            }
        }
        Spacer(Modifier.height(8.dp))

        // Block apps — one tap, no package names
        Spacer(Modifier.height(4.dp))
        Text("Block apps", style = MaterialTheme.typography.titleMedium)
        Text("Tap any app on your phone to block or unblock it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val blockedPkgs = blockedItems.filter { it.kind == "app" }.map { it.name }.toSet()
        var appSearch by remember { mutableStateOf("") }
        OutlinedTextField(value = appSearch, onValueChange = { appSearch = it }, label = { Text("Search apps") }, modifier = Modifier.fillMaxWidth(), singleLine = true, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })
        val visibleApps = launchers.filter { info ->
            val label = pm.getApplicationLabel(info.applicationInfo).toString()
            label.contains(appSearch, ignoreCase = true) || info.packageName.contains(appSearch, ignoreCase = true)
        }
        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
            if (visibleApps.isEmpty()) {
                Text("No apps match \"$appSearch\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
            } else {
                androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(visibleApps, key = { it.packageName }) { info ->
                        val pkg = info.packageName
                        val label = pm.getApplicationLabel(info.applicationInfo).toString()
                        val icon = remember(pkg) {
                            try { pm.getApplicationIcon(pkg).toBitmap().asImageBitmap() } catch (_: Exception) { null }
                        }
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                if (pkg in blockedPkgs) blockedItems.firstOrNull { it.kind != "site" && it.name == pkg }?.let { vm.delete(it.id) }
                                else vm.add("app", pkg)
                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (icon != null) Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                Text(pkg, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Checkbox(checked = pkg in blockedPkgs, onCheckedChange = null)
                        }
                    }
                }
            }
        }
        if (blockedPkgs.isNotEmpty()) {
            Text("Blocked apps: ${blockedPkgs.joinToString { appLabel(ctx, it, launchers) }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            TextButton(onClick = { vm.clearAll() }) { Text("Clear all blocked apps") }
        }

        // Websites (optional, advanced)
        Spacer(Modifier.height(12.dp))
        Text("Block websites (optional)", style = MaterialTheme.typography.titleSmall)
        Text("Site entries are matched against browser URLs via accessibility. Use plain domains (e.g. example.com).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Website domain") }, placeholder = { Text("example.com") }, modifier = Modifier.weight(1f).fillMaxHeight(), singleLine = true)
            Button(onClick = { if (name.isNotBlank()) { vm.add("site", name); name = "" } }, modifier = Modifier.fillMaxHeight()) { Text("Add site") }
        }
        blockedItems.filter { it.kind == "site" }.forEach { b ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(b.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                ConfirmDeleteButton(onConfirm = { vm.delete(b.id) })
            }
        }

        // P0: Notification blocking
        Spacer(Modifier.height(16.dp))
        Text("Notification blocking", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Block notifications from blocked apps")
                        Text(if (listenerOn) "Notification access granted" else "Notification access needed", style = MaterialTheme.typography.bodySmall, color = if (listenerOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    }
                    Switch(checked = notifEnabled, onCheckedChange = { NotificationBlockHolder.setEnabled(ctx, it) })
                }
                if (!listenerOn) {
                    OutlinedButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }, modifier = Modifier.padding(top = 6.dp)) {
                        Text("Grant notification access")
                    }
                }
                val blockAll by NotificationBlockHolder.blockAll.collectAsState()
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Block all except allowed apps", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = blockAll, onCheckedChange = { NotificationBlockHolder.setBlockAll(ctx, it) })
                }
                if (notifEnabled && listenerOn) {
                    Spacer(Modifier.height(6.dp))
                    Text("Instant (allowed): ${notifAllowed.joinToString { it.substringAfterLast('.') }}", style = MaterialTheme.typography.bodySmall)
                    Text("Blocked inbox: ${notifInbox.size} notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    notifInbox.take(5).forEach { n ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${n.pkg.substringAfterLast('.')}: ${n.title}", style = MaterialTheme.typography.bodySmall)
                                Text(n.text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            TextButton(onClick = { NotificationBlockHolder.setAllowed(ctx, notifAllowed + n.pkg) }) { Text("Allow") }
                            TextButton(onClick = { NotificationBlockHolder.setAllowed(ctx, notifAllowed - n.pkg) }) { Text("Block") }
                        }
                    }
                    if (notifInbox.isNotEmpty()) TextButton(onClick = { NotificationBlockHolder.clearInbox(ctx) }) { Text("Clear inbox") }
                }
            }
        }

        // Adult content filter — one working control (BlockerHero-style URL detection).
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Block adult content")
                        Text(
                            if (adultOn) "On — adult sites blocked in browsers" else "Off",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (adultOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = adultOn, onCheckedChange = { on ->
                        adultOn = on
                        prefs.edit().putBoolean("adult_block", on).apply()
                        com.regainplus.RegainPlusApp.syncWatchdog(ctx)
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Display over other apps"); Text(if (android.provider.Settings.canDrawOverlays(ctx)) "Granted — block screen works" else "REQUIRED for blocking", style = MaterialTheme.typography.bodySmall, color = if (android.provider.Settings.canDrawOverlays(ctx)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                    if (!android.provider.Settings.canDrawOverlays(ctx)) OutlinedButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) { Text("Grant") }
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Floating bubble"); Text("Quick toggle overlay", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    var bubbleOn by remember { mutableStateOf(prefs.getBoolean("bubble_on", false)) }
                    Switch(checked = bubbleOn, onCheckedChange = { on ->
                        if (on && !android.provider.Settings.canDrawOverlays(ctx)) {
                            ctx.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } else {
                            bubbleOn = on
                            prefs.edit().putBoolean("bubble_on", on).apply()
                            if (on) ctx.startService(Intent(ctx, com.regainplus.service.FloatingBubbleService::class.java))
                            else ctx.stopService(Intent(ctx, com.regainplus.service.FloatingBubbleService::class.java))
                        }
                    })
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("Battery optimization"); Text(if (batteryOn) "Exempt — blocking stays alive" else "Needed for MIUI reliability", style = MaterialTheme.typography.bodySmall, color = if (batteryOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
                    if (!batteryOn) OutlinedButton(onClick = {
                        try { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        catch (_: Exception) { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                    }) { Text("Exempt") }
                }
            }
        }

        // Schedule-based blocking
        Spacer(Modifier.height(16.dp))
        Text("Schedule-based blocking", style = MaterialTheme.typography.titleMedium)
        Text("Block an app only during a time window on selected days.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = schedPkg, onValueChange = { schedPkg = it }, label = { Text("Package") }, modifier = Modifier.weight(1f).fillMaxHeight(), singleLine = true)
            OutlinedButton(onClick = { showPicker = true }, modifier = Modifier.fillMaxHeight()) { Text("Pick") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = schedStart,
                onValueChange = { schedStart = it },
                label = { Text("Start") },
                placeholder = { Text("HH:mm") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = schedEnd,
                onValueChange = { schedEnd = it },
                label = { Text("End") },
                placeholder = { Text("HH:mm") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            dayLabels.forEachIndexed { i, lbl ->
                FilterChip(selected = schedDays.getOrElse(i) { '0' } == '1', onClick = {
                    val arr = schedDays.toCharArray()
                    if (i < arr.size) arr[i] = if (arr[i] == '1') '0' else '1'
                    schedDays = String(arr)
                }, label = { Text(lbl) })
            }
        }
        Button(onClick = {
            if (schedPkg.isNotBlank()) {
                fun toMin(s: String): Int {
                    val parts = s.split(":")
                    return (parts.getOrElse(0) { "0" }.toIntOrNull() ?: 0) * 60 + (parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0)
                }
                val new = BlockSchedule(schedPkg, toMin(schedStart), toMin(schedEnd), schedDays)
                ScheduleHolder.set(schedules + new, ctx)
                com.regainplus.RegainPlusApp.syncWatchdog(ctx)
                schedPkg = ""
            }
        }, modifier = Modifier.padding(top = 6.dp)) { Text("Add schedule") }
        Spacer(Modifier.height(6.dp))
        schedules.forEach { sc ->
            Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${sc.pkg}  ${sc.startMin / 60}:${"%02d".format(sc.startMin % 60)}-${sc.endMin / 60}:${"%02d".format(sc.endMin % 60)}  ${sc.days}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        ScheduleHolder.set(schedules - sc, ctx)
                        com.regainplus.RegainPlusApp.syncWatchdog(ctx)
                    }) { Text("Del") }
                }
            }
        }

        if (showPicker) {
            AlertDialog(onDismissRequest = { showPicker = false }, title = { Text("Pick an app") }, text = {
                LazyColumn(Modifier.height(400.dp)) {
                    items(launchers.size) { i ->
                        val info = launchers[i]
                        val label = pm.getApplicationLabel(info.applicationInfo).toString()
                        TextButton(onClick = { name = info.packageName; schedPkg = info.packageName; showPicker = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(label)
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } })
        }
    }
}

private fun appLabel(ctx: Context, pkg: String, launchers: List<android.content.pm.ActivityInfo>): String {
    launchers.firstOrNull { it.packageName == pkg }?.let {
        val l = ctx.packageManager.getApplicationLabel(it.applicationInfo).toString()
        if (l.isNotBlank() && l != pkg) return l
    }
    return when {
        pkg.contains("chrome") -> "Chrome"
        pkg.contains("youtube") -> "YouTube"
        pkg.contains("instagram") -> "Instagram"
        pkg.contains("facebook") -> "Facebook"
        pkg.contains("whatsapp") -> "WhatsApp"
        pkg.contains("tiktok") || pkg.contains("musically") -> "TikTok"
        pkg.contains("telegram") -> "Telegram"
        pkg.contains("reddit") -> "Reddit"
        pkg.contains("snapchat") -> "Snapchat"
        else -> pkg.substringAfterLast('.')
    }
}

private fun isListenerEnabled(ctx: Context): Boolean {
    val enabled = Settings.Secure.getString(ctx.contentResolver, "enabled_notification_listeners") ?: ""
    return enabled.contains(ComponentName(ctx, com.regainplus.service.RegainNotificationListener::class.java).flattenToString())
}
