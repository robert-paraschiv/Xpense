package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.rokudo.xpense.R
import com.rokudo.xpense.data.viewmodels.TransactionViewModel
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
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
                val (startDate, endDate) = remember { calculateLast7Days() }
                val transactions by transactionViewModel.loadTransactionsDateInterval(wallet.id, startDate, endDate).observeAsState(emptyList())

                BarDetailsScreen(
                    wallet = wallet,
                    transactions = transactions,
                    onBackClick = {
                        findNavController().popBackStack()
                    }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarDetailsScreen(
    wallet: Wallet,
    transactions: List<Transaction>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_back_ios_24),
                            contentDescription = "Back"
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF))
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
                                chart,
                                ArrayList(transactions),
                                TextView(chart.context).currentTextColor
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Transactions (Last 7 Days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (transactions.isNotEmpty()) {
                    val sortedTransactions = transactions.sortedByDescending { it.date }
                    items(sortedTransactions) { transaction ->
                        TransactionItem(transaction, wallet.currency ?: "")
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions in the last 7 days", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currency: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF)),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.title ?: transaction.category ?: "Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    transaction.category ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                String.format("%.2f %s", transaction.amount, currency),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (transaction.type == Transaction.INCOME_TYPE)
                    Color(0xFF4CAF50)
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

