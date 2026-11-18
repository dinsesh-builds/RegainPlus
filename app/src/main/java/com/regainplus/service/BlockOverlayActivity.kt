package com.regainplus.service

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.regainplus.ui.theme.RegainPlusTheme

class BlockOverlayActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pkg = intent.getStringExtra("pkg") ?: ""
        val isShorts = intent.getBooleanExtra("shorts", false)
        val title = if (isShorts) "Shorts blocked" else "Focus Mode"
        val prompt = if (isShorts)
            "Short-form content hijacks your attention. You opened: $pkg"
        else
            "Take a breath. This app is blocked during your focus."
        setContent {
            RegainPlusTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(title, style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("You blocked: $pkg", style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))
                        Text(prompt, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        // Real enforcement: leave the blocked app, go Home.
                        Button(onClick = { goHomeAndClose() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Go home (recommended)")
                        }
                    }
                }
            }
        }
    }

    /** BUG-02 fix: Back cannot dismiss into the blocked app; always return Home. */
    override fun onBackPressed() { goHomeAndClose() }

    private fun goHomeAndClose() {
        val home = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME); flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        try { startActivity(home) } catch (_: Exception) {}
        finish()
    }
}
