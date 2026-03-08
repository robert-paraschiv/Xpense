package com.rokudo.xpense.components.charts

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.utils.CategoriesUtil
import java.text.DecimalFormat
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * A pure-Compose donut / pie chart that integrates with Material 3 theming.
 *
 * @param categories  Map of category name → amount spent
 * @param totalSpent  Sum of all spending (used for center text & percentages)
 * @param currency    Currency symbol shown in center
 * @param isCompact   true on HomeScreen (smaller, no interaction, center text)
 * @param onSliceClick optional callback when a slice is tapped (label name)
 * @param modifier    outer modifier
 */
@Composable
fun XpenseDonutChart(
    categories: Map<String, Double>?,
    totalSpent: Double,
    currency: String,
    isCompact: Boolean,
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

    // Animate sweep progress from 0 → 1
    val animationProgress = remember { Animatable(0f) }
    val dataKey = remember(categories) { categories.hashCode() }
    LaunchedEffect(dataKey) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val strokeWidth = if (isCompact) 28.dp else 36.dp
    val gapAngle = 2f  // degrees between slices

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isCompact) 170.dp else 240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Accumulated start angles for tap detection
        val sliceAngles = remember(slices) {
            val angles = mutableListOf<Triple<Float, Float, String>>() // start, sweep, label
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
                .padding(16.dp)
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
                                angle = (angle + 90f + 360f) % 360f // normalize to start from top

                                val dist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                val radius = min(size.width, size.height) / 2f
                                val strokePx = strokeWidth.toPx()
                                val innerR = radius - strokePx
                                val outerR = radius

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
            val strokePx = strokeWidth.toPx()
            val diameter = min(size.width, size.height) - strokePx
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = slice.value * (360f - gapAngle * slices.size) * animationProgress.value
                drawArc(
                    color = slice.color,
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

        // Center text
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

        // Legend below chart for non-compact mode
        if (!isCompact) {
            // Show center total for non-compact too
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currency ${DecimalFormat("#,##0.00").format(totalSpent)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Legend row for non-compact (analytics)
    if (!isCompact) {
        DonutLegend(slices = slices)
    }
}

@Composable
private fun DonutLegend(slices: List<DonutSlice>) {
    val pf = DecimalFormat("#0.0")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Show up to 6 legend items in 2-column layout
        val items = slices.take(6)
        val rows = items.chunked(2)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { slice ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = slice.color)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${slice.label} ${pf.format(slice.value * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Fill empty spot if odd number
                if (row.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

