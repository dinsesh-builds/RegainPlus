package com.regainplus.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/** Two-tap delete button to prevent accidental data loss. */
@Composable
fun ConfirmDeleteButton(onConfirm: () -> Unit, label: String = "Del") {
    var armed by remember { mutableStateOf(false) }
    LaunchedEffect(armed) {
        if (armed) { delay(3000); armed = false }
    }
    TextButton(onClick = {
        if (armed) { onConfirm(); armed = false } else armed = true
    }) {
        Text(if (armed) "Confirm?" else label)
    }
}
