package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.rokudo.xpense.components.EmptyState
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.components.LoadingState
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.XpenseTheme
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Bank Account",
                onBackClick = onBackClick,
                actions = {
                    TextButton(onClick = onRefreshClick) {
                        Text("Refresh", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Bank Account Details Card
            AnimatedVisibility(
                visible = bAccount != null,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                if (bAccount != null) {
                    XpenseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(bAccount.bankPic),
                                contentDescription = bAccount.bankName,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = bAccount.bankName ?: "Bank Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${balance ?: "0.00"} ${bAccount.linked_acc_currency ?: ""}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "IBAN: ${bAccount.linked_acc_iban ?: "N/A"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions Section
            Box(modifier = Modifier.fillMaxSize()) {
                LoadingState(
                    isLoading = isLoading && transactions.isEmpty(),
                    modifier = Modifier.fillMaxSize()
                )

                EmptyState(
                    visible = !isLoading && transactions.isEmpty(),
                    title = "No transactions",
                    subtitle = "Pull to refresh"
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = transactions.isNotEmpty(),
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(300))
                ) {
                    Column {
                        Text(
                            "Recent Transactions",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transactions) { transaction ->
                                XpenseCard(
                                    modifier = Modifier.fillMaxWidth(),
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

    XpenseTheme(dynamicColor = false) {
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
}
