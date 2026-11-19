package com.regainplus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.regainplus.ui.navigation.BottomBar
import com.regainplus.ui.navigation.RegainNavHost
import com.regainplus.ui.theme.RegainPlusTheme
import com.regainplus.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RegainPlusRoot() }
    }
}

@Composable
fun RegainPlusRoot() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    val settingsVm = hiltViewModel<SettingsViewModel>()
    val themePref by com.regainplus.ui.theme.ThemePrefs.theme.collectAsState()
    val accentPref by com.regainplus.ui.theme.ThemePrefs.accent.collectAsState()
    val fontScale by com.regainplus.ui.theme.ThemePrefs.fontScale.collectAsState()
    val dark = when (themePref) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val onboardedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(prefs.getBoolean("onboarded", false)) }
    val onboarded = onboardedState.value
    val nav = rememberNavController()
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if ((ctx as? android.app.Activity)?.intent?.getBooleanExtra("openBlock", false) == true) {
            nav.navigate(com.regainplus.ui.navigation.Screen.Block.route)
        }
    }
    val notifPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(onboarded) {
        if (onboarded && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    RegainPlusTheme(darkTheme = dark, accent = accentPref, fontScale = fontScale) {
        if (!onboarded) {
            com.regainplus.ui.screens.OnboardingScreen(onDone = { onboardedState.value = true })
        } else {
            Scaffold(bottomBar = { BottomBar(nav) }) { padding ->
                androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
                    RegainNavHost(nav)
                }
            }
        }
    }
}