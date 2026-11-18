package com.regainplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Filled area chart with an optional % change badge (Symmetry-style). */
@Composable
fun AreaChart(
    values: List<Float>,
    changePercent: Float? = null,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (values.size < 2) return
    val max = values.max().coerceAtLeast(1f)
    val min = values.min()
    val range = (max - min).coerceAtLeast(1f)
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Trend", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            changePercent?.let { pct ->
                val positive = pct >= 0f
                val c = if (positive) Color(0xFF83D65C) else Color(0xFFFF5252)
                Surface(shape = RoundedCornerShape(12.dp), color = c.copy(alpha = 0.15f)) {
                    Text(
                        "${if (positive) "+" else ""}${pct.toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = c,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            val n = values.size
            val stepX = size.width / (n - 1).coerceAtLeast(1)
            val points = values.mapIndexed { i, v ->
                Offset(i * stepX, size.height - ((v - min) / range) * size.height * 0.85f - size.height * 0.05f)
            }
            val fillPath = Path().apply {
                moveTo(points.first().x, size.height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, size.height)
                close()
            }
            drawPath(fillPath, color = lineColor.copy(alpha = 0.18f))
            for (i in 0 until points.size - 1) {
                drawLine(lineColor, points[i], points[i + 1], strokeWidth = 3f, cap = StrokeCap.Round)
            }
            points.forEach { p -> drawCircle(lineColor, radius = 4f, center = p) }
        }
    }
}
