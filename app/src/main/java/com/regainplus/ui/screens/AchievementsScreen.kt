package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.viewmodel.*

@Composable
fun AchievementsScreen(
    homeVm: HomeViewModel = hiltViewModel(),
    studyVm: StudyViewModel = hiltViewModel(),
    gymVm: GymViewModel = hiltViewModel(),
    cardVm: FlashcardViewModel = hiltViewModel(),
    wardrobeVm: WardrobeViewModel = hiltViewModel()
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    val dates by homeVm.activeDates.collectAsState()
    val sessions by studyVm.sessions.collectAsState()
    val gymLogs by gymVm.logs.collectAsState()
    val cards by cardVm.cards.collectAsState()
    val wardrobe by wardrobeVm.items.collectAsState()
    val streak = homeVm.streak()
    val totalMin = sessions.sumOf { it.minutes }

    val badges = listOf(
        "First Focus" to sessions.isNotEmpty(),
        "Focus 10h" to (totalMin >= 600),
        "7-Day Streak" to (streak >= 7),
        "30-Day Streak" to (streak >= 30),
        "Gym Starter" to gymLogs.isNotEmpty(),
        "Gym Regular" to (gymLogs.size >= 10),
        "Closet Curator" to (wardrobe.size >= 10),
        "Flashcard Scholar" to (cards.size >= 20),
        "Commitment Keeper" to prefs.getBoolean("committed", false),
        "Clean Day" to dates.isNotEmpty()
    )
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Achievements", style = MaterialTheme.typography.headlineMedium)
        Text("${badges.count { it.second }} / ${badges.size} unlocked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        badges.forEach { (name, unlocked) ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (unlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(if (unlocked) "★" else "☆", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
