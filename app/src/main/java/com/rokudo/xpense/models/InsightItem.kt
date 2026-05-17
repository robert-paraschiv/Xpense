package com.rokudo.xpense.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single smart insight to display in Analytics.
 *
 * @param icon      Material icon for the card
 * @param value     The key metric (e.g. "+25%", "$30", "Sat")
 * @param label     Short description (e.g. "vs last month", "daily avg")
 * @param accentColor  Color for the accent/icon/value
 * @param type      Semantic type for styling
 */
data class InsightItem(
    val icon: ImageVector,
    val value: String,
    val label: String,
    val accentColor: Color,
    val type: InsightType = InsightType.NEUTRAL
)

enum class InsightType {
    POSITIVE,   // green — good news (spending down, savings up)
    NEGATIVE,   // red — alert (spending up, overspend)
    NEUTRAL     // blue-gray — informational
}

