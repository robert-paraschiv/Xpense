package com.rokudo.xpense.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.models.InsightItem
import com.rokudo.xpense.models.InsightType

/**
 * A single compact insight card used inside the horizontal insights row.
 */
@Composable
fun InsightCard(
    insight: InsightItem,
    modifier: Modifier = Modifier
) {
    val bgColor = when (insight.type) {
        InsightType.POSITIVE -> insight.accentColor.copy(alpha = 0.08f)
        InsightType.NEGATIVE -> insight.accentColor.copy(alpha = 0.08f)
        InsightType.NEUTRAL  -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier.width(150.dp),
        shape = MaterialTheme.shapes.medium,
        color = bgColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(insight.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint = insight.accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Value — the key metric
            Text(
                text = insight.value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.3).sp
                ),
                color = insight.accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Label — short description
            Text(
                text = insight.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    lineHeight = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Horizontally scrollable row of insight cards with staggered entrance animation.
 */
@Composable
fun InsightsRow(
    insights: List<InsightItem>,
    modifier: Modifier = Modifier
) {
    if (insights.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        itemsIndexed(
            items = insights,
            key = { index, item -> "${item.label}_$index" }
        ) { index, insight ->
            // Staggered fade-slide entrance
            var appeared by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { appeared = true }

            val alpha by animateFloatAsState(
                targetValue = if (appeared) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 350,
                    delayMillis = index * 60
                ),
                label = "insight_alpha_$index"
            )
            val translationY by animateFloatAsState(
                targetValue = if (appeared) 0f else 24f,
                animationSpec = tween(
                    durationMillis = 350,
                    delayMillis = index * 60
                ),
                label = "insight_ty_$index"
            )

            InsightCard(
                insight = insight,
                modifier = Modifier
                    .alpha(alpha)
                    .graphicsLayer { this.translationY = translationY }
            )
        }
    }
}

