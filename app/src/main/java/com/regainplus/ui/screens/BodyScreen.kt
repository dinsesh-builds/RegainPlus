package com.regainplus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.components.BarChart
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.BodyViewModel
import com.regainplus.ui.viewmodel.WaterViewModel

@Composable
fun BodyScreen(vm: BodyViewModel = hiltViewModel(), waterVm: WaterViewModel = hiltViewModel()) {
    val cals by vm.calories.collectAsState()
    val metrics by vm.metrics.collectAsState()
    val water by waterVm.todayMl.collectAsState()
    val waterGoal = waterVm.goalMl
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.addPhoto(it.toString()) }
    }
    var meal by remember { mutableStateOf("") }
    var cal by remember { mutableStateOf("") }
    var p by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var f by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Text("Body", style = MaterialTheme.typography.headlineMedium)
        Text("Calories", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = meal, onValueChange = { meal = it }, label = { Text("Meal") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = cal, onValueChange = { cal = it }, label = { Text("Kcal") }, modifier = Modifier.width(80.dp), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            Button(onClick = { if (meal.isNotBlank()) { vm.addCalorie(meal, cal.toIntOrNull() ?: 0, p.toIntOrNull() ?: 0, c.toIntOrNull() ?: 0, f.toIntOrNull() ?: 0); meal = ""; cal = ""; p = ""; c = ""; f = "" } }) { Text("Add") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("P") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("C") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            OutlinedTextField(value = f, onValueChange = { f = it }, label = { Text("F") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        }
        val todayCals = cals.filter { it.date > System.currentTimeMillis() - 86400000 }
        Text("Total today: ${todayCals.sumOf { it.calories }} kcal · P:${todayCals.sumOf { it.protein }}g C:${todayCals.sumOf { it.carbs }}g F:${todayCals.sumOf { it.fat }}g", style = MaterialTheme.typography.bodySmall)
        cals.take(8).forEach { c ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "${c.meal} — ${c.calories} kcal",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    ConfirmDeleteButton(onConfirm = { vm.deleteCalorie(c.id) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Weight", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            Button(onClick = { weight.toDoubleOrNull()?.let { vm.addWeight(it, "") ; weight = "" } }) { Text("Log") }
        }
        metrics.take(5).forEach { m ->
            Text("${"%.1f".format(m.weightKg)} kg · ${java.text.SimpleDateFormat("MMM d").format(java.util.Date(m.date))}", style = MaterialTheme.typography.bodySmall)
        }
        val weightSeries = metrics.filter { it.weightKg > 0 }.sortedBy { it.date }
            .map { java.text.SimpleDateFormat("d/M", java.util.Locale.US).format(java.util.Date(it.date)) to it.weightKg.toFloat() }
        if (weightSeries.size >= 2) {
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Weight trend", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    BarChart(data = weightSeries, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { photoPicker.launch("image/*") }) { Text("Pick progress photo") }
        metrics.filter { it.photoUri != null }.take(4).forEach { m ->
            Text("Photo · ${java.text.SimpleDateFormat("MMM d").format(java.util.Date(m.date))}", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        Text("Water: $water ml / $waterGoal ml", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { waterVm.add(250) }) { Text("+250ml") }
            Button(onClick = { waterVm.add(500) }) { Text("+500ml") }
            OutlinedTextField(value = waterGoal.toString(), onValueChange = { waterVm.setGoal(it.toIntOrNull() ?: waterGoal) }, label = { Text("Goal") }, modifier = Modifier.width(80.dp), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
        }
    }
}