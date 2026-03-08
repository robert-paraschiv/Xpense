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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.github.mikephil.charting.charts.PieChart
import com.rokudo.xpense.R
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.PieChartUtils
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
                val statisticsDoc by statisticsViewModel.listenForStatisticsDoc(wallet.id, Date()).observeAsState()

                PieDetailsScreen(
                    wallet = wallet,
                    statisticsDoc = statisticsDoc,
                    onBackClick = {
                        findNavController().popBackStack()
                    }
                )
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Details") },
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
                        PieChart(context).apply {
                            PieChartUtils.setupPieChart(this, TextView(context).currentTextColor, true)
                        }
                    },
                    update = { chart ->
                        if (statisticsDoc != null) {
                            PieChartUtils.updatePieChartData(
                                chart,
                                wallet.currency ?: "",
                                statisticsDoc.amountByCategory,
                                statisticsDoc.totalAmountSpent,
                                true
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
                "Spending by Category",
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
                if (statisticsDoc != null && statisticsDoc.amountByCategory != null) {
                    val categories = statisticsDoc.amountByCategory.entries.toList()
                        .sortedByDescending { it.value }
                    items(categories) { (category, amount) ->
                        CategoryAmountItem(category, amount, wallet.currency ?: "")
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No data available yet", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryAmountItem(category: String, amount: Double, currency: String) {
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
            Text(category, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                String.format("%.2f %s", amount, currency),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
