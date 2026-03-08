package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokudo.xpense.components.EmptyState
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.XpenseTheme
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
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Transactions",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Month Selector
            AnimatedContent(
                targetState = showMonthPicker,
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                },
                label = "month_picker"
            ) { expanded ->
                if (expanded) {
                    XpenseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableMonths) { month ->
                                FilterChip(
                                    selected = month == selectedMonth,
                                    onClick = {
                                        onMonthSelected(month)
                                        showMonthPicker = false
                                    },
                                    label = { Text(month) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                } else {
                    XpenseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = { showMonthPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedMonth,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Transactions List
            AnimatedContent(
                targetState = transactions.isEmpty(),
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                },
                label = "transactions_content"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState(
                        visible = true,
                        title = "No transactions for this month",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            transactions,
                            key = { _, t -> t.id ?: UUID.randomUUID().toString() }
                        ) { _, transaction ->
                            XpenseCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                onClick = { onTransactionClick(transaction) }
                            ) {
                                LatestTransactionItem(transaction = transaction)
                            }
                        }
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
            id = "1"
            userName = "John Doe"
            amount = 50.0
            currency = "$"
            date = Date()
            category = "Food"
            type = Transaction.EXPENSE_TYPE
        },
        Transaction().apply {
            id = "2"
            userName = "Grocery Store"
            amount = 120.50
            currency = "$"
            date = Date()
            category = "Shopping"
            type = Transaction.EXPENSE_TYPE
        },
        Transaction().apply {
            id = "3"
            userName = "Salary"
            amount = 3000.0
            currency = "$"
            date = Date()
            category = "Income"
            type = Transaction.INCOME_TYPE
        }
    )

    XpenseTheme(dynamicColor = false) {
        ListTransactionsScreen(
            transactions = mockTransactions,
            selectedMonth = "Mar 2024",
            availableMonths = listOf("Jan 2024", "Feb 2024", "Mar 2024", "Apr 2024"),
            onBackClick = {},
            onMonthSelected = {},
            onTransactionClick = {}
        )
    }
}
