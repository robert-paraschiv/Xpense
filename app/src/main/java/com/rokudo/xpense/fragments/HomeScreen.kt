package com.rokudo.xpense.fragments

import androidx.compose.ui.tooling.preview.Preview

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.rokudo.xpense.R
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.BarChartUtils
import com.rokudo.xpense.utils.PieChartUtils
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.SpentMostItem
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(
    wallet: Wallet?,
    latestTransaction: Transaction?,
    barChartTransactions: List<Transaction>,
    statisticsDoc: StatisticsDoc?,
    spentMostItems: List<SpentMostItem>,
    bankBalance: String?, // "Run" or "amount"
    bankCurrency: String?,
    onWalletClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit, // Pass wallet amount
    onAddBankClick: () -> Unit,
    onFabClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_round_add_24), contentDescription = "Add Transaction")
            }
        },
        bottomBar = {
            // Placeholder for BottomAppBar if managed by activity, but here fragment had it.
            // In a full migration, navigation would be composable too. for now, just replicate look or wrap existing
            // Since Fragment uses CoordinatorLayout with BottomAppBar and FAB anchored to it
            // We can replicate BottomAppBar here
             BottomAppBar(
                actions = {
                    // Placeholder items
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBF1F8)) // fragments_bg_color
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(8.dp)
        ) {
            // Wallet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = wallet?.title ?: "Select Wallet",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    IconButton(onClick = onWalletClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_keyboard_arrow_down_24),
                            contentDescription = "Select Wallet"
                        )
                    }
                }
                // Shared Icon Placeholder
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_person_24),
                    contentDescription = "User Profile",
                    modifier = Modifier.size(40.dp)
                )
            }

            // Wallet Balance & Bank Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(120.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onAdjustBalanceClick() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        SuggestionChip(
                            onClick = { onAdjustBalanceClick() },
                            label = { Text("Adjust") },
                            icon = { Icon(painterResource(id = R.drawable.ic_round_edit_24), null, modifier = Modifier.size(16.dp)) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wallet?.currency ?: "",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = wallet?.amount?.toString() ?: "0.00",
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { if (bankBalance == null) onAddBankClick() },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    if (bankBalance != null) {
                         Column(
                            modifier = Modifier.padding(16.dp).fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                             SuggestionChip(
                                onClick = {},
                                label = { Text("Bank Account") },
                                icon = { Icon(painterResource(id = R.drawable.ic_baseline_local_atm_24), null, modifier = Modifier.size(16.dp)) }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bankCurrency ?: "",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bankBalance,
                                fontSize = 20.sp
                            )
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Tap to Link\nBank Account",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Latest Transaction
            if (latestTransaction != null) {
                Text(
                    text = "Latest Transaction",
                    modifier = Modifier.padding(start = 12.dp),
                    fontSize = 12.sp
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    LatestTransactionItem(transaction = latestTransaction)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Charts Section (Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "Last 7 days spending",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    // Bar Chart
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                    ) {
                        AndroidView(
                            factory = { context ->
                                BarChart(context).apply {
                                    BarChartUtils.setupBarChart(this, TextView(context).currentTextColor, true)
                                }
                            },
                            update = { chart ->
                                if (barChartTransactions.isNotEmpty()) {
                                    BarChartUtils.updateBarchartData(chart, ArrayList(barChartTransactions), TextView(chart.context).currentTextColor)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                         text = "Spent most on",
                         fontSize = 12.sp,
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    // Spent Most On List Placeholder
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                    ) {
                        if (spentMostItems.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(spentMostItems) { item ->
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(item.title ?: "", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(item.category ?: "", fontSize = 10.sp)
                                        Text(item.amount ?: "", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text("No data")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Pie Chart
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                     Text(
                         text = "This month's spending",
                         fontSize = 12.sp,
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                    ) {
                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    PieChartUtils.setupPieChart(this, TextView(context).currentTextColor, true)
                                }
                            },
                            update = { chart ->
                                if (statisticsDoc != null) {
                                    PieChartUtils.updatePieChartData(
                                        chart,
                                        wallet?.currency ?: "",
                                        statisticsDoc.amountByCategory,
                                        statisticsDoc.totalAmountSpent,
                                        true
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockWallet = Wallet().apply {
        id = "1"
        title = "Personal"
        currency = "$"
        amount = 1250.50
    }

    val mockTransaction = Transaction().apply {
        id = "1"
        userName = "John Doe"
        amount = 50.0
        currency = "$"
        date = java.util.Date()
        category = "Food"
    }

    HomeScreen(
        wallet = mockWallet,
        latestTransaction = mockTransaction,
        barChartTransactions = listOf(mockTransaction, mockTransaction),
        statisticsDoc = null,
        spentMostItems = listOf(
            SpentMostItem("Groceries", "Food", "$150", "Today"),
            SpentMostItem("Rent", "Housing", "$1200", "Monthly")
        ),
        bankBalance = "5000.00",
        bankCurrency = "$",
        onWalletClick = {},
        onAdjustBalanceClick = {},
        onAddBankClick = {},
        onFabClick = {}
    )
}
