package com.rokudo.xpense.components

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
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val visual = CategoryIconMapper.get(transaction.category)
    val isIncome = transaction.type == Transaction.INCOME_TYPE
    val isTransfer = transaction.type == Transaction.TRANSFER_TYPE

    val amountColor = when {
        isIncome -> IncomeGreen
        isTransfer -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> ExpenseRed
    }
    val prefix = when {
        isIncome -> "+"
        isTransfer -> ""
        else -> "-"
    }
    val currency = transaction.currency ?: ""
    val amountText = if (transaction.amount != null) {
        "$prefix$currency${String.format("%.2f", transaction.amount)}"
    } else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(visual.containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = transaction.category,
                tint = visual.color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title
                    ?: transaction.userName
                    ?: transaction.category
                    ?: "Transaction",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.category ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.date != null) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(transaction.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Amount — single line with currency baked in
        Text(
            text = amountText,
            style = MaterialTheme.typography.bodyMedium,
            color = amountColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Keep old name as alias for backward compat with HomeScreen
@Composable
fun LatestTransactionItem(transaction: Transaction) {
    TransactionRow(transaction = transaction)
}
