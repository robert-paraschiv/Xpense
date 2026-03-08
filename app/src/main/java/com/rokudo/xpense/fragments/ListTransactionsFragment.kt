package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.DateUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import com.rokudo.xpense.ui.theme.XpenseTheme

class ListTransactionsFragment : Fragment() {

    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var walletId: String
    private lateinit var currency: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        val args = ListTransactionsFragmentArgs.fromBundle(requireArguments())
        walletId = args.walletId
        currency = args.walletCurrency

        return ComposeView(requireContext()).apply {
            setContent {
                XpenseTheme {
                var selectedMonth by remember { mutableStateOf(DateUtils.monthYearFormat.format(Date())) }
                var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

                // Generate available months (from 2022-01 to current)
                val availableMonths = remember { generateAvailableMonths() }

                // Load transactions for selected month
                LaunchedEffect(selectedMonth) {
                    val date = try {
                        DateUtils.monthYearFormat.parse(selectedMonth) ?: Date()
                    } catch (e: Exception) {
                        Date()
                    }

                    statisticsViewModel.loadStatisticsDoc(walletId, date, false)
                        .observe(viewLifecycleOwner) { statsDoc ->
                            if (statsDoc != null && statsDoc.transactions != null) {
                                val sortedTransactions = statsDoc.transactions.values
                                    .sortedByDescending { it.dateLong }
                                transactions = sortedTransactions
                            } else {
                                transactions = emptyList()
                            }
                        }
                }

                // Load initial transactions from stored state
                LaunchedEffect(Unit) {
                    statisticsViewModel.storedStatisticsDoc?.let { statsDoc ->
                        if (statsDoc.transactions != null) {
                            val sortedTransactions = statsDoc.transactions.values
                                .sortedByDescending { it.dateLong }
                            transactions = sortedTransactions
                        }
                    }
                }

                ListTransactionsScreen(
                    transactions = transactions,
                    selectedMonth = selectedMonth,
                    availableMonths = availableMonths,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onMonthSelected = { month ->
                        selectedMonth = month
                    },
                    onTransactionClick = { transaction ->
                        val action = ListTransactionsFragmentDirections
                            .actionListTransactionsFragmentToAddTransactionLayout(
                                walletId,
                                currency,
                                transaction,
                                true
                            )
                        findNavController().navigate(action)
                    }
                )
                } // XpenseTheme
            }
        }
    }

    private fun generateAvailableMonths(): List<String> {
        val months = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Start from 2022, January
        calendar.set(2022, 0, 1)

        while (true) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            // Stop if we've reached the current month/year
            if (year > currentYear || (year == currentYear && month > currentMonth)) {
                break
            }

            months.add(DateUtils.monthYearFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        return months.reversed() // Show most recent first
    }
}

