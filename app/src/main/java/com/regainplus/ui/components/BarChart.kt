package com.regainplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return
    val max = data.maxOf { it.second }.coerceAtLeast(1f)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            val slot = size.width / data.size
            val barWidth = (slot * 0.55f).coerceAtMost(64.dp.toPx())
            data.forEachIndexed { i, (_, v) ->
                val h = (v / max) * size.height * 0.85f
                val x = slot * i + (slot - barWidth) / 2
                drawRect(barColor, topLeft = Offset(x, size.height - h), size = Size(barWidth, h))
            }
        }
        Row(Modifier.fillMaxWidth()) {
            data.forEach { (label, _) ->
                Text(
                    label.take(8), style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
