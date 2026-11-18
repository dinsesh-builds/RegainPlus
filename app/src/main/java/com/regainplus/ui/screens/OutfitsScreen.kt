package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.OutfitsViewModel

@Composable
fun OutfitsScreen(vm: OutfitsViewModel = hiltViewModel()) {
    val outfits by vm.outfits.collectAsState()
    val items by vm.items.collectAsState()
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var occasion by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        Text("Outfits", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Outfit name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = occasion, onValueChange = { occasion = it }, label = { Text("Occasion") }, modifier = Modifier.fillMaxWidth())
        Text("Pick items:", style = MaterialTheme.typography.titleSmall)
        items.forEach { it ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = selected.contains(it.id), onCheckedChange = { c ->
                    selected = if (c) selected + it.id else selected - it.id
                })
                Text("${it.name} (${it.type})")
            }
        }
        Button(onClick = { if (name.isNotBlank() && selected.isNotEmpty()) { vm.add(name, selected.joinToString(","), occasion, ""); name = ""; selected = emptySet() } }, modifier = Modifier.padding(top = 8.dp)) { Text("Save outfit") }
        Spacer(Modifier.height(12.dp))
        outfits.forEach { o ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(8.dp)) {
                    Text(o.name, style = MaterialTheme.typography.titleMedium)
                    Text("Items: ${o.itemIds.split(",").size} · ${o.occasion}", style = MaterialTheme.typography.bodySmall)
                    if (o.lastWorn != null) Text("Last worn: ${java.text.SimpleDateFormat("MMM d").format(java.util.Date(o.lastWorn))}", style = MaterialTheme.typography.bodySmall)
                    Row {
                        TextButton(onClick = { vm.markWorn(o.id) }) { Text("Mark worn today") }
                        ConfirmDeleteButton(onConfirm = { vm.delete(o.id) })
                    }
                }
            }
        }
    }
}