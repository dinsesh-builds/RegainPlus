package com.regainplus.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.ExpandableSection
import com.regainplus.ui.viewmodel.BlockViewModel
import com.regainplus.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudyScreen(studyVm: StudyViewModel = hiltViewModel(), blockVm: BlockViewModel = hiltViewModel(), navController: androidx.navigation.NavHostController? = null) {
    val sessions by studyVm.sessions.collectAsState()
    val blocked by blockVm.items.collectAsState()
    val studyItems = blocked.filter { it.kind == "study" }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE) }
    LaunchedEffect(Unit) {
        com.regainplus.service.FocusTimerHolder.init(ctx)
        val finished = com.regainplus.service.FocusTimerHolder.consumePendingFocus()
        if (finished > 0) studyVm.save("General", "", finished, System.currentTimeMillis())
    }
    val running by com.regainplus.service.FocusTimerHolder.running.collectAsState()
    val isBreak by com.regainplus.service.FocusTimerHolder.isBreak.collectAsState()
    val remaining by com.regainplus.service.FocusTimerHolder.remaining.collectAsState()
    val focusMin by com.regainplus.service.FocusTimerHolder.focusMin.collectAsState()
    val breakMin by com.regainplus.service.FocusTimerHolder.breakMin.collectAsState()
    val strict by com.regainplus.service.FocusTimerHolder.strict.collectAsState()
    val pomodoro by com.regainplus.service.FocusTimerHolder.pomodoro.collectAsState()
    var studyInput by remember { mutableStateOf("") }
    val studyMode by com.regainplus.service.BlockHolder.studyMode.collectAsState()
    val studyAllow by com.regainplus.service.BlockHolder.studyAllow.collectAsState()

    var goal by remember { mutableStateOf(prefs.getInt("study_goal", 60)) }
    val todayMin = sessions.filter { it.start > System.currentTimeMillis() - 86400000 }.sumOf { it.minutes }
    var subject by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Tick from holder — survives navigation; session persisted on completion.
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running) {
            kotlinx.coroutines.delay(1000)
            val completed = com.regainplus.service.FocusTimerHolder.tick(ctx)
            if (completed) {
                val finished = com.regainplus.service.FocusTimerHolder.consumePendingFocus()
                studyVm.save(subject.ifBlank { "General" }, notes, finished, System.currentTimeMillis())
                subject = ""; notes = ""
            }
        }
    }
    val ringProgress = (remaining.toFloat() / ((if (isBreak) breakMin else focusMin) * 60).toFloat()).coerceIn(0f, 1f)
    val canStop = com.regainplus.service.FocusTimerHolder.canStop

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Circular focus ring with countdown
        Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val focusColor = MaterialTheme.colorScheme.secondary
            val breakColor = MaterialTheme.colorScheme.primary
            Canvas(Modifier.size(200.dp)) {
                val stroke = 24.dp.toPx(); val inset = stroke / 2
                drawArc(trackColor, -90f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - stroke, size.height - stroke))
                drawArc(if (isBreak) breakColor else focusColor, -90f, 360f * ringProgress, false, style = Stroke(stroke, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - stroke, size.height - stroke))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("%02d:%02d".format(remaining / 60, remaining % 60), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text(if (isBreak) "Break" else if (running) "Focusing" else "Focus timer", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))

        // Mode config row
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            NumberBox("Focus", focusMin) { com.regainplus.service.FocusTimerHolder.setFocusMin(ctx, it) }
            NumberBox("Break", breakMin) { com.regainplus.service.FocusTimerHolder.setBreakMin(ctx, it) }
            FilterChip(selected = pomodoro, onClick = { com.regainplus.service.FocusTimerHolder.setPomodoro(ctx, !pomodoro) }, label = { Text("Pomodoro") })
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Strict mode (no quit during focus)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = strict, enabled = !running || isBreak, onCheckedChange = { com.regainplus.service.FocusTimerHolder.setStrict(ctx, it) })
        }
        Spacer(Modifier.height(12.dp))

        // Focus CTA
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable {
                if (!running) com.regainplus.service.FocusTimerHolder.start(ctx)
                else if (canStop) {
                    if (!isBreak) studyVm.save(subject.ifBlank { "General" }, notes, com.regainplus.service.FocusTimerHolder.sessionStartMs, System.currentTimeMillis())
                    com.regainplus.service.FocusTimerHolder.stop(ctx)
                    subject = ""; notes = ""
                }
            },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp
        ) {
            Row(Modifier.padding(start = 24.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when {
                        !running -> "Start focus timer"
                        isBreak -> "End break"
                        strict -> "Locked by strict mode"
                        else -> "Stop & save session"
                    },
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                val actionEnabled = !running || canStop
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (actionEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ) {
                    Text(
                        if (running && canStop) "■" else "▶",
                        color = if (actionEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        Column(Modifier.padding(horizontal = 20.dp)) {
            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Daily goal: $todayMin / $goal min", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                OutlinedTextField(value = goal.toString(), onValueChange = { v -> v.toIntOrNull()?.let { goal = it; prefs.edit().putInt("study_goal", it).apply() } }, label = { Text("Goal") }, modifier = Modifier.width(80.dp), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            }
        }
        Spacer(Modifier.height(16.dp))

        ExpandableSection(
            title = "Resource library",
            subtitle = "Links and curated educational videos",
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
                var resName by remember { mutableStateOf("") }; var resUrl by remember { mutableStateOf("") }
                val resPrefs = remember { ctx.getSharedPreferences("resources", android.content.Context.MODE_PRIVATE) }
                var resources by remember { mutableStateOf(resPrefs.getStringSet("links", emptySet()) ?: emptySet()) }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = resName, onValueChange = { resName = it }, label = { Text("Title") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = resUrl, onValueChange = { resUrl = it }, label = { Text("URL") }, modifier = Modifier.weight(1.4f), singleLine = true)
                }
                Button(onClick = { if (resUrl.isNotBlank()) { resPrefs.edit().putStringSet("links", resources + "${resName.ifBlank { "Resource" }}|$resUrl").apply(); resources = resPrefs.getStringSet("links", emptySet()) ?: emptySet(); resName = ""; resUrl = "" } }, modifier = Modifier.padding(top = 6.dp)) { Text("Add resource") }
                resources.forEach { entry ->
                    val (name, url) = entry.split("|", limit = 2).let { if (it.size == 2) it[0] to it[1] else entry to entry }
                    Text(
                        name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))) }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text("YouTube (Educational, curated)", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Curated educational videos only. Search within this library and play in a restricted in-app browser (ads may appear; no other videos).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                data class EduVideo(val id: String, val title: String, val subject: String, val channel: String)
                val curatedVideos = remember {
                    try {
                        val json = ctx.assets.open("youtube_edu.json").bufferedReader().use { it.readText() }
                        val arr = org.json.JSONArray(json)
                        List(arr.length()) { i ->
                            val o = arr.getJSONObject(i)
                            EduVideo(
                                id = o.optString("id"),
                                title = o.optString("title"),
                                subject = o.optString("subject"),
                                channel = o.optString("channel")
                            )
                        }.filter { it.id.isNotBlank() && it.title.isNotBlank() }
                    } catch (_: Exception) {
                        emptyList<EduVideo>()
                    }
                }
                val subjects = remember(curatedVideos) { listOf("All") + curatedVideos.map { it.subject }.distinct().sorted() }
                var ytSearch by remember { mutableStateOf("") }
                var ytSubject by remember { mutableStateOf("All") }

                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ytSearch,
                    onValueChange = { ytSearch = it },
                    label = { Text("Search curated videos") },
                    placeholder = { Text("Title, subject, or channel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { s ->
                        FilterChip(selected = ytSubject == s, onClick = { ytSubject = s }, label = { Text(s, maxLines = 1) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                val filtered = curatedVideos.filter { v ->
                    (ytSubject == "All" || v.subject == ytSubject) &&
                        (ytSearch.isBlank() || listOf(v.title, v.subject, v.channel).any { it.contains(ytSearch, ignoreCase = true) })
                }
                if (filtered.isEmpty()) {
                    Text("No matching videos in the curated library.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    filtered.take(30).forEach { v ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable(enabled = navController != null) {
                                    val route = "youtube_edu?videoId=" + java.net.URLEncoder.encode(v.id, "UTF-8") +
                                        "&title=" + java.net.URLEncoder.encode(v.title, "UTF-8")
                                    navController?.navigate(route)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(v.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(2.dp))
                                Text("${v.subject} • ${v.channel}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                if (navController == null) {
                                    Text("Open Study screen in app to play", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                }
        }
        }
        Spacer(Modifier.height(16.dp))
        ExpandableSection(
            title = "Study Mode",
            subtitle = if (studyMode) "On — distractions blocked" else "Off — add study resources",
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(if (studyMode) "Blocking distractions" else "Allow only study resources", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (studyMode) "On — blocks distractions except your study resources"
                            else "Off — add your study resources, blockers stay off",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = studyMode, onCheckedChange = { on ->
                        com.regainplus.service.BlockHolder.setStudyMode(on, ctx)
                        com.regainplus.service.BlockMonitorService.sync(ctx)
                        com.regainplus.RegainPlusApp.syncWatchdog(ctx)
                    })
                }
                Spacer(Modifier.height(8.dp))
                Text("Study resources are allowed during focus. Everything else is blocked while Study Mode is on.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studyInput, onValueChange = { studyInput = it }, label = { Text("Study app / domain") }, modifier = Modifier.weight(1f), singleLine = true)
                    Button(onClick = { if (studyInput.isNotBlank()) { blockVm.add("study", studyInput); studyInput = "" } }) { Text("Add") }
                }
                studyItems.forEach { s ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { blockVm.delete(s.id) }) { Text("Remove") }
                    }
                }
        }
        Spacer(Modifier.height(16.dp))
        ExpandableSection(
            title = "History",
            subtitle = "${sessions.size} sessions",
            modifier = Modifier.padding(horizontal = 20.dp),
            initiallyExpanded = false
        ) {
            sessions.take(8).forEach { s ->
            Card(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        s.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${s.minutes} min · ${SimpleDateFormat("MMM d", Locale.US).format(Date(s.start))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
            if (sessions.isEmpty()) Text("No sessions yet. Start one above.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NumberBox(label: String, value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(value = value.toString(), onValueChange = { v -> v.toIntOrNull()?.let { if (it > 0) onChange(it) } }, label = { Text(label) }, modifier = Modifier.width(70.dp), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
}
