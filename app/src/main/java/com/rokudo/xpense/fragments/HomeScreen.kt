package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rokudo.xpense.R
import com.rokudo.xpense.components.*
import com.rokudo.xpense.components.charts.XpenseDonutChart
import com.rokudo.xpense.models.SpentMostItem
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.models.WalletUser
import com.rokudo.xpense.ui.theme.*
import com.rokudo.xpense.utils.CategoryIconMapper
import java.util.Calendar

@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(
    wallet: Wallet?,
    latestTransaction: Transaction?,
    recentTransactions: List<Transaction>,
    barChartTransactions: List<Transaction>,
    statisticsDoc: StatisticsDoc?,
    spentMostItems: List<SpentMostItem>,
    bankBalance: String?,
    bankCurrency: String?,
    monthlySpent: Double,
    monthlyIncome: Double,
    topCategories: List<Pair<String, Double>>,
    spendingChangePercent: Double? = null,
    spendingTrendUp: Boolean? = null,
    biggestTransaction: Transaction? = null,
    savingsRate: Double? = null,
    dailyAverage: Double = 0.0,
    onWalletClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onAddBankClick: () -> Unit,
    onFabClick: () -> Unit,
    onBarChartClick: () -> Unit,
    onPieChartClick: () -> Unit,
    onTransactionClick: () -> Unit,
    onSpentClick: () -> Unit,
    onEarnedClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val greeting = getGreeting()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_add_24),
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(paddingValues)
        ) {
            // ─── Header: Greeting + Settings ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = wallet?.title ?: "Select Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.clickable(onClick = onWalletClick)
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Balance Display ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAdjustBalanceClick),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = wallet?.currency ?: "$",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.2f", wallet?.amount ?: 0.0),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 36.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onWalletClick)
                ) {
                    Text(
                        text = wallet?.title ?: "Select Wallet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_keyboard_arrow_down_24),
                        contentDescription = "Switch wallet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Bank balance line
                AnimatedVisibility(visible = bankBalance != null) {
                    Text(
                        text = "Bank: ${bankCurrency ?: ""} $bankBalance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // Shared wallet indicator
                val otherUser = remember(wallet) {
                    WalletUser.getOtherWalletUser(wallet?.walletUsers)
                }
                AnimatedVisibility(visible = otherUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        AsyncImage(
                            model = otherUser?.userPic,
                            contentDescription = "Shared with",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape),
                            placeholder = painterResource(id = R.drawable.ic_baseline_person_24),
                            error = painterResource(id = R.drawable.ic_baseline_person_24)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Shared with ${otherUser?.userName ?: "someone"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Summary Pills: Spent / Earned ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val spentSubtitle = if (spendingChangePercent != null) {
                    val arrow = if (spendingTrendUp == true) "↑" else "↓"
                    "$arrow ${String.format("%.0f", kotlin.math.abs(spendingChangePercent))}% vs last mo"
                } else null

                SummaryPill(
                    label = "Spent",
                    amount = "-${String.format("%.0f", monthlySpent)}",
                    containerColor = ExpenseRedLight,
                    contentColor = ExpenseRed,
                    modifier = Modifier.weight(1f),
                    subtitle = spentSubtitle,
                    onClick = onSpentClick
                )
                SummaryPill(
                    label = "Earned",
                    amount = "+${String.format("%.0f", monthlyIncome)}",
                    containerColor = IncomeGreenLight,
                    contentColor = IncomeGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onEarnedClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Quick Insights (compact inline) ───
            val hasInsights = (savingsRate != null && monthlyIncome > 0) ||
                    dailyAverage > 0 ||
                    spendingChangePercent != null ||
                    biggestTransaction != null
            AnimatedVisibility(
                visible = hasInsights,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut(tween(300))
            ) {
                XpenseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // vs Last Month
                        if (spendingChangePercent != null) {
                            val isUp = spendingChangePercent > 0
                            val changeColor = if (isUp) ExpenseRed else IncomeGreen
                            val arrow = if (isUp) "↑" else "↓"
                            val label = if (isUp) "more" else "less"
                            QuickInsightRow(
                                icon = if (isUp) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                iconTint = changeColor,
                                label = "vs last month",
                                value = "$arrow ${String.format("%.0f", kotlin.math.abs(spendingChangePercent))}% $label",
                                valueColor = changeColor
                            )
                        }

                        // Savings Rate
                        if (savingsRate != null && monthlyIncome > 0) {
                            val savingsColor = if (savingsRate >= 0) IncomeGreen else ExpenseRed
                            QuickInsightRow(
                                icon = Icons.Outlined.Savings,
                                iconTint = savingsColor,
                                label = "Savings rate",
                                value = "${String.format("%.0f", savingsRate)}%",
                                valueColor = savingsColor
                            )
                        }

                        // Daily Average
                        if (dailyAverage > 0) {
                            QuickInsightRow(
                                icon = Icons.Outlined.CalendarMonth,
                                iconTint = MaterialTheme.colorScheme.primary,
                                label = "Daily average",
                                value = "${wallet?.currency ?: "$"}${String.format("%.0f", dailyAverage)}",
                                valueColor = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Biggest Expense
                        if (biggestTransaction != null) {
                            val visual = CategoryIconMapper.get(biggestTransaction.category)
                            QuickInsightRow(
                                icon = visual.icon,
                                iconTint = visual.color,
                                label = "Biggest: ${biggestTransaction.title ?: biggestTransaction.category ?: "Expense"}",
                                value = "${wallet?.currency ?: "$"}${String.format("%.0f", biggestTransaction.amount ?: 0.0)}",
                                valueColor = ExpenseRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── Recent Transactions ───
            val txToShow = if (recentTransactions.isNotEmpty()) recentTransactions
                else listOfNotNull(latestTransaction)

            if (txToShow.isNotEmpty()) {
                SectionHeader(
                    title = "Recent Transactions",
                    action = "See all",
                    onActionClick = onTransactionClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                XpenseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = onTransactionClick
                ) {
                    Column {
                        txToShow.forEachIndexed { index, transaction ->
                            LatestTransactionItem(transaction = transaction)
                            if (index < txToShow.lastIndex) {
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

            Spacer(modifier = Modifier.height(28.dp))

            // ─── This Month: Pie Chart + Category Bars ───
            if (statisticsDoc != null || topCategories.isNotEmpty()) {
                SectionHeader(
                    title = "This Month",
                    action = "Details",
                    onActionClick = onPieChartClick,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                XpenseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = onPieChartClick
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Pie chart
                        if (statisticsDoc != null) {
                            XpenseDonutChart(
                                categories = statisticsDoc.amountByCategory,
                                totalSpent = statisticsDoc.totalAmountSpent ?: 0.0,
                                currency = wallet?.currency ?: "$",
                                isCompact = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Top category bars
                        val maxAmount = topCategories.maxOfOrNull { it.second } ?: 1.0
                        topCategories.forEach { (catName, amount) ->
                            val visual = CategoryIconMapper.get(catName)
                            CategoryBar(
                                icon = visual.icon,
                                iconColor = visual.color,
                                iconBgColor = visual.containerColor,
                                name = catName,
                                amount = "${wallet?.currency ?: "$"}${String.format("%.0f", amount)}",
                                fraction = (amount / maxAmount).toFloat(),
                                barColor = visual.color
                            )
                        }
                    }
                }
            }

            // Bottom spacer for FAB
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/**
 * A single compact insight row: small icon • label • value aligned to the right.
 */
@Composable
private fun QuickInsightRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockWallet = Wallet().apply {
        id = "1"; title = "Personal"; currency = "$"; amount = 1250.50
    }
    val mockTransaction = Transaction().apply {
        id = "1"; userName = "John Doe"; amount = 50.0; currency = "$"
        date = java.util.Date(); category = "Groceries"; type = Transaction.EXPENSE_TYPE
        title = "Weekly groceries"
    }
    val mockTx2 = Transaction().apply {
        id = "2"; userName = "Uber"; amount = 12.0; currency = "$"
        date = java.util.Date(); category = "Transport"; type = Transaction.EXPENSE_TYPE
        title = "Uber ride"
    }
    val mockTx3 = Transaction().apply {
        id = "3"; amount = 3000.0; currency = "$"
        date = java.util.Date(); category = "Income"; type = Transaction.INCOME_TYPE
        title = "Salary"
    }

    XpenseTheme(dynamicColor = false) {
        HomeScreen(
            wallet = mockWallet,
            latestTransaction = mockTransaction,
            recentTransactions = listOf(mockTransaction, mockTx2, mockTx3),
            barChartTransactions = listOf(mockTransaction),
            statisticsDoc = null,
            spentMostItems = emptyList(),
            bankBalance = null,
            bankCurrency = null,
            monthlySpent = 340.20,
            monthlyIncome = 3000.0,
            topCategories = listOf("Groceries" to 150.0, "Transport" to 80.0, "Bills" to 60.0),
            spendingChangePercent = 12.5,
            spendingTrendUp = true,
            biggestTransaction = mockTransaction,
            savingsRate = 88.7,
            dailyAverage = 24.3,
            onWalletClick = {},
            onAdjustBalanceClick = {},
            onAddBankClick = {},
            onFabClick = {},
            onBarChartClick = {},
            onPieChartClick = {},
            onTransactionClick = {},
            onSpentClick = {},
            onEarnedClick = {},
            onSettingsClick = {}
        )
    }
}
