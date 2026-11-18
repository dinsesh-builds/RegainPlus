package com.regainplus.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.regainplus.ui.screens.BlockScreen
import com.regainplus.ui.screens.FlashcardsScreen
import com.regainplus.ui.screens.CurriculumScreen
import com.regainplus.ui.screens.BodyScreen
import com.regainplus.ui.screens.ColorPaletteScreen
import com.regainplus.ui.screens.GlowUpScreen
import com.regainplus.ui.screens.GymScreen
import com.regainplus.ui.screens.HomeScreen
import com.regainplus.ui.screens.MoreScreen
import com.regainplus.ui.screens.MindScreen
import com.regainplus.ui.screens.ScreenTimeScreen
import com.regainplus.ui.screens.AchievementsScreen
import com.regainplus.ui.screens.SettingsScreen
import com.regainplus.ui.screens.WeeklyReviewScreen
import com.regainplus.ui.screens.SkillsScreen
import com.regainplus.ui.screens.StudyScreen
import com.regainplus.ui.screens.TasksScreen
import com.regainplus.ui.screens.WardrobeScreen

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Study : Screen("study", "Study")
    object Tasks : Screen("tasks", "Tasks")
    object Block : Screen("block", "Block")
    object Body : Screen("body", "Body")
    object Gym : Screen("gym", "Gym")
    object Glow : Screen("glow", "Glow")
    object Mind : Screen("mind", "Mind")
    object Wardrobe : Screen("wardrobe", "Wardrobe")
    object Skills : Screen("skills", "Skills")
    object ScreenTime : Screen("screentime", "Screen time")
    object Achievements : Screen("achievements", "Achievements")
    object Settings : Screen("settings", "Settings")
    object More : Screen("more", "More")
}

// Bottom bar: 4 main tabs + More (Regain-style, uncramped)
val bottomScreens = listOf(Screen.Home, Screen.Block, Screen.Gym, Screen.Glow, Screen.More)

// Everything reachable from More
val moreScreens = listOf(
    Screen.Study, Screen.Tasks, Screen.Body, Screen.Mind,
    Screen.Wardrobe, Screen.Skills, Screen.ScreenTime, Screen.Settings,
    Screen.Achievements,
)

private fun iconFor(s: Screen, filled: Boolean) = when (s) {
    Screen.Home -> if (filled) Icons.Filled.Home else Icons.Outlined.Home
    Screen.Block -> if (filled) Icons.Filled.Block else Icons.Outlined.Block
    Screen.Gym -> if (filled) Icons.Filled.FitnessCenter else Icons.Outlined.FitnessCenter
    Screen.Glow -> if (filled) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
    Screen.More -> if (filled) Icons.Filled.Apps else Icons.Outlined.Apps
    Screen.Study -> Icons.AutoMirrored.Filled.MenuBook
    Screen.Tasks -> Icons.Default.CheckCircle
    Screen.Body -> Icons.Default.MonitorWeight
    Screen.Mind -> Icons.Default.Psychology
    Screen.Wardrobe -> Icons.Default.Checkroom
    Screen.Skills -> Icons.Default.EmojiEvents
    Screen.ScreenTime -> Icons.Filled.Apps
    Screen.Achievements -> Icons.Default.Star
    Screen.Settings -> Icons.Default.Settings
}

private const val NAV_ANIM = 260

@Composable
fun RegainNavHost(navController: NavHostController) {
    NavHost(
        navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(tween(NAV_ANIM, easing = FastOutSlowInEasing)) { it / 6 } +
                fadeIn(tween(NAV_ANIM, easing = FastOutSlowInEasing))
        },
        exitTransition = {
            slideOutHorizontally(tween(NAV_ANIM, easing = FastOutSlowInEasing)) { -it / 10 } +
                fadeOut(tween(NAV_ANIM / 2))
        },
        popEnterTransition = {
            slideInHorizontally(tween(NAV_ANIM, easing = FastOutSlowInEasing)) { -it / 10 } +
                fadeIn(tween(NAV_ANIM, easing = FastOutSlowInEasing))
        },
        popExitTransition = {
            slideOutHorizontally(tween(NAV_ANIM, easing = FastOutSlowInEasing)) { it / 6 } +
                fadeOut(tween(NAV_ANIM / 2))
        }
    ) {
        composable(Screen.Home.route) { HomeScreen(navController = navController) }
        composable(Screen.Study.route) { StudyScreen() }
        composable(Screen.Tasks.route) { TasksScreen() }
        composable(Screen.Block.route) { BlockScreen() }
        composable(Screen.Body.route) { BodyScreen() }
        composable(Screen.Gym.route) { GymScreen() }
        composable(Screen.Glow.route) { GlowUpScreen(navController = navController) }
        composable("palette") { ColorPaletteScreen() }
        composable("flashcards") { FlashcardsScreen() }
        composable("curriculum") { CurriculumScreen() }
        composable(Screen.Mind.route) { MindScreen() }
        composable("weekly") { WeeklyReviewScreen() }
        composable(Screen.Wardrobe.route) { WardrobeScreen() }
        composable(Screen.Skills.route) { SkillsScreen() }
        composable(Screen.ScreenTime.route) { ScreenTimeScreen() }
        composable(Screen.Achievements.route) { AchievementsScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.More.route) { MoreScreen(navController = navController) }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val entry = navController.currentBackStackEntryAsState().value
    val current = entry?.destination?.route
    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Main pill: 4 primary tabs (Regain-style grouped nav)
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 6.dp
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    bottomScreens.take(4).forEach { s ->
                        val selected = current == s.route
                        Row(
                            Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
                                .clickable {
                                    navController.navigate(s.route) {
                                        launchSingleTop = true
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        restoreState = true
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                iconFor(s, selected),
                                contentDescription = s.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (selected) {
                                Text(
                                    s.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            // Separate More pill (icon, like Regain's standalone Blocks pill)
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 6.dp
            ) {
                val moreSelected = current == Screen.More.route || (current != null && current in moreScreens.map { it.route })
                Row(
                    Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
                        .clickable {
                            navController.navigate(Screen.More.route) {
                                launchSingleTop = true
                                popUpTo(Screen.Home.route) { saveState = true }
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        iconFor(Screen.More, moreSelected),
                        contentDescription = Screen.More.label,
                        tint = if (moreSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
