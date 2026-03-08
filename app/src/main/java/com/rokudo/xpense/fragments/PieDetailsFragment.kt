package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.rokudo.xpense.components.SectionHeader
import com.rokudo.xpense.components.TransactionRow
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.CategoryIconMapper
import com.rokudo.xpense.utils.PieChartUtils
import java.text.DecimalFormat
import java.util.*

class PieDetailsFragment : Fragment() {

    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]
        val args = PieDetailsFragmentArgs.fromBundle(requireArguments())
        val wallet = args.wallet

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                    val statisticsDoc by statisticsViewModel.listenForStatisticsDoc(wallet.id, Date()).observeAsState()
                    PieDetailsScreen(
                        wallet = wallet,
                        statisticsDoc = statisticsDoc,
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PieDetailsScreen(
    wallet: Wallet,
    statisticsDoc: StatisticsDoc?,
    onBackClick: () -> Unit
) {
    val currency = wallet.currency ?: ""
    val totalSpent = statisticsDoc?.totalAmountSpent ?: 0.0
    val categories = statisticsDoc?.amountByCategory?.entries
        ?.sortedByDescending { it.value }
        ?.toList() ?: emptyList()
    val maxAmount = categories.maxOfOrNull { it.value } ?: 1.0

    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(title = "Spending Details", onBackClick = onBackClick)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero total
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Spent", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$currency ${DecimalFormat("#,##0.00").format(totalSpent)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Pie chart
            item {
                XpenseCard(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    if (statisticsDoc != null) {
                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    PieChartUtils.setupPieChart(this, TextView(context).currentTextColor, false)
                                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                                            if (e is PieEntry) {
                                                expandedCategory = if (expandedCategory == e.label) null else e.label
                                            }
                                        }
                                        override fun onNothingSelected() {}
                                    })
                                }
                            },
                            update = { chart ->
                                PieChartUtils.updatePieChartData(chart, currency, statisticsDoc.amountByCategory, statisticsDoc.totalAmountSpent, false)
                            },
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No data yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Expandable category breakdown
            if (categories.isNotEmpty()) {
                item { SectionHeader(title = "Breakdown") }

                categories.forEach { (catName, amount) ->
                    item(key = "pie_cat_$catName") {
                        PieExpandableCategoryCard(
                            catName = catName,
                            amount = amount,
                            maxAmount = maxAmount,
                            currency = currency,
                            isExpanded = expandedCategory == catName,
                            transactions = statisticsDoc?.categories?.get(catName)?.values?.toList()
                                ?.sortedByDescending { it.dateLong } ?: emptyList(),
                            onToggle = {
                                expandedCategory = if (expandedCategory == catName) null else catName
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PieExpandableCategoryCard(
    catName: String,
    amount: Double,
    maxAmount: Double,
    currency: String,
    isExpanded: Boolean,
    transactions: List<Transaction>,
    onToggle: () -> Unit
) {
    val visual = CategoryIconMapper.get(catName)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(250),
        label = "pie_chevron_$catName"
    )
    val animatedFraction by animateFloatAsState(
        targetValue = (amount / maxAmount).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "pie_bar_$catName"
    )

    XpenseCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(visual.containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(visual.icon, catName, tint = visual.color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(catName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${transactions.size} transactions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "$currency${DecimalFormat("#,##0").format(amount)}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = visual.color
                )
                Icon(
                    Icons.Filled.KeyboardArrowDown, if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(24.dp).rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Progress bar
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = if (isExpanded) 0.dp else 10.dp)
            ) {
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.extraSmall).background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        Modifier.fillMaxHeight().fillMaxWidth(animatedFraction).clip(MaterialTheme.shapes.extraSmall).background(visual.color)
                    )
                }
            }

            // Expanded transactions
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                exit = shrinkVertically(tween(250)) + fadeOut(tween(200))
            ) {
                if (transactions.isNotEmpty()) {
                    Column {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        transactions.forEachIndexed { idx, tx ->
                            TransactionRow(transaction = tx)
                            if (idx < transactions.lastIndex) {
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
