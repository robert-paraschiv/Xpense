package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokudo.xpense.components.EmptyState
import com.rokudo.xpense.components.TransactionRow
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.ExpenseRed
import com.rokudo.xpense.ui.theme.IncomeGreen
import com.rokudo.xpense.ui.theme.XpenseTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListTransactionsScreen(
    transactions: List<Transaction>,
    selectedMonth: String,
    availableMonths: List<String>,
    onBackClick: () -> Unit,
    onMonthSelected: (String) -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val currentIndex = availableMonths.indexOf(selectedMonth)

    // Group transactions by day
    val groupedTransactions = remember(transactions) {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        transactions
            .filter { it.date != null }
            .sortedByDescending { it.date?.time ?: 0 }
            .groupBy { dateFormat.format(it.date!!) }
    }
    val dayTotals = remember(groupedTransactions) {
        groupedTransactions.mapValues { (_, txs) ->
            txs.sumOf { t ->
                val amt = t.amount ?: 0.0
                if (t.type == Transaction.INCOME_TYPE) amt else -amt
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(title = "Transactions", onBackClick = onBackClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // ─── Month Navigator with arrows ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) onMonthSelected(availableMonths[currentIndex - 1])
                    },
                    enabled = currentIndex > 0
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = if (currentIndex > 0) MaterialTheme.colorScheme.onSurface
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
                Text(
                    text = selectedMonth,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(
                    onClick = {
                        if (currentIndex < availableMonths.lastIndex) onMonthSelected(availableMonths[currentIndex + 1])
                    },
                    enabled = currentIndex < availableMonths.lastIndex
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = if (currentIndex < availableMonths.lastIndex) MaterialTheme.colorScheme.onSurface
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            // ─── Transaction List ───
            AnimatedContent(
                targetState = transactions.isEmpty(),
                transitionSpec = { fadeIn(tween(300)).togetherWith(fadeOut(tween(200))) },
                label = "transactions_content"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState(
                        visible = true,
                        title = "No transactions",
                        subtitle = "Nothing recorded for $selectedMonth",
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        groupedTransactions.forEach { (dayLabel, dayTransactions) ->
                            item(key = "day_$dayLabel") {
                                // Day header
                                val dayTotal = dayTotals[dayLabel] ?: 0.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dayLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = String.format("%+.2f", dayTotal),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayTotal >= 0) IncomeGreen else ExpenseRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // All transactions for this day in ONE card
                                XpenseCard(modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        dayTransactions.forEachIndexed { index, tx ->
                                            TransactionRow(
                                                transaction = tx,
                                                modifier = Modifier.clickable {
                                                    onTransactionClick(tx)
                                                }
                                            )
                                            if (index < dayTransactions.lastIndex) {
                                                HorizontalDivider(
                                                    modifier = Modifier.padding(horizontal = 68.dp),
                                                    thickness = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListTransactionsScreenPreview() {
    val mockTransactions = listOf(
        Transaction().apply {
            id = "1"; title = "Weekly groceries"; amount = 45.20; currency = "$"
            date = Date(); category = "Groceries"; type = Transaction.EXPENSE_TYPE
        },
        Transaction().apply {
            id = "2"; title = "Lunch at café"; amount = 12.0; currency = "$"
            date = Date(); category = "Restaurant"; type = Transaction.EXPENSE_TYPE
        },
        Transaction().apply {
            id = "3"; title = "Salary"; amount = 3000.0; currency = "$"
            date = Date(); category = "Income"; type = Transaction.INCOME_TYPE
        }
    )
    XpenseTheme(dynamicColor = false) {
        ListTransactionsScreen(
            transactions = mockTransactions,
            selectedMonth = "Mar 2026",
            availableMonths = listOf("Jan 2026", "Feb 2026", "Mar 2026"),
            onBackClick = {},
            onMonthSelected = {},
            onTransactionClick = {}
        )
    }
}
