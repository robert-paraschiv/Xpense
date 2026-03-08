package com.rokudo.xpense.fragments

import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.rokudo.xpense.R
import com.rokudo.xpense.components.AnimatedAmountText
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.models.SpentMostItem
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.BarChartUtils
import com.rokudo.xpense.utils.PieChartUtils

@Composable
fun HomeScreen(
    wallet: Wallet?,
    latestTransaction: Transaction?,
    barChartTransactions: List<Transaction>,
    statisticsDoc: StatisticsDoc?,
    spentMostItems: List<SpentMostItem>,
    bankBalance: String?,
    bankCurrency: String?,
    onWalletClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onAddBankClick: () -> Unit,
    onFabClick: () -> Unit,
    onBarChartClick: () -> Unit,
    onPieChartClick: () -> Unit,
    onTransactionClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_add_24),
                    contentDescription = "Add Transaction"
                )
            }
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                actions = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Wallet Header
            WalletHeader(
                walletTitle = wallet?.title,
                onWalletClick = onWalletClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Balance Cards
            BalanceCardsRow(
                wallet = wallet,
                bankBalance = bankBalance,
                bankCurrency = bankCurrency,
                onAdjustBalanceClick = onAdjustBalanceClick,
                onAddBankClick = onAddBankClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Latest Transaction
            AnimatedVisibility(
                visible = latestTransaction != null,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Column {
                    Text(
                        text = "Latest Transaction",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                    XpenseCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onTransactionClick
                    ) {
                        if (latestTransaction != null) {
                            LatestTransactionItem(transaction = latestTransaction)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Charts Section
            ChartsSection(
                barChartTransactions = barChartTransactions,
                statisticsDoc = statisticsDoc,
                spentMostItems = spentMostItems,
                walletCurrency = wallet?.currency ?: "",
                onBarChartClick = onBarChartClick,
                onPieChartClick = onPieChartClick
            )
        }
    }
}

@Composable
private fun WalletHeader(
    walletTitle: String?,
    onWalletClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onWalletClick)
        ) {
            Text(
                text = walletTitle ?: "Select Wallet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            IconButton(onClick = onWalletClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_keyboard_arrow_down_24),
                    contentDescription = "Select Wallet",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_person_24),
            contentDescription = "User Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun BalanceCardsRow(
    wallet: Wallet?,
    bankBalance: String?,
    bankCurrency: String?,
    onAdjustBalanceClick: () -> Unit,
    onAddBankClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Wallet Balance Card
        XpenseCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = onAdjustBalanceClick
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SuggestionChip(
                    onClick = onAdjustBalanceClick,
                    label = {
                        Text("Adjust", style = MaterialTheme.typography.labelSmall)
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.ic_round_edit_24),
                            null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = wallet?.currency ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AnimatedAmountText(
                    amount = wallet?.amount?.toString() ?: "0.00",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bank Balance Card
        XpenseCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = if (bankBalance == null) onAddBankClick else null
        ) {
            if (bankBalance != null) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text("Bank Account", style = MaterialTheme.typography.labelSmall)
                        },
                        icon = {
                            Icon(
                                painterResource(id = R.drawable.ic_baseline_local_atm_24),
                                null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bankCurrency ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedAmountText(
                        amount = bankBalance,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Tap to Link\nBank Account",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartsSection(
    barChartTransactions: List<Transaction>,
    statisticsDoc: StatisticsDoc?,
    spentMostItems: List<SpentMostItem>,
    walletCurrency: String,
    onBarChartClick: () -> Unit,
    onPieChartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left column: Bar chart + Spent Most
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Last 7 days spending",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            XpenseCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onClick = onBarChartClick
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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))

            SpentMostCard(
                spentMostItems = spentMostItems,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        // Right column: Pie chart
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "This month's spending",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            XpenseCard(
                modifier = Modifier.fillMaxSize(),
                onClick = onPieChartClick
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
                                walletCurrency,
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

@Composable
private fun SpentMostCard(
    spentMostItems: List<SpentMostItem>,
    modifier: Modifier = Modifier
) {
    XpenseCard(modifier = modifier) {
        AnimatedContent(
            targetState = spentMostItems.isNotEmpty(),
            transitionSpec = {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
            },
            label = "spent_most_content"
        ) { hasItems ->
            if (hasItems) {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(spentMostItems) { item ->
                        SpentMostItemCard(item)
                    }
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "No data",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SpentMostItemCard(item: SpentMostItem) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.title ?: "",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = item.category ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = item.amount ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
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

    XpenseTheme(dynamicColor = false) {
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
            onFabClick = {},
            onBarChartClick = {},
            onPieChartClick = {},
            onTransactionClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    val mockWallet = Wallet().apply {
        id = "1"
        title = "Personal"
        currency = "$"
        amount = 1250.50
    }

    XpenseTheme(dynamicColor = false) {
        HomeScreen(
            wallet = mockWallet,
            latestTransaction = null,
            barChartTransactions = emptyList(),
            statisticsDoc = null,
            spentMostItems = emptyList(),
            bankBalance = null,
            bankCurrency = null,
            onWalletClick = {},
            onAdjustBalanceClick = {},
            onAddBankClick = {},
            onFabClick = {},
            onBarChartClick = {},
            onPieChartClick = {},
            onTransactionClick = {}
        )
    }
}
