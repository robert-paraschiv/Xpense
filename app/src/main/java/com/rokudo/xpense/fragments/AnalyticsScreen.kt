package com.rokudo.xpense.fragments

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.rokudo.xpense.R
import com.rokudo.xpense.components.LatestTransactionItem
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.TransEntry
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.AnalyticsBarUtils
import com.rokudo.xpense.utils.PieChartUtils
import java.text.DecimalFormat
import java.util.*

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
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
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
            // Mode Selector (Month/Year)
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
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isYearMode,
                    onClick = { onToggleMode(true) },
                    label = { Text("This Year") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Date Selector
            if (showDatePicker) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
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
                                label = { Text(date) }
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
                ) {
                    Text(
                        text = selectedDate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total Spent
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Spent", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        "$currency ${DecimalFormat("0.00").format(totalSpent)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
            ) {
                if (showBarChart) {
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

            Spacer(modifier = Modifier.height(8.dp))

            // Categories or Transactions List
            if (categories.isNotEmpty()) {
                Text(
                    "Spending by Category",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
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
            } else if (transactions.isNotEmpty()) {
                Text(
                    "Transactions",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
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

@Composable
fun CategoryItem(
    category: ExpenseCategory,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
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
                Text(category.name, fontWeight = FontWeight.Bold)
                Text(
                    "${category.transactionList?.size ?: 0} transactions",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                "$currency ${DecimalFormat("0.00").format(category.amount ?: 0.0)}",
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

