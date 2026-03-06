package com.rokudo.xpense.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.models.Transaction
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
    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF9FCFF)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBF1F8))
                .padding(paddingValues)
        ) {
            // Month Selector
            if (showMonthPicker) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
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
                                label = { Text(month) }
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { showMonthPicker = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedMonth,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions for this month",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { transaction ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTransactionClick(transaction) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                        ) {
                            LatestTransactionItem(transaction = transaction)
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

    val availableMonths = listOf(
        "Jan 2024", "Feb 2024", "Mar 2024", "Apr 2024"
    )

    ListTransactionsScreen(
        transactions = mockTransactions,
        selectedMonth = "Mar 2024",
        availableMonths = availableMonths,
        onBackClick = {},
        onMonthSelected = {},
        onTransactionClick = {}
    )
}

