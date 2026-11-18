package com.regainplus.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var a11y by remember { mutableStateOf(isA11yEnabled(ctx)) }
    var usage by remember { mutableStateOf(hasUsageAccess(ctx)) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome to RegainPlus", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Grant these permissions to unlock blocking and screen-time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        PermissionRow("Accessibility (app blocking)", a11y) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionRow("Usage access (screen time)", usage) {
            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionRow("Battery (blocking stays alive)", batteryOk(ctx)) {
            try { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(android.net.Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            catch (_: Exception) { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }
        PermissionRow("Display over other apps (block screen)", android.provider.Settings.canDrawOverlays(ctx)) {
            ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            prefs.edit().putBoolean("onboarded", true).apply()
            onDone()
        }, modifier = Modifier.fillMaxWidth()) { Text("Get started") }
        TextButton(onClick = { prefs.edit().putBoolean("onboarded", true).apply(); onDone() }, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now")
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            a11y = isA11yEnabled(ctx)
            usage = hasUsageAccess(ctx)
            if (a11y && usage && batteryOk(ctx)) break
        }
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onOpen: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(if (granted) "Granted" else "Not granted", style = MaterialTheme.typography.bodySmall, color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            if (!granted) OutlinedButton(onClick = onOpen) { Text("Grant") }
        }
    }
}

private fun isA11yEnabled(ctx: Context): Boolean {
    val enabled = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
    return enabled.contains(ctx.packageName)
}

private fun hasUsageAccess(ctx: Context): Boolean {
    val appOps = ctx.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), ctx.packageName)
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

private fun batteryOk(ctx: Context): Boolean {
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
    return pm.isIgnoringBatteryOptimizations(ctx.packageName)
}
