package com.regainplus

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import com.regainplus.ui.theme.RegainPlusTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.rememberNavController
import com.regainplus.ui.navigation.BottomBar
import com.regainplus.ui.navigation.RegainNavHost
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.viewmodel.SettingsViewModel

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