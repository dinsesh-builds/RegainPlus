package com.regainplus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.regainplus.ui.components.ExpandableSection
import com.regainplus.ui.navigation.Screen

private data class MoreEntry(val route: String, val label: String, val desc: String, val icon: ImageVector)
private data class MoreGroup(val title: String, val subtitle: String, val items: List<MoreEntry>)

private val groups = listOf(
    MoreGroup("Learn & track", "Study, tasks, and review", listOf(
        MoreEntry(Screen.Study.route, "Study", "Pomodoro, focus, resources", Icons.AutoMirrored.Filled.MenuBook),
        MoreEntry("flashcards", "Flashcards", "Spaced-repetition review", Icons.AutoMirrored.Filled.MenuBook),
        MoreEntry("curriculum", "Curriculum", "Languages, chess, cooking, fitness", Icons.AutoMirrored.Filled.MenuBook),
        MoreEntry("weekly", "Weekly review", "Your last 7 days + reflection", Icons.Default.Star),
        MoreEntry(Screen.Tasks.route, "Tasks", "To-dos for the day", Icons.Default.CheckCircle),
    )),
    MoreGroup("Life", "Body, mind, and growth", listOf(
        MoreEntry(Screen.Body.route, "Body", "Calories, weight, water, photos", Icons.Default.MonitorWeight),
        MoreEntry(Screen.Mind.route, "Mind", "Mood, journal, defense", Icons.Default.Psychology),
        MoreEntry(Screen.Wardrobe.route, "Wardrobe", "Closet, outfits, style lab", Icons.Default.Checkroom),
        MoreEntry(Screen.Skills.route, "Skills", "Tracks, curricula, practice", Icons.Default.EmojiEvents),
    )),
    MoreGroup("Tools", "Usage, achievements, settings", listOf(
        MoreEntry(Screen.ScreenTime.route, "Screen time", "App usage & daily report", Icons.Default.Apps),
        MoreEntry(Screen.Achievements.route, "Achievements", "Badges & milestones", Icons.Default.Star),
        MoreEntry(Screen.Settings.route, "Settings", "Theme, security, data", Icons.Default.Settings),
    )),
)

@Composable
fun MoreScreen(navController: NavHostController) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp)) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 8 }) {
            Column(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text("More", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(4.dp))
                Text("Everything else in RegainPlus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        groups.forEachIndexed { gi, group ->
            AnimatedVisibility(visible = visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 10 + gi * 8 }) {
                ExpandableSection(
                    title = group.title,
                    subtitle = group.subtitle,
                    initiallyExpanded = gi == 0,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    group.items.forEachIndexed { i, e ->
                        Row(
                            Modifier.fillMaxWidth().clickable { navController.navigate(e.route) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                Box(Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                                    Icon(e.icon, contentDescription = e.label, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(e.label, style = MaterialTheme.typography.titleSmall)
                                Text(e.desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (i < group.items.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), modifier = Modifier.padding(start = 50.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
