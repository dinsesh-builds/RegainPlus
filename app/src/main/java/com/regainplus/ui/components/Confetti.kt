package com.regainplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

/** Lightweight confetti burst; runs ~2.5s then particles fall off-screen. */
@Composable
fun ConfettiOverlay(visible: Boolean) {
    if (!visible) return
    val palette = listOf(
        Color(0xFF3593E0), Color(0xFFFFD640), Color(0xFF914EF9),
        Color(0xFF1BB0B5), Color(0xFFFF8000), Color(0xFF83D65C)
    )
    val particles = remember {
        List(60) {
            Triple(
                Random.nextFloat(),                              // x start (0..1)
                Random.nextFloat() * 2f - 1f,                    // horizontal drift -1..1
                palette[Random.nextInt(palette.size)]            // color
            )
        }
    }
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (t < 1f) {
            withFrameNanos { now ->
                t = ((now - start) / 2_500_000_000f).coerceIn(0f, 1f)
            }
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val s = size.width * 0.015f
        particles.forEach { (x, drift, color) ->
            val px = size.width * x + size.width * 0.08f * drift * sin(t * 12f + x * 10f)
            val py = size.height * t * (0.9f + 0.4f * x)
            drawRect(color = color, topLeft = Offset(px, py), size = Size(s, s * 1.6f))
        }
    }
}
