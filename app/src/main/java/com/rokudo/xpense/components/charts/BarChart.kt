package com.rokudo.xpense.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.models.TransEntry

/**
 * A pure-Compose Canvas bar chart that integrates with Material 3 theming.
 * Supports tap-to-highlight on individual bars.
 *
 * @param entries  List of TransEntry (day label + amount)
 * @param onBarClick optional callback with the tapped bar index
 * @param modifier outer modifier
 */
@Composable
fun XpenseBarChart(
    entries: List<TransEntry>,
    onBarClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    val barColor = MaterialTheme.colorScheme.primary
    val highlightColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    val maxAmount = remember(entries) { entries.maxOfOrNull { it.amount } ?: 1f }

    // Animate bars from 0 to 1
    val animationProgress = remember { Animatable(0f) }
    val dataKey = remember(entries) { entries.hashCode() }
    LaunchedEffect(dataKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    // Selected bar index (-1 = none)
    var selectedIndex by remember { mutableStateOf(-1) }
    // Reset selection when data changes
    LaunchedEffect(dataKey) { selectedIndex = -1 }

    // Animate per-bar highlight (0f = normal, 1f = selected)
    val barHighlights = entries.indices.map { i ->
        animateFloatAsState(
            targetValue = if (selectedIndex == i) 1f else 0f,
            animationSpec = tween(durationMillis = 200),
            label = "bar_hl_$i"
        )
    }
    // Animate global dim when any bar is selected
    val dimAlpha by animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 0.35f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "bar_dim"
    )

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 10.sp.toPx() }
    val valueTextSizePx = with(density) { 9.sp.toPx() }
    val selectedValueTextSizePx = with(density) { 11.sp.toPx() }
    val labelColorArgb = colorToArgb(labelColor)
    val valueColorArgb = colorToArgb(valueColor)
    val highlightColorArgb = colorToArgb(highlightColor)

    // Keep bar rects for tap detection
    data class BarRect(val x: Float, val y: Float, val width: Float, val height: Float)
    val barRects = remember { mutableStateListOf<BarRect>() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
            .pointerInput(dataKey) {
                detectTapGestures { offset ->
                    val tappedIdx = barRects.indexOfFirst { rect ->
                        // Extend hit area a bit above and below the bar
                        offset.x in rect.x..(rect.x + rect.width) &&
                                offset.y in (rect.y - 20.dp.toPx())..(rect.y + rect.height + 30.dp.toPx())
                    }
                    selectedIndex = if (tappedIdx == selectedIndex) -1 else tappedIdx
                    if (tappedIdx >= 0) onBarClick?.invoke(tappedIdx)
                }
            }
    ) {
        val barCount = entries.size
        val bottomPadding = 40.dp.toPx()
        val topPadding = 20.dp.toPx()
        val chartHeight = size.height - bottomPadding - topPadding
        val barSpacing = 6.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtMost(48.dp.toPx())
        val totalBarsWidth = barWidth * barCount + totalSpacing
        val startX = (size.width - totalBarsWidth) / 2f

        // Rebuild rects
        barRects.clear()

        entries.forEachIndexed { index, entry ->
            val barHeight = if (maxAmount > 0f) {
                (entry.amount / maxAmount) * chartHeight * animationProgress.value
            } else 0f

            val x = startX + index * (barWidth + barSpacing)
            val y = topPadding + chartHeight - barHeight

            barRects.add(BarRect(x, y, barWidth, barHeight))

            val hl = barHighlights[index].value  // 0f..1f animated
            // Bar alpha: selected → full, others → animated dim
            val barAlpha = hl + (1f - hl) * dimAlpha

            // Background track (subtle)
            drawRoundRect(
                color = surfaceVariant.copy(alpha = 0.5f),
                topLeft = Offset(x, topPadding),
                size = Size(barWidth, chartHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Bar
            drawRoundRect(
                color = barColor.copy(alpha = barAlpha),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Value text above bar
            if (animationProgress.value > 0.5f && entry.amount > 0f) {
                val isSelected = hl > 0.5f
                val valueText = if (entry.amount >= 100) {
                    String.format("%.0f", entry.amount)
                } else {
                    String.format("%.1f", entry.amount)
                }
                val textPaint = android.graphics.Paint().apply {
                    color = if (isSelected) highlightColorArgb else valueColorArgb
                    textSize = if (isSelected) selectedValueTextSizePx else valueTextSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    isFakeBoldText = isSelected
                }
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    x + barWidth / 2,
                    y - 4.dp.toPx(),
                    textPaint
                )
            }

            // Label below
            val isLabelSelected = hl > 0.5f
            val labelPaint = android.graphics.Paint().apply {
                color = if (isLabelSelected) highlightColorArgb else labelColorArgb
                textSize = labelTextSizePx
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                isFakeBoldText = isLabelSelected
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
