package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.rokudo.xpense.components.SectionHeader
import com.rokudo.xpense.components.TransactionRow
import com.rokudo.xpense.components.XpenseCard
import com.rokudo.xpense.components.XpenseTopAppBar
import com.rokudo.xpense.data.viewmodels.TransactionViewModel
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.ui.theme.XpenseTheme
import com.rokudo.xpense.utils.BarChartUtils
import java.util.*

class BarDetailsFragment : Fragment() {

    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        transactionViewModel = ViewModelProvider(requireActivity())[TransactionViewModel::class.java]
        val args = BarDetailsFragmentArgs.fromBundle(requireArguments())
        val wallet = args.wallet

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                    val (startDate, endDate) = remember { calculateLast7Days() }
                    val transactions by transactionViewModel.loadTransactionsDateInterval(wallet.id, startDate, endDate).observeAsState(emptyList())
                    BarDetailsScreen(
                        wallet = wallet,
                        transactions = transactions,
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }

    private fun calculateLast7Days(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.time
        return Pair(start, end)
    }
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarDetailsScreen(
    wallet: Wallet,
    transactions: List<Transaction>,
    onBackClick: () -> Unit
) {
    val sortedTransactions = remember(transactions) {
        transactions.sortedByDescending { it.date }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            XpenseTopAppBar(title = "Spending History", onBackClick = onBackClick)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─── Bar Chart Card ───
            item {
                XpenseCard(
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            BarChart(context).apply {
                                BarChartUtils.setupBarChart(this, TextView(context).currentTextColor, false)
                            }
                        },
                        update = { chart ->
                            if (transactions.isNotEmpty()) {
                                BarChartUtils.updateBarchartData(
                                    chart, ArrayList(transactions), TextView(chart.context).currentTextColor
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize().padding(12.dp)
                    )
                }
            }

            // ─── Transactions in one card ───
            item {
                SectionHeader(title = "Last 7 Days")
            }

            item {
                if (sortedTransactions.isNotEmpty()) {
                    XpenseCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            sortedTransactions.forEachIndexed { index, tx ->
                                TransactionRow(transaction = tx)
                                if (index < sortedTransactions.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 68.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions in the last 7 days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
