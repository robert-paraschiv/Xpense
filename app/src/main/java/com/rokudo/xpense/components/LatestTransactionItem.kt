package com.rokudo.xpense.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.ExpenseRed
import com.rokudo.xpense.ui.theme.IncomeGreen
import com.rokudo.xpense.utils.CategoryIconMapper
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LatestTransactionItem(transaction: Transaction) {
    val visual = CategoryIconMapper.get(transaction.category)
    val isIncome = transaction.type == Transaction.INCOME_TYPE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon in a colored circle
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(visual.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = transaction.category,
                tint = visual.color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title + category + date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title
                    ?: transaction.userName
                    ?: transaction.category
                    ?: "Transaction",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = transaction.category ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = visual.color,
                    fontWeight = FontWeight.Medium
                )
                if (transaction.date != null) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(transaction.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Amount
        Column(horizontalAlignment = Alignment.End) {
            val amountColor = if (isIncome) IncomeGreen else ExpenseRed
            val amountPrefix = if (isIncome) "+" else "-"
            val amountText = if (transaction.amount != null) {
                "$amountPrefix${String.format("%.2f", transaction.amount)}"
            } else ""

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleSmall,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.currency ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
