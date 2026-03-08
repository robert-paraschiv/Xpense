package com.rokudo.xpense.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.ExpenseRed
import com.rokudo.xpense.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LatestTransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = transaction.picUrl,
            contentDescription = "User profile picture",
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
            error = painterResource(id = R.drawable.ic_baseline_person_24)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.userName ?: "Unknown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val dateFormat = SimpleDateFormat("EEE, MMM dd, HH:mm", Locale.getDefault())
            Text(
                text = transaction.date?.let { dateFormat.format(it) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val isIncome = transaction.type == Transaction.INCOME_TYPE
            val amountColor = if (isIncome) IncomeGreen else ExpenseRed
            val amountPrefix = if (isIncome) "+" else "-"
            val amountText = if (transaction.amount != null) {
                "$amountPrefix${transaction.amount} ${transaction.currency ?: ""}"
            } else ""

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleSmall,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.category ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
