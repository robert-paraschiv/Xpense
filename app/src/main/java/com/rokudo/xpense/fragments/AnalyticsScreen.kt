package com.rokudo.xpense.fragments

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rokudo.xpense.R
import com.rokudo.xpense.components.*
import com.rokudo.xpense.components.charts.XpenseBarChart
import com.rokudo.xpense.components.charts.XpenseDonutChart
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.TransEntry
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.ui.theme.*
import com.rokudo.xpense.utils.CategoryIconMapper
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
    transEntryList: List<TransEntry>,
    categoryAmounts: Map<String, Double>,
    showBarChart: Boolean,
    onBackClick: () -> Unit,
    onToggleChart: () -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onDateSelected: (String) -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onBarClick: (Int) -> Unit = {}
) {
    val currentDateIndex = availableDates.indexOf(selectedDate)
    // Track which category is expanded — null means none
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // If selected date is not in the list, auto-select the most recent date
    LaunchedEffect(currentDateIndex, availableDates) {
        if (currentDateIndex == -1 && availableDates.isNotEmpty()) {
            onDateSelected(availableDates.first())
        }
    }

    // Reset expansion when date or mode changes
    LaunchedEffect(selectedDate, isYearMode) {
        expandedCategory = null
    }

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
                            contentDescription = if (showBarChart) "Switch to Pie Chart" else "Switch to Bar Chart"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Mode Toggle (Segmented) ───
            item(key = "mode_toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(false to "Monthly", true to "Yearly").forEach { (mode, label) ->
                        val selected = isYearMode == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.small,
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { onToggleMode(mode) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ─── Date Navigator ───
            // availableDates is ordered most-recent-first (reversed),
            // so higher index = older date. Left arrow = older, Right arrow = newer.
            item(key = "date_nav") {
                val canGoOlder = currentDateIndex >= 0 && currentDateIndex < availableDates.lastIndex
                val canGoNewer = currentDateIndex > 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (canGoOlder) onDateSelected(availableDates[currentDateIndex + 1])
                        },
                        enabled = canGoOlder
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous (older)",
                            tint = if (canGoOlder) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    Text(
                        text = if (currentDateIndex >= 0) selectedDate else availableDates.firstOrNull() ?: selectedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = {
                            if (canGoNewer) onDateSelected(availableDates[currentDateIndex - 1])
                        },
                        enabled = canGoNewer
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next (newer)",
                            tint = if (canGoNewer) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ─── Total Spent ───
            item(key = "total") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currency ${DecimalFormat("#,##0.00").format(totalSpent)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // ─── Chart Card ───
            item(key = "chart") {
                XpenseCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Crossfade(
                        targetState = showBarChart,
                        animationSpec = tween(400),
                        label = "chart_crossfade"
                    ) { isBarChart ->
                        if (isBarChart) {
                            XpenseBarChart(
                                entries = transEntryList,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            XpenseDonutChart(
                                categories = categoryAmounts,
                                totalSpent = totalSpent,
                                currency = currency,
                                isCompact = false,
                                selectedSlice = expandedCategory,
                                onSliceClick = { label ->
                                    expandedCategory = if (expandedCategory == label) null else label
                                }
                            )
                        }
                    }
                }
            }

            // ─── Category Breakdown — expandable ───
            if (categories.isNotEmpty()) {
                item(key = "cat_header") {
                    SectionHeader(title = "Spending by Category")
                }

                val maxAmount = categories.maxOfOrNull { it.amount ?: 0.0 } ?: 1.0

                categories.forEach { category ->
                    item(key = "cat_${category.name}") {
                        ExpandableCategoryCard(
                            category = category,
                            currency = currency,
                            maxAmount = maxAmount,
                            isExpanded = expandedCategory == category.name,
                            onToggle = {
                                expandedCategory = if (expandedCategory == category.name) null else category.name
                            },
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ExpandableCategoryCard(
    category: ExpenseCategory,
    currency: String,
    maxAmount: Double,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val visual = CategoryIconMapper.get(category.name)
    val amount = category.amount ?: 0.0

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(250),
        label = "chevron_${category.name}"
    )
    val animatedFraction by animateFloatAsState(
        targetValue = (amount / maxAmount).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "bar_${category.name}"
    )

    XpenseCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Header row — clickable to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(visual.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = category.name,
                        tint = visual.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${category.transactionList?.size ?: 0} transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$currency${DecimalFormat("#,##0").format(amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = visual.color
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = if (isExpanded) 0.dp else 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedFraction)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(visual.color)
                    )
                }
            }

            // Expanded transactions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
            ) {
                val txList = category.transactionList
                if (!txList.isNullOrEmpty()) {
                    Column {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        txList.sortedByDescending { it.dateLong }.forEachIndexed { idx, tx ->
                            TransactionRow(
                                transaction = tx,
                                modifier = Modifier.clickable { onTransactionClick(tx) }
                            )
                            if (idx < txList.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 68.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
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
fun AnalyticsScreenPreview() {
    val mockCategories = listOf(
        ExpenseCategory("Groceries").apply {
            amount = 450.0
            transactionList = listOf(
                Transaction().apply { id = "1"; title = "Lidl"; amount = 120.0; currency = "$"; category = "Groceries"; type = Transaction.EXPENSE_TYPE; date = java.util.Date() },
                Transaction().apply { id = "2"; title = "Kaufland"; amount = 330.0; currency = "$"; category = "Groceries"; type = Transaction.EXPENSE_TYPE; date = java.util.Date() }
            )
        },
        ExpenseCategory("Transport").apply { amount = 200.0; transactionList = listOf(Transaction()) },
        ExpenseCategory("Bills").apply { amount = 150.0; transactionList = listOf(Transaction()) },
        ExpenseCategory("Restaurant").apply { amount = 120.0; transactionList = listOf(Transaction()) }
    )
    XpenseTheme(dynamicColor = false) {
        AnalyticsScreen(
            currency = "$",
            totalSpent = 920.0,
            isYearMode = false,
            selectedDate = "Mar 2026",
            availableDates = listOf("Jan 2026", "Feb 2026", "Mar 2026"),
            categories = mockCategories,
            transEntryList = emptyList(),
            categoryAmounts = mapOf("Groceries" to 450.0, "Transport" to 200.0, "Bills" to 150.0),
            showBarChart = false,
            onBackClick = {},
            onToggleChart = {},
            onToggleMode = {},
            onDateSelected = {},
            onTransactionClick = {}
        )
    }
}
