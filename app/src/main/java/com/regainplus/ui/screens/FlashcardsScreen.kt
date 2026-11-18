package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.viewmodel.FlashcardViewModel

@Composable
fun FlashcardsScreen(vm: FlashcardViewModel = hiltViewModel()) {
    val cards by vm.cards.collectAsState()
    val due by vm.due.collectAsState()
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var deck by remember { mutableStateOf("general") }
    var reviewing by remember { mutableStateOf(false) }
    var idx by remember { mutableStateOf(0) }
    var showBack by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Flashcards", style = MaterialTheme.typography.headlineMedium)
        Text("Due: ${due.size} · Total: ${cards.size}", style = MaterialTheme.typography.bodySmall)
        if (cards.isEmpty()) {
            Text("No cards yet. Add one below.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        if (due.isEmpty() && reviewing) { reviewing = false; showBack = false; idx = 0 }
        if (due.isNotEmpty() && reviewing) {
            val card = due.getOrElse(idx) { due.first() }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(card.front, style = MaterialTheme.typography.titleLarge)
                    if (showBack) {
                        Spacer(Modifier.height(8.dp))
                        Text(card.back, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (!showBack) {
                        Button(onClick = { showBack = true }) { Text("Show answer") }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.review(card, false); showBack = false; idx = (idx + 1).coerceAtMost(due.size - 1) }) { Text("Hard") }
                            Button(onClick = { vm.review(card, true); showBack = false; idx = (idx + 1).coerceAtMost(due.size - 1) }) { Text("Good") }
                        }
                    }
                }
            }
        } else {
            if (due.isNotEmpty()) Button(onClick = { reviewing = true; idx = 0; showBack = false }) { Text("Review due (${due.size})") }
        }
        Spacer(Modifier.height(12.dp))
        Text("Add card", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = deck, onValueChange = { deck = it }, label = { Text("Deck") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text("Front") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text("Back") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { if (front.isNotBlank()) { vm.add(deck, front, back); front = ""; back = "" } }, modifier = Modifier.padding(top = 8.dp)) { Text("Add") }
    }
}