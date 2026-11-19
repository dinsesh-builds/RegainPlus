package com.regainplus.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var a11y by remember { mutableStateOf(isA11yEnabled(ctx)) }
    var usage by remember { mutableStateOf(hasUsageAccess(ctx)) }
    var overlay by remember { mutableStateOf(android.provider.Settings.canDrawOverlays(ctx)) }
    var battery by remember { mutableStateOf(batteryOk(ctx)) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        Text("RegainPlus", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "A private, offline space for focus, study, habits, and balance. Turn on only what you need — you can change everything later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        Text("Optional setup", style = MaterialTheme.typography.titleMedium)
        Text(
            "Some features need extra permissions to work on your device. Nothing is required to start.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        PermissionRow(
            title = "App blocking",
            reason = "Lets RegainPlus notice distracting apps and show a block screen. Needed for blocking, study mode, and short-form limits.",
            granted = a11y,
            actionLabel = "Enable accessibility"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionRow(
            title = "Screen time insights",
            reason = "Shows how long apps are used so you can track habits and focus time.",
            granted = usage,
            actionLabel = "Allow usage access"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionRow(
            title = "Show block screen reliably",
            reason = "Allows the block screen to appear over other apps when a limit is reached.",
            granted = overlay,
            actionLabel = "Allow display over apps"
        ) {
            ctx.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionRow(
            title = "Keep blocking active",
            reason = "Reduces the chance your phone stops blocking/timers in the background. Helpful on MIUI and similar devices.",
            granted = battery,
            actionLabel = "Allow battery exception"
        ) {
            try { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(android.net.Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            catch (_: Exception) { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            prefs.edit().putBoolean("onboarded", true).apply()
            onDone()
        }, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) { Text("Continue") }
        TextButton(onClick = { prefs.edit().putBoolean("onboarded", true).apply(); onDone() }, modifier = Modifier.fillMaxWidth()) {
            Text("Skip for now")
        }
        Spacer(Modifier.height(16.dp))
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            a11y = isA11yEnabled(ctx)
            usage = hasUsageAccess(ctx)
            overlay = android.provider.Settings.canDrawOverlays(ctx)
            battery = batteryOk(ctx)
        }
    }
}

@Composable
private fun PermissionRow(title: String, reason: String, granted: Boolean, actionLabel: String, onOpen: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    if (granted) "Ready" else "Optional",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!granted) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onOpen, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) { Text(actionLabel) }
            }
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
