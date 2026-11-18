package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.AreaChart
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.components.BarChart
import com.regainplus.ui.viewmodel.GymViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GymScreen(vm: GymViewModel = hiltViewModel()) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val logs by vm.logs.collectAsState()
    var ex by remember { mutableStateOf("") }
    var restTimer by remember { mutableStateOf(0) }
    var sessionSets by remember { mutableStateOf(listOf(Triple(0, 0, false))) }
    var daysAgo by remember { mutableIntStateOf(0) }
    val gymPrefs = remember { ctx.getSharedPreferences("gym_goals", android.content.Context.MODE_PRIVATE) }
    var weekGoal by remember { mutableIntStateOf(gymPrefs.getInt("week_goal", 3)) }

    LaunchedEffect(restTimer) {
        if (restTimer > 0) { kotlinx.coroutines.delay(1000); restTimer-- }
    }

    val grouped = logs.groupBy { it.exercise }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text("Workout", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Previous bests load automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            // Gym goals + obstacle reflection (Regain-style commitment)
            val goalPrefs = remember { ctx.getSharedPreferences("gym_goals", android.content.Context.MODE_PRIVATE) }
            var gymGoal by remember { mutableStateOf(goalPrefs.getString("goal", "") ?: "") }
            var blocker by remember { mutableStateOf(goalPrefs.getString("blocker", "") ?: "") }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Your gym goal", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = gymGoal, onValueChange = { gymGoal = it; goalPrefs.edit().putString("goal", it).apply() }, label = { Text("e.g. Bench 60kg × 8") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sessions/week goal: $weekGoal", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(value = weekGoal.toString(), onValueChange = { v -> v.toIntOrNull()?.let { if (it > 0) { weekGoal = it; gymPrefs.edit().putInt("week_goal", it).apply() } } }, label = { Text("Goal") }, modifier = Modifier.width(70.dp), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    }
                    val weekSessions = logs.filter { it.date > System.currentTimeMillis() - 7 * 86400000L }.size
                    Text("This week: $weekSessions / $weekGoal sessions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("What's stopping you?", style = MaterialTheme.typography.titleSmall)
                    val blockers = listOf("Time", "Motivation", "Gym access", "Injury", "Energy")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        blockers.forEach { b ->
                            FilterChip(selected = blocker == b, onClick = { blocker = b; goalPrefs.edit().putString("blocker", b).apply() }, label = { Text(b) })
                        }
                    }
                    if (blocker.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Plan around it: ${when (blocker) { "Time" -> "shorter 20-min sessions"; "Motivation" -> "schedule a workout partner"; "Gym access" -> "bodyweight fallback"; "Injury" -> "mobility work"; else -> "sleep + hydration first" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Log for", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Today", "Yesterday", "2 days").forEachIndexed { i, lbl ->
                            FilterChip(selected = daysAgo == i, onClick = { daysAgo = i }, label = { Text(lbl) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ex, onValueChange = { ex = it },
                        label = { Text("Exercise") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    // Table header (Symmetry-style)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Set", modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(6.dp))
                        Text("Previous", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Start)
                        Text("KG", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Text("REPS", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.width(40.dp))
                    }
                    sessionSets.forEachIndexed { i, s ->
                        SetRow(
                            index = i,
                            prev = previousBest(grouped[ex], i),
                            kg = s.first, reps = s.second, checked = s.third,
                            onKg = { v -> sessionSets = sessionSets.toMutableList().also { it[i] = Triple(v, s.second, s.third) } },
                            onReps = { v -> sessionSets = sessionSets.toMutableList().also { it[i] = Triple(s.first, v, s.third) } },
                            onCheck = {
                                if (!s.third) {
                                    vm.addAt(ex.ifBlank { "Exercise" }, 1, s.second, s.first.toDouble(), System.currentTimeMillis() - daysAgo * 86400000L)
                                    sessionSets = sessionSets.toMutableList().also { it[i] = Triple(s.first, s.second, true) }
                                    restTimer = 90
                                    if (i == sessionSets.lastIndex) sessionSets = sessionSets + Triple(s.first, s.second, false)
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { sessionSets = sessionSets + Triple(0, 0, false) },
                            modifier = Modifier.weight(1f)
                        ) { Text("Add set") }
                        OutlinedButton(onClick = { restTimer = 90 }, modifier = Modifier.weight(1f)) {
                            Text(if (restTimer > 0) "Rest ${restTimer}s" else "Rest timer")
                        }
                    }
                }
            }
        }

        // Volume area chart (Symmetry-style analytics with % badge)
        item {
            val volumeByDate = logs
                .groupBy { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(it.date)) }
                .map { (_, ls) -> ls.sumOf { it.sets * it.reps }.toFloat() }
            if (volumeByDate.size >= 2) {
                val first = volumeByDate.first().coerceAtLeast(1f)
                val last = volumeByDate.last()
                val pct = (last - first) / first * 100f
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Volume trend", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        AreaChart(values = volumeByDate.takeLast(14), changePercent = pct, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        // Volume by exercise (bar chart)
        item {
            val volumeByEx = grouped.map { (e, ls) -> e to ls.sumOf { it.sets * it.reps }.toFloat() }
            if (volumeByEx.isNotEmpty()) {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Volume by exercise", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        BarChart(data = volumeByEx, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Empty state
        if (logs.isEmpty()) {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No workouts logged yet", style = MaterialTheme.typography.titleMedium)
                        Text("Add your first set above to start tracking PRs.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        grouped.forEach { (exercise, setsList) ->
            item {
                Column {
                    Row(Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        val hue = (exercise.hashCode().let { if (it < 0) -it else it } % 360)
                        Surface(shape = CircleShape, color = Color.hsv(hue.toFloat(), 0.5f, 0.85f)) {
                            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                Text(exercise.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(exercise, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    setsList.take(6).forEach { g ->
                        Card(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("${g.sets}×${g.reps}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                                Text("@ ${g.weight}kg", modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(SimpleDateFormat("MMM d", Locale.US).format(Date(g.date)), modifier = Modifier.weight(1.4f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                ConfirmDeleteButton(onConfirm = { vm.delete(g.id) })
                            }
                        }
                    }
                    val best = setsList.maxByOrNull { it.weight }
                    if (best != null) {
                        Text(
                            "PR: ${best.weight}kg × ${best.reps}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

private fun previousBest(sets: List<com.regainplus.data.entity.GymLog>?, index: Int): String {
    val last = (sets ?: emptyList()).maxByOrNull { it.date } ?: return "—"
    return "${last.weight.toInt()}kg × ${last.reps}"
}

@Composable
private fun SetRow(
    index: Int, prev: String, kg: Int, reps: Int, checked: Boolean,
    onKg: (Int) -> Unit, onReps: (Int) -> Unit, onCheck: () -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(36.dp), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(
            prev,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        OutlinedTextField(
            value = if (kg == 0) "" else kg.toString(), onValueChange = { v -> onKg(v.toIntOrNull() ?: 0) },
            modifier = Modifier.weight(0.9f).padding(horizontal = 2.dp), singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
        )
        OutlinedTextField(
            value = if (reps == 0) "" else reps.toString(), onValueChange = { v -> onReps(v.toIntOrNull() ?: 0) },
            modifier = Modifier.weight(0.9f).padding(horizontal = 2.dp), singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
        )
        Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            FilledIconToggleButton(checked = checked, onCheckedChange = { onCheck() }, modifier = Modifier.size(34.dp)) {
                Text("✓", fontWeight = FontWeight.Bold)
            }
        }
    }
}
