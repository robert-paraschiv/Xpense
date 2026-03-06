package com.rokudo.xpense.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LatestTransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = transaction.picUrl,
            contentDescription = "User profile picture",
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.userName ?: "Unknown",
                fontWeight = FontWeight.Bold
            )
            val dateFormat = SimpleDateFormat("EEE, MMM dd, HH:mm", Locale.getDefault())
            Text(
                text = transaction.date?.let { dateFormat.format(it) } ?: "",
                style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val amountText = if (transaction.amount != null) "${transaction.amount} ${transaction.currency}" else ""
            Text(
                text = amountText,
                color = Color(0xFFD50000),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.category ?: "",
                fontSize = 12.sp
            )
        }
    }
}

