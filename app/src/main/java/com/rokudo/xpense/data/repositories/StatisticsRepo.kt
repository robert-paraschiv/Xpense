package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.api.ApiServiceProvider
import com.rokudo.xpense.data.api.models.Anomaly
import com.rokudo.xpense.data.api.models.CategoryForecast
import com.rokudo.xpense.data.api.models.CompareResponse
import com.rokudo.xpense.data.api.models.ForecastResponse
import com.rokudo.xpense.data.api.models.MtdProjectionResponse
import com.rokudo.xpense.data.api.models.RecurringTransaction
import com.rokudo.xpense.data.api.models.SpendingHistoryResponse
import com.rokudo.xpense.data.api.models.StatisticsResponse
import com.rokudo.xpense.data.api.models.YearlyStatisticsResponse
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsRepo {
    companion object {
        private const val TAG = "StatisticsRepo"
        val instance by lazy { StatisticsRepo() }
    }

    var storedStatisticsDoc: StatisticsDoc? = null
        private set
    var homeStatisticsDoc: StatisticsDoc? = null
    var analyticsStoredDoc: StatisticsDoc? = null

    private val statisticsLiveData = MutableLiveData<StatisticsDoc?>()
    private var walletId: String? = null
    private var storedDate: String? = null

    /**
     * Load statistics for a wallet in a given month/year.
     * Uses the backend /statistics endpoint directly.
     */
    fun loadStatisticsDoc(wallet: String, date: Date, isYearStatisticsDoc: Boolean): MutableLiveData<StatisticsDoc> {
        val result = MutableLiveData<StatisticsDoc>()
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isYearStatisticsDoc) {
                    val response = ApiServiceProvider.statisticsService.getYearlyStatistics(wallet, year)
                    if (response.isSuccessful) {
                        val yearlyStats = response.body()
                        result.postValue(buildDocFromYearlyResponse(yearlyStats))
                    } else {
                        // Fallback to spending history if yearly stats endpoint not available
                        val fallback = ApiServiceProvider.predictionService.getSpendingHistory(wallet, "MONTHLY", 12)
                        if (fallback.isSuccessful) {
                            result.postValue(buildDocFromSpendingHistory(fallback.body()))
                        }
                    }
                } else {
                    val response = ApiServiceProvider.statisticsService.getMonthlyStatistics(wallet, year, month)
                    if (response.isSuccessful) {
                        val stats = response.body()
                        result.postValue(buildDocFromStatisticsResponse(stats))
                    } else {
                        // Fallback: fetch all transactions and compute locally
                        val fallback = ApiServiceProvider.transactionService.getWalletTransactions(wallet)
                        if (fallback.isSuccessful) {
                            val transactions = fallback.body() ?: emptyList()
                            result.postValue(buildStatisticsDocFromTransactions(transactions, date))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadStatisticsDoc: ${e.message}", e)
            }
        }
        return result
    }

    /**
     * Fetch statistics for the home screen. Uses backend /statistics endpoint.
     */
    fun listenForStatisticsDoc(wallet: String, date: Date): MutableLiveData<StatisticsDoc?> {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)

        if (walletId == null || walletId != wallet
            || storedDate == null || storedDate != year + month
        ) {
            statisticsLiveData.value = null
            walletId = wallet
            storedDate = year + month

            val calendar = Calendar.getInstance().apply { time = date }
            val yearInt = calendar.get(Calendar.YEAR)
            val monthInt = calendar.get(Calendar.MONTH) + 1

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiServiceProvider.statisticsService.getMonthlyStatistics(wallet, yearInt, monthInt)
                    if (response.isSuccessful) {
                        val doc = buildDocFromStatisticsResponse(response.body())
                        statisticsLiveData.postValue(doc)
                        storedStatisticsDoc = doc
                    } else {
                        // Fallback: fetch all transactions and compute locally
                        val fallback = ApiServiceProvider.transactionService.getWalletTransactions(wallet)
                        if (fallback.isSuccessful) {
                            val transactions = fallback.body() ?: emptyList()
                            val doc = buildStatisticsDocFromTransactions(transactions, date)
                            statisticsLiveData.postValue(doc)
                            storedStatisticsDoc = doc
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "listenForStatisticsDoc: ${e.message}", e)
                }
            }
        }
        return statisticsLiveData
    }

    /**
     * Force refresh statistics for the current wallet/date.
     */
    fun refreshStatistics(wallet: String, date: Date) {
        walletId = null
        storedDate = null
        listenForStatisticsDoc(wallet, date)
    }

    /**
     * Get comparison statistics between current and previous period.
     */
    fun getCompareStatistics(
        walletId: String,
        year: Int? = null,
        month: Int? = null,
        type: String = "MONTHLY"
    ): MutableLiveData<CompareResponse?> {
        val data = MutableLiveData<CompareResponse?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.statisticsService.getCompareStatistics(walletId, year, month, type)
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getCompareStatistics: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun setHomeStoredStatisticsDoc(doc: StatisticsDoc?) {
        homeStatisticsDoc = doc
    }

    fun setAnalyticsStoredStatisticsDoc(doc: StatisticsDoc?) {
        analyticsStoredDoc = doc
    }

    // ── Prediction / Analytics helpers ──

    fun getNextMonthForecast(walletId: String): MutableLiveData<ForecastResponse?> {
        val data = MutableLiveData<ForecastResponse?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getNextMonthForecast(walletId)
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getNextMonthForecast: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun getEndOfMonthProjection(walletId: String): MutableLiveData<MtdProjectionResponse?> {
        val data = MutableLiveData<MtdProjectionResponse?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getEndOfMonthProjection(walletId)
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getEndOfMonthProjection: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun getCategoryForecasts(): MutableLiveData<List<CategoryForecast>?> {
        val data = MutableLiveData<List<CategoryForecast>?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getCategoryForecasts()
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getCategoryForecasts: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun getRecurringTransactions(): MutableLiveData<List<RecurringTransaction>?> {
        val data = MutableLiveData<List<RecurringTransaction>?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getRecurringTransactions()
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getRecurringTransactions: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun getAnomalies(): MutableLiveData<List<Anomaly>?> {
        val data = MutableLiveData<List<Anomaly>?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getAnomalies()
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getAnomalies: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    fun getSpendingHistory(
        walletId: String,
        periodType: String = "MONTHLY",
        months: Int = 12
    ): MutableLiveData<SpendingHistoryResponse?> {
        val data = MutableLiveData<SpendingHistoryResponse?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.predictionService.getSpendingHistory(walletId, periodType, months)
                data.postValue(if (response.isSuccessful) response.body() else null)
            } catch (e: Exception) {
                Log.e(TAG, "getSpendingHistory: ${e.message}", e)
                data.postValue(null)
            }
        }
        return data
    }

    // ── Private helpers: Build StatisticsDoc from backend responses ──

    private fun buildDocFromStatisticsResponse(stats: StatisticsResponse?): StatisticsDoc {
        if (stats == null) return StatisticsDoc()

        // Build transactions map from transactionsByCategory
        val transactionsMap = mutableMapOf<String, Transaction>()
        val categoriesMap = mutableMapOf<String, MutableMap<String, Transaction>>()

        stats.transactionsByCategory?.forEach { (category, txList) ->
            val catMap = mutableMapOf<String, Transaction>()
            txList.forEach { tx ->
                val id = tx.id ?: UUID.randomUUID().toString()
                transactionsMap[id] = tx
                catMap[id] = tx
            }
            categoriesMap[category] = catMap
        }

        return StatisticsDoc(
            totalAmountSpent = stats.totalAmountSpent,
            amountByCategory = stats.amountByCategory?.toMutableMap(),
            categories = categoriesMap,
            transactions = transactionsMap
        )
    }

    private fun buildDocFromYearlyResponse(stats: YearlyStatisticsResponse?): StatisticsDoc {
        if (stats == null) return StatisticsDoc()

        val transactionsMap = mutableMapOf<String, Transaction>()
        val categoriesMap = mutableMapOf<String, MutableMap<String, Transaction>>()

        stats.transactionsByCategory?.forEach { (category, txList) ->
            val catMap = mutableMapOf<String, Transaction>()
            txList.forEach { tx ->
                val id = tx.id ?: UUID.randomUUID().toString()
                transactionsMap[id] = tx
                catMap[id] = tx
            }
            categoriesMap[category] = catMap
        }

        return StatisticsDoc(
            totalAmountSpent = stats.totalAmountSpent,
            amountByCategory = stats.amountByCategory?.toMutableMap(),
            categories = categoriesMap,
            transactions = transactionsMap
        )
    }

    private fun buildDocFromSpendingHistory(history: SpendingHistoryResponse?): StatisticsDoc {
        val totalSpent = history?.data?.sumOf { it.totalExpense ?: 0.0 } ?: 0.0
        return StatisticsDoc(
            totalAmountSpent = totalSpent,
            amountByCategory = mutableMapOf(),
            categories = mutableMapOf(),
            transactions = mutableMapOf()
        )
    }

    /**
     * Fallback: Build statistics locally from raw transactions when backend endpoints are unavailable.
     */
    private fun buildStatisticsDocFromTransactions(transactions: List<Transaction>, date: Date): StatisticsDoc {
        val calendar = Calendar.getInstance().apply { time = date }
        val targetMonth = calendar.get(Calendar.MONTH)
        val targetYear = calendar.get(Calendar.YEAR)

        val monthTransactions = transactions.filter { t ->
            val cal = Calendar.getInstance().apply { time = t.date ?: Date(0) }
            cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
        }

        val totalSpent = monthTransactions
            .filter { it.type?.equals("Expense", ignoreCase = true) == true || it.type?.equals("EXPENSE", ignoreCase = true) == true }
            .sumOf { it.amount ?: 0.0 }

        val amountByCategory = mutableMapOf<String, Double>()
        val categories = mutableMapOf<String, MutableMap<String, Transaction>>()
        val transactionsMap = mutableMapOf<String, Transaction>()

        for (t in monthTransactions) {
            val category = t.category ?: "Other"
            val id = t.id ?: UUID.randomUUID().toString()

            transactionsMap[id] = t

            if (t.type?.equals("Expense", ignoreCase = true) == true || t.type?.equals("EXPENSE", ignoreCase = true) == true) {
                amountByCategory[category] = (amountByCategory[category] ?: 0.0) + (t.amount ?: 0.0)
            }

            if (!categories.containsKey(category)) {
                categories[category] = mutableMapOf()
            }
            categories[category]!![id] = t
        }

        return StatisticsDoc(
            totalAmountSpent = totalSpent,
            amountByCategory = amountByCategory,
            categories = categories,
            transactions = transactionsMap
        )
    }
}
