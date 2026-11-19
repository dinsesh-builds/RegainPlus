package com.regainplus.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.regainplus.ui.viewmodel.OutfitsViewModel
import com.regainplus.ui.components.ConfirmDeleteButton
import com.regainplus.ui.viewmodel.WardrobeViewModel

@Composable
fun WardrobeScreen(vm: WardrobeViewModel = hiltViewModel(), outfitsVm: OutfitsViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    val items by vm.items.collectAsState()
    val outfits by outfitsVm.outfits.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("top") }
    var color by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("all") }
    var outfitName by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var photoUri by remember { mutableStateOf<String?>(null) }
    val stylePrefs = remember { ctx.getSharedPreferences("style_refs", Context.MODE_PRIVATE) }
    var refUri by remember { mutableStateOf(stylePrefs.getString("ref_uri", null)) }
    var skinTone by remember { mutableStateOf(stylePrefs.getInt("skin", 2)) }
    val capsulePrefs = remember { ctx.getSharedPreferences("capsules", Context.MODE_PRIVATE) }
    var capsules by remember { mutableStateOf(capsulePrefs.getStringSet("sets", emptySet()) ?: emptySet()) }
    var capsuleName by remember { mutableStateOf("") }
    var capsuleSelected by remember { mutableStateOf(setOf<String>()) }
    var typeFilter by remember { mutableStateOf("all") }
    var seasonFilter by remember { mutableStateOf("all") }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { photoUri = it.toString() }
    }
    val refPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            refUri = it.toString()
            stylePrefs.edit().putString("ref_uri", refUri).apply()
        }
    }
    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 16.dp) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Items", maxLines = 1) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Outfits", maxLines = 1) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Style", maxLines = 1) })
            Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Declutter", maxLines = 1) })
            Tab(selected = tab == 4, onClick = { tab = 4 }, text = { Text("Capsules", maxLines = 1) })
        }
        Column(Modifier.padding(16.dp).weight(1f).verticalScroll(rememberScrollState())) {
            when (tab) {
                0 -> {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = season, onValueChange = { season = it }, label = { Text("Season") }, modifier = Modifier.weight(1f), singleLine = true)
                        Button(onClick = { if (name.isNotBlank()) { vm.add(name, type, color, season); name = "" } }) { Text("Add") }
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = typeFilter == "all", onClick = { typeFilter = "all" }, label = { Text("All") })
                        items.map { it.type.lowercase() }.distinct().take(8).forEach { t ->
                            FilterChip(selected = typeFilter == t, onClick = { typeFilter = t }, label = { Text(t) })
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("all", "spring", "summer", "autumn", "winter").forEach { s ->
                            FilterChip(selected = seasonFilter == s, onClick = { seasonFilter = s }, label = { Text(s.replaceFirstChar { it.uppercase() }) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (items.isEmpty()) {
                        Text("No items yet. Add your first piece above.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val visibleItems = items.filter { it ->
                        (typeFilter == "all" || it.type.lowercase() == typeFilter) &&
                            (seasonFilter == "all" || it.season.lowercase() == seasonFilter || it.season == "all")
                    }
                    val grouped = visibleItems.groupBy { it.type }
                    grouped.forEach { (type, list) ->
                        Text(type.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth().height((list.size / 2 * 132 + 132).dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false
                        ) {
                            items(list.size) { i ->
                                val w = list[i]
                                Card(
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Box(
                                            Modifier.fillMaxWidth().height(64.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(w.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                        }
                                        Text(w.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(w.color.ifBlank { "—" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Review-driven fix: generate a season-aware outfit instead of random mismatches
                    Button(onClick = {
                        val seasonNow = when (java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)) {
                            in 2..4 -> "spring"; in 5..7 -> "summer"; in 8..10 -> "autumn"; else -> "winter"
                        }
                        fun pick(prefixes: Set<String>) = items.filter { w ->
                            val t = w.type.lowercase()
                            prefixes.any { t.contains(it) } && (w.season.equals(seasonNow, true) || w.season.equals("all", true))
                        }.ifEmpty { items.filter { w -> prefixes.any { w.type.lowercase().contains(it) } } }
                            .randomOrNull()
                        val top = pick(setOf("top", "shirt", "tshirt", "dress", "jacket"))
                        val bottom = pick(setOf("bottom", "trouser", "jean", "pant", "skirt"))
                        val shoes = pick(setOf("shoe", "sneaker", "sandal", "boot"))
                        val chosen = listOfNotNull(top, bottom, shoes).distinct()
                        if (chosen.isNotEmpty()) {
                            outfitsVm.add("Dress me (${seasonNow})", chosen.joinToString(",") { it.id }, "auto", seasonNow)
                            selectedItems = chosen.map { it.id }.toSet()
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Dress me — smart outfit") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = outfitName, onValueChange = { outfitName = it }, label = { Text("Outfit name") }, modifier = Modifier.fillMaxWidth())
                    items.forEach { it ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selectedItems.contains(it.id), onCheckedChange = { c -> selectedItems = if (c) selectedItems + it.id else selectedItems - it.id })
                            Text("${it.name} (${it.type})", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Button(onClick = { if (outfitName.isNotBlank() && selectedItems.isNotEmpty()) { outfitsVm.add(outfitName, selectedItems.joinToString(","), "", ""); outfitName = ""; selectedItems = emptySet() } }, modifier = Modifier.padding(top = 8.dp)) { Text("Save outfit") }
                    Spacer(Modifier.height(8.dp))
                    outfits.forEach { o ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Column(Modifier.padding(8.dp)) {
                                Text(o.name, style = MaterialTheme.typography.titleMedium)
                                Text("Items: ${o.itemIds.split(",").size}", style = MaterialTheme.typography.bodySmall)
                                if (o.lastWorn != null) Text("Last worn: ${java.text.SimpleDateFormat("MMM d").format(java.util.Date(o.lastWorn))}", style = MaterialTheme.typography.bodySmall)
                                Row {
                                    TextButton(onClick = { outfitsVm.markWorn(o.id) }) { Text("Mark worn") }
                                    ConfirmDeleteButton(onConfirm = { outfitsVm.delete(o.id) })
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Text("Style Lab", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Your skin tone", style = MaterialTheme.typography.titleSmall)
                    val tones = listOf(
                        Triple("Porcelain", "FFF0E0", "Best: navy, emerald, burgundy, soft pastels"),
                        Triple("Fair", "FFE0C4", "Best: teal, cobalt, deep green, coral"),
                        Triple("Light", "F5CBA7", "Best: olive, mustard, royal blue, rust"),
                        Triple("Medium", "D9A066", "Best: white, black, jade, terracotta, gold"),
                        Triple("Tan", "A96A3B", "Best: white, cobalt, emerald, coral, cream"),
                        Triple("Deep", "6B3F26", "Best: bright white, gold, jewel tones, cobalt")
                    )
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tones.forEachIndexed { i, t ->
                            Surface(
                                modifier = Modifier.size(44.dp).clickable { skinTone = i; stylePrefs.edit().putInt("skin", i).apply() },
                                shape = CircleShape,
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#${t.second}")),
                                border = if (i == skinTone) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {}
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(tones[skinTone].first, style = MaterialTheme.typography.bodyMedium)
                    Text(tones[skinTone].third, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Your photo & dress reference", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { photoPicker.launch("image/*") }) { Text("Your photo") }
                        OutlinedButton(onClick = { refPicker.launch("image/*") }) { Text("Dress reference") }
                    }
                    val shown = refUri ?: photoUri
                    if (shown != null) {
                        val bmp = try {
                            @Suppress("DEPRECATION")
                            android.provider.MediaStore.Images.Media.getBitmap(ctx.contentResolver, Uri.parse(shown)).asImageBitmap()
                        } catch (_: Exception) { null }
                        if (bmp != null) {
                            androidx.compose.foundation.Image(bitmap = bmp, contentDescription = "Reference image", modifier = Modifier.fillMaxWidth().height(200.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)))
                            TextButton(onClick = { refUri = null; stylePrefs.edit().remove("ref_uri").apply() }) { Text("Clear reference") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Pinterest inspiration:", style = MaterialTheme.typography.titleSmall)
                    val searches = listOf("outfit ideas", "capsule wardrobe", "streetwear men", "minimalist fashion", "color palette outfit", "gym wear")
                    searches.forEach { q ->
                        TextButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pinterest.com/search/pins/?q=${java.net.URLEncoder.encode(q, "UTF-8")}"))) }) { Text(q) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tip: Save pins you like, then recreate with items in your closet. Match colors to your seasonal palette.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                3 -> {
                    // Wear it or lose it: items unworn for 30+ days (or never worn)
                    val now = System.currentTimeMillis()
                    val threshold = now - 30L * 24 * 60 * 60 * 1000
                    val stale = items.filter { it.lastWorn == null || it.lastWorn < threshold }
                    Text("Wear it or lose it", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (stale.isEmpty()) "Nothing stale — your closet is active."
                        else "${stale.size} items unworn for 30+ days. Wear them or let them go.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    stale.forEach { w ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text("${w.name} · ${w.type}", style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        w.lastWorn?.let { "Last worn ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US).format(java.util.Date(it))}" } ?: "Never worn",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    TextButton(onClick = { vm.markWorn(w.id) }, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)) { Text("Wore it", maxLines = 1) }
                                    ConfirmDeleteButton(onConfirm = { vm.delete(w.id) }, label = "Let go")
                                }
                            }
                        }
                    }
                    }
                    4 -> {
                        Text("Capsule wardrobe", style = MaterialTheme.typography.titleMedium)
                        Text("Build a small set you can mix & match for a season.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = capsuleName, onValueChange = { capsuleName = it }, label = { Text("Capsule name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(Modifier.height(4.dp))
                        items.forEach { it ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = capsuleSelected.contains(it.id), onCheckedChange = { c -> capsuleSelected = if (c) capsuleSelected + it.id else capsuleSelected - it.id })
                                Text("${it.name} (${it.type})", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Button(onClick = {
                            if (capsuleName.isNotBlank() && capsuleSelected.isNotEmpty()) {
                                capsulePrefs.edit().putStringSet("sets", capsules + "${capsuleName}|${capsuleSelected.joinToString(",")}").apply()
                                capsules = capsulePrefs.getStringSet("sets", emptySet()) ?: emptySet()
                                capsuleName = ""; capsuleSelected = emptySet()
                            }
                        }, modifier = Modifier.padding(top = 8.dp)) { Text("Save capsule") }
                        Spacer(Modifier.height(8.dp))
                        capsules.forEach { entry ->
                            val (cname, ids) = entry.split("|", limit = 2).let { if (it.size == 2) it[0] to it[1] else entry to "" }
                            Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Column(Modifier.padding(8.dp)) {
                                    Text(cname, style = MaterialTheme.typography.titleMedium)
                                    Text("${ids.split(",").filter { it.isNotBlank() }.size} items", style = MaterialTheme.typography.bodySmall)
                                    TextButton(onClick = {
                                        capsulePrefs.edit().putStringSet("sets", capsules - entry).apply()
                                        capsules = capsulePrefs.getStringSet("sets", emptySet()) ?: emptySet()
                                    }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
                }
    }
}