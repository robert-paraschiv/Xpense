package com.rokudo.xpense.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.models.TransEntry

/**
 * A pure-Compose Canvas bar chart that integrates with Material 3 theming.
 *
 * @param entries  List of TransEntry (day label + amount)
 * @param modifier outer modifier
 */
@Composable
fun XpenseBarChart(
    entries: List<TransEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface

    val maxAmount = remember(entries) { entries.maxOfOrNull { it.amount } ?: 1f }

    // Animate bars from 0 → 1
    val animationProgress = remember { Animatable(0f) }
    val dataKey = remember(entries) { entries.hashCode() }
    LaunchedEffect(dataKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 10.sp.toPx() }
    val valueTextSizePx = with(density) { 9.sp.toPx() }
    val labelColorArgb = colorToArgb(labelColor)
    val valueColorArgb = colorToArgb(valueColor)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
    ) {
        val barCount = entries.size
        val bottomPadding = 40.dp.toPx() // space for labels
        val topPadding = 20.dp.toPx()    // space for value text above bars
        val chartHeight = size.height - bottomPadding - topPadding
        val barSpacing = 6.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(48.dp.toPx())
        val totalBarsWidth = barWidth * barCount + totalSpacing
        val startX = (size.width - totalBarsWidth) / 2f

        entries.forEachIndexed { index, entry ->
            val barHeight = if (maxAmount > 0f) {
                (entry.amount / maxAmount) * chartHeight * animationProgress.value
            } else 0f

            val x = startX + index * (barWidth + barSpacing)
            val y = topPadding + chartHeight - barHeight

            // Draw bar with rounded top corners
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw value text above bar
            if (animationProgress.value > 0.5f && entry.amount > 0f) {
                val valueText = if (entry.amount >= 100) {
                    String.format("%.0f", entry.amount)
                } else {
                    String.format("%.1f", entry.amount)
                }
                val textPaint = android.graphics.Paint().apply {
                    color = valueColorArgb
                    textSize = valueTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    x + barWidth / 2,
                    y - 4.dp.toPx(),
                    textPaint
                )
            }

            // Draw label below
            val labelPaint = android.graphics.Paint().apply {
                color = labelColorArgb
                textSize = labelTextSizePx
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val labelX = x + barWidth / 2
            val labelY = topPadding + chartHeight + 20.dp.toPx()

            if (barCount > 5) {
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(-35f, labelX, labelY)
                drawContext.canvas.nativeCanvas.drawText(entry.day, labelX, labelY, labelPaint)
                drawContext.canvas.nativeCanvas.restore()
            } else {
                drawContext.canvas.nativeCanvas.drawText(entry.day, labelX, labelY, labelPaint)
            }
        }
    }
}

private fun colorToArgb(color: Color): Int {
    return android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )
}
