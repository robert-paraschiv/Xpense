package com.rokudo.xpense.fragments

import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.rokudo.xpense.R
import com.rokudo.xpense.components.AnimatedAmountText
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.TransEntry
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.IncomeGreen
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.AnalyticsBarUtils
import com.rokudo.xpense.utils.PieChartUtils
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    currency: String,
    totalSpent: Double,
    isYearMode: Boolean,
    selectedDate: String,
    availableDates: List<String>,
    categories: List<ExpenseCategory>,
    transactions: List<Transaction>,
    transEntryList: List<TransEntry>,
    categoryAmounts: Map<String, Double>,
    showBarChart: Boolean,
    onBackClick: () -> Unit,
    onToggleChart: () -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onDateSelected: (String) -> Unit,
    onCategoryClick: (ExpenseCategory) -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(
                title = "Analytics",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onToggleChart) {
                        Icon(
                            painter = painterResource(
                                id = if (showBarChart) R.drawable.baseline_pie_chart_24
                                else R.drawable.baseline_bar_chart_24
                            ),
                            contentDescription = "Toggle Chart"
                        )
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
            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isYearMode,
                    onClick = { onToggleMode(false) },
                    label = { Text("This Month") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                FilterChip(
                    selected = isYearMode,
                    onClick = { onToggleMode(true) },
                    label = { Text("This Year") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Date Selector
            AnimatedContent(
                targetState = showDatePicker,
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                },
                label = "date_picker"
            ) { expanded ->
                if (expanded) {
                    XpenseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableDates) { date ->
                                FilterChip(
                                    selected = date == selectedDate,
                                    onClick = {
                                        onDateSelected(date)
                                        showDatePicker = false
                                    },
                                    label = { Text(date) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                } else {
                    XpenseCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        onClick = { showDatePicker = true }
                    ) {
                        Text(
                            text = selectedDate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Spent
            XpenseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AnimatedAmountText(
                        amount = "$currency ${DecimalFormat("0.00").format(totalSpent)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart
            XpenseCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Crossfade(
                    targetState = showBarChart,
                    animationSpec = tween(400),
                    label = "chart_crossfade"
                ) { isBarChart ->
                    if (isBarChart) {
                        AndroidView(
                            factory = { context ->
                                BarChart(context).apply {
                                    AnalyticsBarUtils.setupBarChart(this, TextView(context).currentTextColor)
                                }
                            },
                            update = { chart ->
                                if (transEntryList.isNotEmpty()) {
                                    AnalyticsBarUtils.updateBarchartData(
                                        chart,
                                        ArrayList(transEntryList),
                                        TextView(chart.context).currentTextColor
                                    )
                                    chart.animateXY(500, 500)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    PieChartUtils.setupPieChart(this, TextView(context).currentTextColor, false)
                                }
                            },
                            update = { chart ->
                                if (categoryAmounts.isNotEmpty()) {
                                    PieChartUtils.updatePieChartData(
                                        chart,
                                        currency,
                                        HashMap(categoryAmounts),
                                        totalSpent,
                                        false
                                    )
                                    chart.animateXY(500, 500)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Categories or Transactions List
            AnimatedContent(
                targetState = categories.isNotEmpty(),
                transitionSpec = {
                    fadeIn(tween(300)).togetherWith(fadeOut(tween(200)))
                },
                label = "list_content"
            ) { showCategories ->
                if (showCategories) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Spending by Category",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                CategoryItem(
                                    category = category,
                                    currency = currency,
                                    onClick = { onCategoryClick(category) }
                                )
                            }
                        }
                    }
                } else if (transactions.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Transactions",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
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

@Composable
fun CategoryItem(
    category: ExpenseCategory,
    currency: String,
    onClick: () -> Unit
) {
    XpenseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = category.resourceId),
                contentDescription = category.name,
                tint = Color(category.color),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${category.transactionList?.size ?: 0} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$currency ${DecimalFormat("0.00").format(category.amount ?: 0.0)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    val mockCategories = listOf(
        ExpenseCategory("Food").apply {
            amount = 450.0
            transactionList = listOf(Transaction(), Transaction())
        },
        ExpenseCategory("Transport").apply {
            amount = 200.0
            transactionList = listOf(Transaction())
        }
    )

    XpenseTheme(dynamicColor = false) {
        AnalyticsScreen(
            currency = "$",
            totalSpent = 1500.0,
            isYearMode = false,
            selectedDate = "Mar 2024",
            availableDates = listOf("Jan 2024", "Feb 2024", "Mar 2024"),
            categories = mockCategories,
            transactions = emptyList(),
            transEntryList = emptyList(),
            categoryAmounts = mapOf("Food" to 450.0, "Transport" to 200.0),
            showBarChart = false,
            onBackClick = {},
            onToggleChart = {},
            onToggleMode = {},
            onDateSelected = {},
            onCategoryClick = {},
            onTransactionClick = {}
        )
    }
}
