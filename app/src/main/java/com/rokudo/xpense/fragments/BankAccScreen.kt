package com.rokudo.xpense.fragments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.models.Transaction
import java.text.DecimalFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccScreen(
    bAccount: BAccount?,
    balance: String?,
    transactions: List<Transaction>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onRefreshClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Account") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onRefreshClick) {
                        Text("Refresh")
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
            // Bank Account Details Card
            if (bAccount != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Bank Logo
                        Image(
                            painter = rememberAsyncImagePainter(bAccount.bankPic),
                            contentDescription = bAccount.bankName,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bank Name
                        Text(
                            text = bAccount.bankName ?: "Bank Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Balance
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = "Balance",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${balance ?: "0.00"} ${bAccount.linked_acc_currency ?: ""}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // IBAN
                        Text(
                            text = "IBAN: ${bAccount.linked_acc_iban ?: "N/A"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions Section
            if (isLoading && transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No transactions",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                        Text(
                            "Pull to refresh",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    "Recent Transactions",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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
fun BankAccScreenPreview() {
    val mockBAccount = BAccount().apply {
        bankName = "Sample Bank"
        linked_acc_iban = "RO12BANK1234567890"
        linked_acc_currency = "RON"
    }

    val mockTransactions = listOf(
        Transaction().apply {
            id = "1"
            amount = 150.0
            title = "Grocery Store"
            date = Date()
        },
        Transaction().apply {
            id = "2"
            amount = -50.0
            title = "ATM Withdrawal"
            date = Date()
        }
    )

    BankAccScreen(
        bAccount = mockBAccount,
        balance = "1234.56",
        transactions = mockTransactions,
        isLoading = false,
        onBackClick = {},
        onTransactionClick = {},
        onRefreshClick = {}
    )
}

