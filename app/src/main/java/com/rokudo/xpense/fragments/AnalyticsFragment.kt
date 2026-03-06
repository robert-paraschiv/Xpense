package com.rokudo.xpense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel
import com.rokudo.xpense.models.ExpenseCategory
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.AnalyticsBarUtils
import com.rokudo.xpense.utils.CategoriesUtil
import com.rokudo.xpense.utils.DateUtils
import com.rokudo.xpense.utils.MapUtil
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var wallet: Wallet
    private var showBarChartInitially = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        val args = AnalyticsFragmentArgs.fromBundle(requireArguments())
        wallet = args.wallet
        showBarChartInitially = args.type == "bar"

        return ComposeView(requireContext()).apply {
            setContent {
                var isYearMode by remember { mutableStateOf(false) }
                var showBarChart by remember { mutableStateOf(showBarChartInitially) }
                var selectedDate by remember { mutableStateOf(Date()) }
                var categories by remember { mutableStateOf<List<ExpenseCategory>>(emptyList()) }
                var selectedCategoryTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

                val dateFormat = if (isYearMode) DateUtils.yearFormat else DateUtils.monthYearFormat
                val selectedDateStr = dateFormat.format(selectedDate)

                // Generate available dates
                val availableDates = remember(isYearMode) {
                    generateAvailableDates(isYearMode)
                }

                // Load statistics
                LaunchedEffect(selectedDate, isYearMode) {
                    loadStatistics(selectedDate, isYearMode)
                }

                // Observe statistics changes
                val statisticsDoc by statisticsViewModel.loadStatisticsDoc(
                    wallet.id,
                    selectedDate,
                    isYearMode
                ).observeAsState()

                // Update categories when statistics change
                LaunchedEffect(statisticsDoc) {
                    statisticsDoc?.let { doc ->
                        categories = buildCategoryList(
                            doc.amountByCategory ?: emptyMap(),
                            doc.categories ?: emptyMap()
                        )
                    } ?: run {
                        categories = emptyList()
                    }
                }

                val totalSpent = statisticsDoc?.totalAmountSpent ?: 0.0
                val transEntryList = statisticsDoc?.let { doc ->
                    val transactions = doc.transactions?.values?.toList()
                        ?.sortedByDescending { it.dateLong } ?: emptyList()
                    AnalyticsBarUtils.getTransEntryArrayList(ArrayList(transactions), isYearMode)
                } ?: emptyList()

                AnalyticsScreen(
                    currency = wallet.currency ?: "$",
                    totalSpent = totalSpent,
                    isYearMode = isYearMode,
                    selectedDate = selectedDateStr,
                    availableDates = availableDates,
                    categories = if (selectedCategoryTransactions.isEmpty()) categories else emptyList(),
                    transactions = selectedCategoryTransactions,
                    transEntryList = transEntryList,
                    categoryAmounts = statisticsDoc?.amountByCategory ?: emptyMap(),
                    showBarChart = showBarChart,
                    onBackClick = {
                        findNavController().popBackStack()
                    },
                    onToggleChart = {
                        showBarChart = !showBarChart
                    },
                    onToggleMode = { yearMode ->
                        isYearMode = yearMode
                        selectedDate = Date()
                        selectedCategoryTransactions = emptyList()
                    },
                    onDateSelected = { dateStr ->
                        try {
                            selectedDate = dateFormat.parse(dateStr) ?: Date()
                            selectedCategoryTransactions = emptyList()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    onCategoryClick = { category ->
                        selectedCategoryTransactions = category.transactionList ?: emptyList()
                    },
                    onTransactionClick = { transaction ->
                        val action = AnalyticsFragmentDirections
                            .actionAnalyticsFragmentToAddTransactionFragment(
                                wallet.id,
                                wallet.currency,
                                transaction,
                                true
                            )
                        findNavController().navigate(action)
                    }
                )
            }
        }
    }

    private fun generateAvailableDates(isYearMode: Boolean): List<String> {
        val dates = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        calendar.set(2022, 0, 1)

        val format = if (isYearMode) DateUtils.yearFormat else DateUtils.monthYearFormat

        while (true) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            if (year > currentYear || (year == currentYear && month > currentMonth)) {
                break
            }

            if (isYearMode) {
                dates.add(format.format(calendar.time))
                calendar.add(Calendar.YEAR, 1)
            } else {
                dates.add(format.format(calendar.time))
                calendar.add(Calendar.MONTH, 1)
            }
        }

        return dates.reversed()
    }

    private fun loadStatistics(date: Date, isYearMode: Boolean) {
        statisticsViewModel.loadStatisticsDoc(wallet.id, date, isYearMode)
    }

    private fun buildCategoryList(
        amountByCategory: Map<String, Double>,
        transactionsByCategory: Map<String, Map<String, Transaction>>
    ): List<ExpenseCategory> {
        val categoryList = mutableListOf<ExpenseCategory>()

        val sortedCategories = MapUtil.sortByValue(amountByCategory)

        sortedCategories.forEach { (categoryName, categoryAmount) ->
            if (categoryName == "Income" || categoryAmount == 0.0 || !transactionsByCategory.containsKey(categoryName)) {
                return@forEach
            }

            val transactions = transactionsByCategory[categoryName]?.values?.toList()
                ?.sortedByDescending { it.dateLong } ?: emptyList()

            val expenseCategory = ExpenseCategory(categoryName).apply {
                transactionList = transactions
                amount = categoryAmount
            }

            if (CategoriesUtil.expenseCategoryList.contains(expenseCategory)) {
                val matchingCategory = CategoriesUtil.expenseCategoryList[
                    CategoriesUtil.expenseCategoryList.indexOf(expenseCategory)
                ]
                expenseCategory.resourceId = matchingCategory.resourceId
                expenseCategory.color = matchingCategory.color
                categoryList.add(expenseCategory)
            }
        }

        return categoryList
    }
}

