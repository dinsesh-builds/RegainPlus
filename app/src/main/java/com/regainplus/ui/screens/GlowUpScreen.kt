package com.regainplus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.regainplus.ui.components.ExpandableSection

data class Tip(val category: String, val title: String, val body: String, val tier: String)

val STARTER_TIPS = listOf(
    Tip("Posture", "Posterior chain walk", "Feel glutes fire each step. Feet straight/slightly inward, narrow track, outward knee bow, counter-rotate shoulders/pelvis.", "strong"),
    Tip("Posture", "Wall angels", "Back to wall, raise arms without lifting fingers/head/back/butt. Retrains hunching.", "strong"),
    Tip("Grooming", "90-second grooming check", "Haircut every 3-5 weeks, tidy beard or clean shave, brows, nose hair, nails weekly.", "strong"),
    Tip("Skin", "SPF daily", "Cleanse, moisturize, SPF 30+. Sunscreen is the #1 anti-aging step.", "strong"),
    Tip("Style", "Fit over brand", "Check shoulders, sleeve and trouser length. Fit beats label.", "strong"),
    Tip("Sleep", "Fixed window", "7-9h at same time. Phone away 30 min before bed.", "strong"),
    Tip("Speech", "Slow 10-20%", "Speak slower than default. 0.5-1s pause at commas. Downward inflection at ends.", "strong"),
    Tip("Eyes", "4-6s eye contact", "Hold 4-6s, then clean exit (side/down). Not a stare.", "strong"),
    Tip("Confidence", "Second question", "Ask a second question about their answer. Listen to understand.", "strong"),
    Tip("Color", "Find your season", "Take the seasonal color quiz (Spring/Summer/Autumn/Winter x3). Wear your palette.", "moderate"),
    Tip("Body", "Strength 2-4x/wk", "Pull-ups, rows, deadlifts for shoulders/back/posture. Abs are secondary.", "strong"),
    Tip("Steps", "6k-10k daily", "Daily steps improve energy, composition, mood.", "strong"),
    Tip("Manipulation", "Slow the pace", "If a new partner rushes commitment, ask to slow down. Respect for that = healthy.", "strong"),
    Tip("Manipulation", "Grey rock", "Stay neutral to reduce manipulation. Document interactions.", "moderate"),
    Tip("Mind", "Minimum viable day", "5 min counts. Never reset to zero. Protect identity.", "strong"),
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GlowUpScreen(navController: androidx.navigation.NavController? = null) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val tips = remember {
        try {
            val json = ctx.assets.open("tips.json").bufferedReader().use { it.readText() }
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Tip(o.getString("category"), o.getString("title"), o.getString("body"), o.getString("tier"))
            }
        } catch (e: Exception) { STARTER_TIPS }
    }
    var selected by remember { mutableStateOf<String?>(null) }
    val cats = tips.map { it.category }.distinct()

    // Mission ladder — tap to mark done for today (persisted per date)
    val prefs = remember { ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    val ladder = listOf("Discipline", "Gym", "Glow-up", "Monk", "Apex")
    var doneLadder by remember {
        mutableStateOf(prefs.getStringSet("ladder_$today", emptySet()) ?: emptySet())
    }
    var confettiPlayed by remember { mutableStateOf(prefs.getBoolean("confetti_$today", false)) }
    if (doneLadder.size == 5 && !confettiPlayed) {
        com.regainplus.ui.components.ConfettiOverlay(visible = true)
        LaunchedEffect(doneLadder) {
            kotlinx.coroutines.delay(3000)
            confettiPlayed = true
            prefs.edit().putBoolean("confetti_$today", true).apply()
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Glow Up", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(2.dp))
        Text("Evidence-tiered daily tips", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))

        // Mission ladder (Regain mission-style)
        ExpandableSection("Mission ladder", subtitle = "${doneLadder.size}/5 done today", initiallyExpanded = true) {
                ladder.forEachIndexed { i, name ->
                    val done = doneLadder.contains(name)
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable {
                                doneLadder = (if (done) doneLadder - name else doneLadder + name).toSet()
                                prefs.edit().putStringSet("ladder_$today", doneLadder).apply()
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                if (done) Text("✓", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                when (i) {
                                    0 -> "Cold shower, no phone 1st hour"
                                    1 -> "Workout or 6k+ steps"
                                    2 -> "Grooming + style check"
                                    3 -> "No junk content, calm evening"
                                    else -> "All four + hard thing you avoided"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
        }
        Spacer(Modifier.height(8.dp))
        // Commitment ritual (Regain-style tap-and-hold)
        ExpandableSection("My commitment", subtitle = "Set your intention") {
                var commitment by remember { mutableStateOf(prefs.getString("commitment", "") ?: "") }
                OutlinedTextField(value = commitment, onValueChange = { commitment = it; prefs.edit().putString("commitment", it).apply() }, label = { Text("I will use RegainPlus to…") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(6.dp))
                var committed by remember { mutableStateOf(prefs.getBoolean("committed", false)) }
                Text(
                    if (committed) "✓ Committed — tap again to update" else "Tap to commit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (commitment.isNotBlank()) {
                            prefs.edit().putBoolean("committed", true).apply()
                            committed = true
                        }
                    }.padding(8.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        navController?.let { nc -> Button(onClick = { nc.navigate("palette") }) { Text("Color palette quiz") } }
        Spacer(Modifier.height(8.dp))
        ExpandableSection("Tips", subtitle = "Evidence-tiered daily tips", initiallyExpanded = true) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(selected = selected == null, onClick = { selected = null }, label = { Text("All") })
                cats.forEach { c ->
                    FilterChip(selected = selected == c, onClick = { selected = c }, label = { Text(c) })
                }
            }
            Spacer(Modifier.height(8.dp))
        (if (selected == null) tips else tips.filter { it.category == selected }).forEach { t ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${t.category} · ${t.tier}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(t.title, style = MaterialTheme.typography.titleMedium)
                    Text(t.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}