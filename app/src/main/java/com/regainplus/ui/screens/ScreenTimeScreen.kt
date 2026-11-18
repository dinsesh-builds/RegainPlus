package com.regainplus.ui.screens

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun ScreenTimeScreen() {
    val ctx = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }
    var hasAccess by remember { mutableStateOf(hasUsageAccess(ctx)) }
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) { hasAccess = hasUsageAccess(ctx); refreshKey++ }
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    if (!hasAccess) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Usage access needed", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Grant usage access to see screen time and app usage.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }) {
                Text("Open usage access settings")
            }
        }
        return
    }
    val stats = remember(refreshKey) { loadUsage(ctx) }
    val totalMs = stats.sumOf { it.usageMs }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Screen time", style = MaterialTheme.typography.headlineMedium)
                    Text("Today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { refreshKey++ }) { Text("Refresh") }
            }
            Spacer(Modifier.height(8.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(formatDuration(totalMs), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("Total screen time today", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Top apps", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
        }
        items(stats.take(20)) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                val iconBitmap = remember(app.packageName) {
                    try { ctx.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap() } catch (e: Exception) { null }
                }
                if (iconBitmap != null) {
                    Image(bitmap = iconBitmap, contentDescription = app.label, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(app.label, style = MaterialTheme.typography.titleSmall)
                }
                Text(formatDuration(app.usageMs), style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider()
        }
        if (stats.isEmpty()) {
            item { Text("No usage data yet. Use some apps and reopen.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

private data class AppUsage(val packageName: String, val label: String, val usageMs: Long)

private fun hasUsageAccess(ctx: Context): Boolean {
    val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), ctx.packageName)
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun loadUsage(ctx: Context): List<AppUsage> {
    val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val start = cal.timeInMillis
    val end = System.currentTimeMillis()
    val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end) ?: emptyList()
    val pm = ctx.packageManager
    val launcherPackages = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
        .map { it.activityInfo.packageName }.toSet()
    return stats
        .filter { it.totalTimeInForeground > 0 }
        .filter { it.packageName in launcherPackages }
        .map { s ->
            val label = try { pm.getApplicationLabel(pm.getApplicationInfo(s.packageName, 0)).toString() } catch (e: PackageManager.NameNotFoundException) { s.packageName }
            AppUsage(s.packageName, label.ifBlank { s.packageName }, s.totalTimeInForeground)
        }
        .sortedByDescending { it.usageMs }
}

private fun formatDuration(ms: Long): String {
    val totalMin = ms / 60000
    val h = totalMin / 60
    val m = totalMin % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
