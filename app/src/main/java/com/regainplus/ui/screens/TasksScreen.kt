package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.data.entity.Task
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.TaskViewModel

private enum class TaskSection(val label: String) { DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly") }

private fun taskSection(task: Task): TaskSection {
    val t = task.title
    return when {
        t.contains("MONTHLY", ignoreCase = true) -> TaskSection.MONTHLY
        t.contains("WEEKLY", ignoreCase = true) -> TaskSection.WEEKLY
        else -> TaskSection.DAILY
    }
}

@Composable
fun TasksScreen(vm: TaskViewModel = hiltViewModel()) {
    val tasks by vm.tasks.collectAsState()
    var title by remember { mutableStateOf("") }
    val grouped = remember(tasks) { tasks.groupBy { taskSection(it) } }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        item {
            Text("Tasks", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("New task") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = { if (title.isNotBlank()) { vm.add(title); title = "" } }) { Text("Add") }
            }
            Spacer(Modifier.height(12.dp))
        }

        TaskSection.entries.forEach { section ->
            val sectionTasks = grouped[section].orEmpty()
            if (sectionTasks.isNotEmpty() || tasks.isEmpty() && section == TaskSection.DAILY) {
                item(key = "header_${section.name}") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${section.label} (${sectionTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
            items(sectionTasks, key = { it.id }) { t ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = t.done, onCheckedChange = { vm.toggle(t) })
                    Text(
                        t.title,
                        modifier = Modifier.weight(1f),
                        style = if (t.done) MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough)
                        else MaterialTheme.typography.bodyMedium
                    )
                    ConfirmDeleteButton(onConfirm = { vm.delete(t.id) })
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    "No tasks yet. Add one above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}