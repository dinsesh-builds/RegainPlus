package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.TaskViewModel

@Composable
fun TasksScreen(vm: TaskViewModel = hiltViewModel()) {
    val tasks by vm.tasks.collectAsState()
    var title by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Tasks", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("New task") }, modifier = Modifier.weight(1f))
            Button(onClick = { if (title.isNotBlank()) { vm.add(title); title = "" } }) { Text("Add") }
        }
        Spacer(Modifier.height(8.dp))
        tasks.forEach { t ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = t.done, onCheckedChange = { vm.toggle(t) })
                Text(t.title, modifier = Modifier.weight(1f), style = if (t.done) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium)
                ConfirmDeleteButton(onConfirm = { vm.delete(t.id) })
            }
        }
        if (tasks.isEmpty()) {
            Text("No tasks yet. Add one above.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}