package com.rokudo.xpense.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.utils.CategoriesUtil
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * A pure-Compose donut / pie chart that integrates with Material 3 theming.
 *
 * @param categories    Map of category name to amount spent
 * @param totalSpent    Sum of all spending (used for center text and percentages)
 * @param currency      Currency symbol shown in center
 * @param isCompact     true on HomeScreen (smaller, no interaction, center text)
 * @param selectedSlice The currently highlighted slice label, or null
 * @param onSliceClick  Optional callback when a slice is tapped (label name)
 * @param modifier      Outer modifier
 */
@Composable
fun XpenseDonutChart(
    categories: Map<String, Double>?,
    totalSpent: Double,
    currency: String,
    isCompact: Boolean,
    selectedSlice: String? = null,
    onSliceClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (categories.isNullOrEmpty() || totalSpent == 0.0) return

    // Build slices, filtering out Income and zero-value entries
    val slices = remember(categories, totalSpent) {
        categories
            .filter { it.key != "Income" && it.value != 0.0 }
            .entries
            .sortedByDescending { it.value }
            .map { (name, amount) ->
                val catColor = CategoriesUtil.expenseCategoryList
                    .find { it.name == name }?.color
                DonutSlice(
                    label = name,
                    value = (amount / totalSpent).toFloat(),
                    color = if (catColor != null) Color(catColor) else Color.Gray
                )
            }
    }

    if (slices.isEmpty()) return

    // Animate sweep progress from 0 to 1
    val animationProgress = remember { Animatable(0f) }
    val dataKey = remember(categories) { categories.hashCode() }
    LaunchedEffect(dataKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val baseStrokeWidth: Dp = if (isCompact) 28.dp else 36.dp
    val highlightExtraStroke: Dp = 4.dp
    val gapAngle = 2f

    // Animate highlight fraction per slice (0f = normal, 1f = fully highlighted)
    val sliceHighlights = slices.map { slice ->
        animateFloatAsState(
            targetValue = if (selectedSlice == slice.label) 1f else 0f,
            animationSpec = tween(durationMillis = 250),
            label = "highlight_${slice.label}"
        )
    }
    // Animate dimming: 1f = full opacity, 0.45f = dimmed
    val dimAlpha by animateFloatAsState(
        targetValue = if (selectedSlice != null) 0.45f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "dim_alpha"
    )

    // Use a Column so the legend flows below the chart naturally
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Donut ring + center text ──
        val chartHeight = if (isCompact) 170.dp else 220.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            contentAlignment = Alignment.Center
        ) {
            // Pre-compute slice angles for tap detection
            val sliceAngles = remember(slices) {
                val angles = mutableListOf<Triple<Float, Float, String>>()
                var start = -90f
                slices.forEach { slice ->
                    val sweep = slice.value * (360f - gapAngle * slices.size)
                    angles.add(Triple(start, sweep, slice.label))
                    start += sweep + gapAngle
                }
                angles
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .then(
                        if (onSliceClick != null) {
                            Modifier.pointerInput(sliceAngles) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    var angle = Math
                                        .toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                        .toFloat()
                                    angle = (angle + 90f + 360f) % 360f

                                    val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                    val chartDim = min(size.width, size.height)
                                    val totalStroke = baseStrokeWidth.toPx() + highlightExtraStroke.toPx()
                                    val outerR = (chartDim - totalStroke) / 2f + totalStroke
                                    val innerR = (chartDim - totalStroke) / 2f - baseStrokeWidth.toPx()

                                    if (dist in innerR..outerR) {
                                        for ((start, sweep, label) in sliceAngles) {
                                            val normStart = (start + 90f + 360f) % 360f
                                            val normEnd = (normStart + sweep) % 360f
                                            val hit = if (normStart < normEnd) {
                                                angle in normStart..normEnd
                                            } else {
                                                angle >= normStart || angle <= normEnd
                                            }
                                            if (hit) {
                                                onSliceClick(label)
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        } else Modifier
                    )
            ) {
                val highlightPx = highlightExtraStroke.toPx()
                val basePx = baseStrokeWidth.toPx()
                // Reserve space for the thickest possible stroke
                val maxStrokePx = basePx + highlightPx
                val diameter = min(size.width, size.height) - maxStrokePx
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweep = slice.value * (360f - gapAngle * slices.size) * animationProgress.value
                    val highlightFraction = sliceHighlights[index].value
                    val strokePx = basePx + highlightPx * highlightFraction
                    // Selected slice stays full opacity; others use animated dim
                    val alpha = highlightFraction + (1f - highlightFraction) * dimAlpha

                    drawArc(
                        color = slice.color.copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                    startAngle += sweep + gapAngle
                }
            }

            // Center text (compact / home only)
            if (isCompact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = DecimalFormat("#,##0.00").format(totalSpent),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Legend (only for analytics / non-compact) ──
        if (!isCompact) {
            Spacer(modifier = Modifier.height(8.dp))
            DonutLegend(slices = slices, selectedSlice = selectedSlice, onSliceClick = onSliceClick)
        }
    }
}

@Composable
private fun DonutLegend(
    slices: List<DonutSlice>,
    selectedSlice: String?,
    onSliceClick: ((String) -> Unit)?
) {
    val pf = DecimalFormat("#0.0")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val items = slices.take(8)
        val rows = items.chunked(2)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { slice ->
                    val isSelected = selectedSlice == slice.label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (onSliceClick != null) {
                                    Modifier.pointerInput(slice.label) {
                                        detectTapGestures { onSliceClick(slice.label) }
                                    }
                                } else Modifier
                            )
                            .padding(vertical = 2.dp)
                    ) {
                        Canvas(modifier = Modifier.size(if (isSelected) 10.dp else 8.dp)) {
                            drawCircle(color = slice.color)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${slice.label} ${pf.format(slice.value * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
