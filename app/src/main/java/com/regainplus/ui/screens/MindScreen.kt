package com.regainplus.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.ExpandableSection
import com.regainplus.ui.viewmodel.MindViewModel
import kotlinx.coroutines.delay

@Composable
fun MindScreen(vm: MindViewModel = hiltViewModel()) {
    val logs by vm.logs.collectAsState()
    var mood by remember { mutableStateOf(3) }
    var note by remember { mutableStateOf("") }
    var breathing by remember { mutableStateOf(false) }
    var phase by remember { mutableIntStateOf(0) } // 0 inhale, 1 hold, 2 exhale
    var countdown by remember { mutableIntStateOf(0) }

    // Breathing 4-7-8 driver with per-second countdown
    LaunchedEffect(breathing) {
        if (!breathing) {
            countdown = 0
            return@LaunchedEffect
        }
        while (breathing) {
            phase = 0
            for (s in 4 downTo 1) { countdown = s; delay(1000) }
            if (!breathing) break
            phase = 1
            for (s in 7 downTo 1) { countdown = s; delay(1000) }
            if (!breathing) break
            phase = 2
            for (s in 8 downTo 1) { countdown = s; delay(1000) }
        }
        countdown = 0
    }

    val scale by animateFloatAsState(
        targetValue = when (phase) { 0 -> 1.35f; 1 -> 1.35f; else -> 0.8f },
        animationSpec = when (phase) {
            0 -> tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            1 -> tween(durationMillis = 300, easing = LinearEasing)
            else -> tween(durationMillis = 8000, easing = FastOutSlowInEasing)
        },
        label = "breath"
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Mind", style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        ExpandableSection("Mood check-in", subtitle = "Log how you feel", initiallyExpanded = true) {
            Slider(value = mood.toFloat(), onValueChange = { mood = it.toInt() }, valueRange = 1f..5f, steps = 3)
            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { vm.log(mood, note); note = "" }, modifier = Modifier.padding(top = 8.dp)) { Text("Log") }
            logs.take(5).forEach { m ->
                Text(
                    "Mood ${m.mood} · ${java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(m.date))}${if (m.note.isNotBlank()) " — ${m.note}" else ""}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (logs.isEmpty()) Text("No mood logs yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))

        ExpandableSection("Breathing 4-7-8", subtitle = "A calm reset when you feel tense") {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(120.dp).scale(scale).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape))
                    Box(Modifier.size((120 * scale).dp.coerceAtMost(170.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(when (phase) { 0 -> "Inhale"; 1 -> "Hold"; else -> "Exhale" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (breathing) Text("$countdown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        else Text("4 · 7 · 8", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { breathing = !breathing; if (!breathing) { phase = 2; countdown = 0 } }) { Text(if (breathing) "Stop" else "Start breathing") }
            }
        }
        Spacer(Modifier.height(10.dp))

        ExpandableSection("5-4-3-2-1 grounding", subtitle = "Come back to the present moment") {
            listOf("5 things you can see", "4 things you can feel", "3 things you can hear", "2 things you can smell", "1 thing you can taste").forEach { step ->
                Text("• $step", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
        Spacer(Modifier.height(10.dp))

        ExpandableSection("Manipulation defense", subtitle = "Red flags and scripts") {
            listOf(
                "Love bombing: idealize → devalue → discard",
                "Gaslighting: you doubt your own memory",
                "Intermittent reward: hot-cold creates dependency",
                "Coercive control: isolation + monitoring"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(8.dp))
            Text("Scripts", style = MaterialTheme.typography.titleSmall)
            listOf("\"I'm not comfortable with that.\"", "\"Let's slow down.\"", "\"I need time to think.\"").forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(4.dp))
            Text("Grey rock: stay neutral, don't JADE (justify/argue/defend/explain).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))

        ExpandableSection("Sleep hygiene", subtitle = "Simple habits for better sleep") {
            listOf(
                "Same sleep/wake time daily, even weekends",
                "No screens 30-60 min before bed",
                "Cool, dark, quiet room",
                "Caffeine cutoff 8h before sleep",
                "Bed = sleep only (no scrolling)"
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }

        Spacer(Modifier.height(12.dp))
    }
}
