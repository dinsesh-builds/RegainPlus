package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@Composable
fun CurriculumScreen() {
    val ctx = LocalContext.current
    val json = remember {
        try { ctx.assets.open("skill_content.json").bufferedReader().use { it.readText() } } catch (e: Exception) { "{}" }
    }
    val obj = remember { JSONObject(json) }
    var selected by remember { mutableStateOf("languages") }
    Column(Modifier.padding(16.dp)) {
        Text("Skill Curriculum", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("languages", "chess", "cooking", "calisthenics").forEach { c ->
                FilterChip(selected = selected == c, onClick = { selected = c }, label = { Text(c) })
            }
        }
        Spacer(Modifier.height(8.dp))
        val arr = obj.optJSONArray(selected)
        if (arr != null) {
            (0 until arr.length()).forEach { i ->
                val o = arr.getJSONObject(i)
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(8.dp)) {
                        when (selected) {
                            "languages" -> { Text(o.getString("front"), style = MaterialTheme.typography.titleMedium); Text(o.getString("back"), style = MaterialTheme.typography.bodySmall) }
                            "chess" -> { Text(o.getString("move"), style = MaterialTheme.typography.titleMedium); Text("Difficulty: ${o.getString("difficulty")}", style = MaterialTheme.typography.bodySmall); Text(o.getString("fen"), style = MaterialTheme.typography.bodySmall) }
                            "cooking" -> { Text(o.getString("name"), style = MaterialTheme.typography.titleMedium); Text(o.getString("steps"), style = MaterialTheme.typography.bodySmall); Text("${o.getInt("calories")} kcal · ${o.getString("time")}", style = MaterialTheme.typography.bodySmall) }
                            "calisthenics" -> { Text(o.getString("skill"), style = MaterialTheme.typography.titleMedium); Text(o.getString("steps"), style = MaterialTheme.typography.bodySmall); Text("Level: ${o.getString("level")}", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
    }
}