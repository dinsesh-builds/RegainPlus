package com.regainplus.ui.screens

import android.content.Context
import androidx.compose.foundation.horizontalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.data.entity.Skill
import com.regainplus.ui.viewmodel.SkillsViewModel

@Composable
fun SkillsScreen(vm: SkillsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val skills by vm.skills.collectAsState()
    LaunchedEffect(Unit) { if (skills.isEmpty()) vm.seed() }

    val active = skills.filter { it.active }
    val parked = skills.filter { !it.active }
    val activeCount = active.size

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Skills", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Focus on a few skills, track practice, and level up steadily.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Active skills", style = MaterialTheme.typography.titleMedium)
                Text("$activeCount / 3 active now", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (activeCount / 3f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                if (active.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Activate up to 3 skills to focus your practice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (active.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Practice now", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            active.forEach { skill ->
                SkillCard(
                    skill = skill,
                    ctx = ctx,
                    vm = vm,
                    canActivateMore = activeCount < 3,
                    emphasized = true
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("All skills", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))

        if (parked.isEmpty() && active.isNotEmpty()) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Text(
                    "No parked skills. You can park one to make room for another.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            parked.forEach { skill ->
                SkillCard(
                    skill = skill,
                    ctx = ctx,
                    vm = vm,
                    canActivateMore = activeCount < 3,
                    emphasized = false
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SkillCard(
    skill: Skill,
    ctx: Context,
    vm: SkillsViewModel,
    canActivateMore: Boolean,
    emphasized: Boolean
) {
    val total by vm.totalMinutes(skill.id).collectAsState(initial = 0)
    val rubricPrefs = remember { ctx.getSharedPreferences("rubrics", Context.MODE_PRIVATE) }
    var rubric by remember(skill.id) { mutableIntStateOf(rubricPrefs.getInt("r_${skill.id}", 0)) }
    var showCustom by remember(skill.id) { mutableStateOf(false) }
    var customMinutes by remember(skill.id) { mutableStateOf("15") }

    val goal = nextGoal(total)
    val progress = if (goal > 0) (total.toFloat() / goal).coerceIn(0f, 1f) else 0f

    Card(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(skill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (skill.active) "Active" else "Parked",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (skill.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = { vm.setActive(skill, !skill.active) },
                    enabled = skill.active || canActivateMore
                ) {
                    Text(if (skill.active) "Park" else "Activate")
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${total} min", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("total practice", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (total >= goal && goal >= 1000) "Milestone reached: $goal+ min" else "Next goal: $goal min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))
            Text("Log practice", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { vm.addSession(skill.id, 10, "") }, label = { Text("+10m") })
                AssistChip(onClick = { vm.addSession(skill.id, 25, "") }, label = { Text("+25m") })
                AssistChip(onClick = { vm.addSession(skill.id, 45, "") }, label = { Text("+45m") })
                AssistChip(onClick = { showCustom = !showCustom }, label = { Text(if (showCustom) "Close" else "Custom") })
            }
            if (showCustom) {
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Minutes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    Button(onClick = {
                        val m = customMinutes.toIntOrNull() ?: 0
                        if (m > 0) {
                            vm.addSession(skill.id, m, "")
                            showCustom = false
                        }
                    }) { Text("Save") }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Self rating", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { n ->
                    FilterChip(
                        selected = rubric == n,
                        onClick = {
                            rubric = n
                            rubricPrefs.edit().putInt("r_${skill.id}", n).apply()
                        },
                        label = { Text(n.toString()) }
                    )
                }
            }
        }
    }
}

private fun nextGoal(total: Int): Int {
    val steps = listOf(30, 60, 120, 300, 600, 1000, 2000)
    return steps.firstOrNull { it > total } ?: ((total / 500 + 1) * 500)
}