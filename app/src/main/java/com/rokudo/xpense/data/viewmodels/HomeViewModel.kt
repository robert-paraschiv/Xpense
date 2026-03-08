package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rokudo.xpense.models.SpentMostItem
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.models.Wallet
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

// --- MVI Contract ---

data class HomeState(
    val wallet: Wallet? = null,
    val selectedWalletId: String = "",
    val latestTransaction: Transaction? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val barChartTransactions: List<Transaction> = emptyList(),
    val statisticsDoc: StatisticsDoc? = null,
    val spentMostItems: List<SpentMostItem> = emptyList(),
    val bankBalance: String? = null,
    val bankCurrency: String? = null,
    val monthlySpent: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val topCategories: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = true
)

sealed class HomeEvent {
    data class Init(val selectedWalletId: String) : HomeEvent()
    data class WalletLoaded(val wallet: Wallet?) : HomeEvent()
    data class LatestTransactionLoaded(val transaction: Transaction?) : HomeEvent()
    data class RecentTransactionsLoaded(val transactions: List<Transaction>) : HomeEvent()
    data class BarChartTransactionsLoaded(val transactions: List<Transaction>) : HomeEvent()
    data class StatisticsLoaded(val doc: StatisticsDoc?) : HomeEvent()
    data class BankBalanceLoaded(val balance: String?, val currency: String?) : HomeEvent()
    object WalletClicked : HomeEvent()
    object AdjustBalanceClicked : HomeEvent()
    object AddBankClicked : HomeEvent()
    object FabClicked : HomeEvent()
    object BarChartClicked : HomeEvent()
    object PieChartClicked : HomeEvent()
    object TransactionsClicked : HomeEvent()
    object SpentClicked : HomeEvent()
    object EarnedClicked : HomeEvent()
    object SettingsClicked : HomeEvent()
}

sealed class HomeEffect {
    object ShowWalletDialog : HomeEffect()
    object ShowAdjustBalanceDialog : HomeEffect()
    object NavigateToAddBank : HomeEffect()
    object NavigateToAddTransaction : HomeEffect()
    object NavigateToBarDetails : HomeEffect()
    object NavigateToPieDetails : HomeEffect()
    object NavigateToTransactions : HomeEffect()
    object NavigateToExpenses : HomeEffect()
    object NavigateToIncome : HomeEffect()
    object NavigateToSettings : HomeEffect()
    data class ShowToast(val message: String) : HomeEffect()
}

// --- ViewModel ---

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Init -> {
                _state.update { it.copy(selectedWalletId = event.selectedWalletId, isLoading = true) }
            }
            is HomeEvent.WalletLoaded -> {
                _state.update { it.copy(wallet = event.wallet, isLoading = false) }
            }
            is HomeEvent.LatestTransactionLoaded -> {
                _state.update { it.copy(latestTransaction = event.transaction) }
            }
            is HomeEvent.RecentTransactionsLoaded -> {
                val sorted = event.transactions
                    .filter { it.date != null }
                    .sortedByDescending { it.date?.time ?: 0 }
                    .take(5)
                _state.update { it.copy(recentTransactions = sorted) }
            }
            is HomeEvent.BarChartTransactionsLoaded -> {
                _state.update { it.copy(barChartTransactions = event.transactions) }
            }
            is HomeEvent.StatisticsLoaded -> {
                val doc = event.doc
                val wallet = _state.value.wallet
                val spent = doc?.totalAmountSpent ?: 0.0

                // Calculate income from transactions in the doc
                var income = 0.0
                doc?.transactions?.values?.forEach { t ->
                    if (t.type == Transaction.INCOME_TYPE) {
                        income += (t.amount ?: 0.0)
                    }
                }

                // Top 3 categories by amount
                val topCats = doc?.amountByCategory
                    ?.entries
                    ?.sortedByDescending { it.value }
                    ?.take(3)
                    ?.map { Pair(it.key, it.value) }
                    ?: emptyList()

                val spentMost = if (doc?.transactions != null && wallet != null) {
                    calculateSpentMostItems(ArrayList(doc.transactions!!.values), wallet.currency ?: "$")
                } else emptyList()

                _state.update {
                    it.copy(
                        statisticsDoc = doc,
                        spentMostItems = spentMost,
                        monthlySpent = spent,
                        monthlyIncome = income,
                        topCategories = topCats
                    )
                }
            }
            is HomeEvent.BankBalanceLoaded -> {
                _state.update { it.copy(bankBalance = event.balance, bankCurrency = event.currency) }
            }
            is HomeEvent.WalletClicked -> emitEffect(HomeEffect.ShowWalletDialog)
            is HomeEvent.AdjustBalanceClicked -> emitEffect(HomeEffect.ShowAdjustBalanceDialog)
            is HomeEvent.AddBankClicked -> emitEffect(HomeEffect.NavigateToAddBank)
            is HomeEvent.FabClicked -> emitEffect(HomeEffect.NavigateToAddTransaction)
            is HomeEvent.BarChartClicked -> emitEffect(HomeEffect.NavigateToBarDetails)
            is HomeEvent.PieChartClicked -> emitEffect(HomeEffect.NavigateToPieDetails)
            is HomeEvent.TransactionsClicked -> emitEffect(HomeEffect.NavigateToTransactions)
            is HomeEvent.SpentClicked -> emitEffect(HomeEffect.NavigateToExpenses)
            is HomeEvent.EarnedClicked -> emitEffect(HomeEffect.NavigateToIncome)
            is HomeEvent.SettingsClicked -> emitEffect(HomeEffect.NavigateToSettings)
        }
    }

    private fun emitEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    companion object {
        fun calculateSpentMostItems(transactions: List<Transaction>, currency: String): List<SpentMostItem> {
            val items = ArrayList<SpentMostItem>()
            if (transactions.isEmpty()) return items

            var maxTransaction: Transaction? = null
            val categoryMap = HashMap<String, Double>()
            val dayMap = HashMap<LocalDate, Double>()

            for (t in transactions) {
                if (t.type == Transaction.INCOME_TYPE) continue

                if (maxTransaction == null || (t.amount ?: 0.0) > (maxTransaction.amount ?: 0.0)) {
                    maxTransaction = t
                }

                val cat = t.category ?: "Unknown"
                categoryMap[cat] = (categoryMap[cat] ?: 0.0) + (t.amount ?: 0.0)

                if (t.date != null) {
                    val date = t.date!!.toInstant().atZone(ZoneOffset.UTC).toLocalDate()
                    dayMap[date] = (dayMap[date] ?: 0.0) + (t.amount ?: 0.0)
                }
            }

            val maxCategoryEntry = categoryMap.maxByOrNull { it.value }
            if (maxCategoryEntry != null) {
                items.add(SpentMostItem("Category", maxCategoryEntry.key, String.format("%.2f %s", maxCategoryEntry.value, currency), ""))
            }

            val maxDayEntry = dayMap.maxByOrNull { it.value }
            if (maxDayEntry != null) {
                val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
                items.add(SpentMostItem("Day", maxDayEntry.key.format(formatter), String.format("%.2f %s", maxDayEntry.value, currency), ""))
            }

            if (maxTransaction != null) {
                items.add(SpentMostItem("Transaction", maxTransaction.title ?: "Unknown", String.format("%.2f %s", maxTransaction.amount, currency), ""))
            }

            return items
        }
    }
}
