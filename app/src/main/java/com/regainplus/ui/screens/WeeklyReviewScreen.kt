package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.viewmodel.BodyViewModel
import com.regainplus.ui.viewmodel.GymViewModel
import com.regainplus.ui.viewmodel.MindViewModel
import com.regainplus.ui.viewmodel.StudyViewModel
import com.regainplus.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyReviewScreen(
    studyVm: StudyViewModel = hiltViewModel(),
    gymVm: GymViewModel = hiltViewModel(),
    bodyVm: BodyViewModel = hiltViewModel(),
    mindVm: MindViewModel = hiltViewModel(),
    taskVm: TaskViewModel = hiltViewModel()
) {
    val sessions by studyVm.sessions.collectAsState()
    val gym by gymVm.logs.collectAsState()
    val cals by bodyVm.calories.collectAsState()
    val metrics by bodyVm.metrics.collectAsState()
    val moods by mindVm.logs.collectAsState()
    val tasks by taskVm.tasks.collectAsState()
    val weekAgo = System.currentTimeMillis() - 7 * 86400000L
    val weekSessions = sessions.filter { it.start >= weekAgo }
    val weekGym = gym.filter { it.date >= weekAgo }
    val weekCals = cals.filter { it.date >= weekAgo }
    val weekMoodDates = (0..6).map { offset ->
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() - offset * 86400000L))
    }.toSet()
    val weekMoods = moods.filter { it.date in weekMoodDates }
    val doneTasks = tasks.count { it.done }
    Column(Modifier.padding(16.dp)) {
        Text("Weekly Review", style = MaterialTheme.typography.headlineMedium)
        Text("Last 7 days", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Study: ${weekSessions.size} sessions · ${weekSessions.sumOf { it.minutes }} min", style = MaterialTheme.typography.bodyMedium)
                Text("Gym: ${weekGym.size} sessions", style = MaterialTheme.typography.bodyMedium)
                Text("Calories: ${weekCals.sumOf { it.calories }} kcal logged", style = MaterialTheme.typography.bodyMedium)
                Text("Weight logs: ${metrics.count { it.date >= weekAgo }}", style = MaterialTheme.typography.bodyMedium)
                Text("Mood entries: ${weekMoods.size} · avg ${if (weekMoods.isNotEmpty()) "%.1f".format(weekMoods.map { it.mood }.average()) else "-"}", style = MaterialTheme.typography.bodyMedium)
                Text("Tasks done: $doneTasks / ${tasks.size}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Reflection", style = MaterialTheme.typography.titleMedium)
        Text("What went well? What to adjust next week?", style = MaterialTheme.typography.bodySmall)
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val prefs = remember { ctx.getSharedPreferences("reflection", android.content.Context.MODE_PRIVATE) }
        val weekKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(weekAgo))
        var reflection by remember { mutableStateOf(prefs.getString(weekKey, "") ?: "") }
        OutlinedTextField(value = reflection, onValueChange = { reflection = it; prefs.edit().putString(weekKey, it).apply() }, label = { Text("Your reflection") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(12.dp)) {
                Text("What you did this week", style = MaterialTheme.typography.titleSmall)
                Text(
                    "You studied ${weekSessions.sumOf { it.minutes }} minutes across ${weekSessions.size} sessions, trained ${weekGym.size} times, logged ${weekCals.size} meals, checked in ${weekMoods.size} times and completed $doneTasks tasks.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}