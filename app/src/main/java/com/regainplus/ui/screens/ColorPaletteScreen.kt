package com.regainplus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Season(val name: String, val hexes: List<String>, val avoid: String)

val SEASONS = listOf(
    Season("Light Spring", listOf("#FFF4D6","#F5C542","#FF6F61","#28B7A8"), "black, cool pink, icy tones"),
    Season("Warm Spring", listOf("#FFF4D6","#F4A51C","#E94B35","#54A24B"), "blue-gray, taupe, cool burgundy"),
    Season("Bright Spring", listOf("#FFFFFF","#FF6F61","#F4A51C","#177E7A"), "muted earthy, dusty rose"),
    Season("Light Summer", listOf("#E8F0F8","#A8D8EA","#B0C4DE","#D8BFD8"), "orange, warm yellow, black"),
    Season("True Summer", listOf("#D8E2E8","#8FAFC7","#B0A0C0","#6B8E9B"), "warm tones, earthy shades"),
    Season("Soft Summer", listOf("#C9C9C9","#A0A0B0","#8A9BA8","#B8A9C9"), "bright vivid, warm orange"),
    Season("Soft Autumn", listOf("#E8D4B0","#C78319","#B24A24","#4F5426"), "icy pastels, blue-gray"),
    Season("Warm Autumn", listOf("#F5E6C8","#C78319","#B24A24","#8E2F27"), "cool pink, lavender, optic white"),
    Season("Deep Autumn", listOf("#E8D4B0","#B24A24","#5C2E22","#2F211A"), "icy pastels, cool pink"),
    Season("Deep Winter", listOf("#FFFFFF","#000000","#B00030","#004B3C"), "camel, mustard, orange"),
    Season("True Winter", listOf("#FFFFFF","#000000","#8A1538","#006B54"), "warm beige, dusty sage"),
    Season("Bright Winter", listOf("#FFFFFF","#FF0000","#0000FF","#006B54"), "muted, warm, low-contrast")
)

@Composable
fun ColorPaletteScreen() {
    var step by remember { mutableStateOf(0) }
    var undertone by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var chroma by remember { mutableStateOf("") }
    val result = remember(undertone, value, chroma) {
        if (undertone.isEmpty() || value.isEmpty() || chroma.isEmpty()) null
        else {
            val seasonName = when (Triple(undertone, value, chroma)) {
                Triple("warm", "light", "bright") -> "Bright Spring"
                Triple("warm", "light", "soft") -> "Light Spring"
                Triple("warm", "medium", "bright") -> "Warm Spring"
                Triple("warm", "medium", "soft") -> "Warm Autumn"
                Triple("warm", "deep", "bright") -> "Deep Autumn"
                Triple("warm", "deep", "soft") -> "Soft Autumn"
                Triple("cool", "light", "bright") -> "Bright Winter"
                Triple("cool", "light", "soft") -> "Light Summer"
                Triple("cool", "medium", "bright") -> "True Winter"
                Triple("cool", "medium", "soft") -> "True Summer"
                Triple("cool", "deep", "bright") -> "Deep Winter"
                Triple("cool", "deep", "soft") -> "Soft Summer"
                else -> "True Spring"
            }
            SEASONS.firstOrNull { it.name == seasonName } ?: SEASONS.first()
        }
    }
    Column(Modifier.padding(16.dp)) {
        Text("Color Palette", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        if (result == null) {
            Text("Step ${step + 1}/3", style = MaterialTheme.typography.titleMedium)
            when (step) {
                0 -> {
                    Text("Undertone: warm (golden/peachy) or cool (pink/blue)?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { undertone = "warm"; step = 1 }) { Text("Warm") }
                        Button(onClick = { undertone = "cool"; step = 1 }) { Text("Cool") }
                    }
                }
                1 -> {
                    Text("Value: light, medium, or deep?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { value = "light"; step = 2 }) { Text("Light") }
                        Button(onClick = { value = "medium"; step = 2 }) { Text("Medium") }
                        Button(onClick = { value = "deep"; step = 2 }) { Text("Deep") }
                    }
                }
                2 -> {
                    Text("Chroma: soft (muted) or bright (vivid)?")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { chroma = "soft"; step = 3 }) { Text("Soft") }
                        Button(onClick = { chroma = "bright"; step = 3 }) { Text("Bright") }
                    }
                }
            }
        } else {
            Text("Your season: ${result.name}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                result.hexes.forEach { h ->
                    Box(Modifier.size(48.dp).background(Color(android.graphics.Color.parseColor(h))))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Avoid: ${result.avoid}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { step = 0; undertone = ""; value = ""; chroma = "" }) { Text("Retake") }
        }
    }
}