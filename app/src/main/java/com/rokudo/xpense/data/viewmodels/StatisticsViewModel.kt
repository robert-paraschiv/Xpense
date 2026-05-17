package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.api.models.Anomaly
import com.rokudo.xpense.data.api.models.CategoryForecast
import com.rokudo.xpense.data.api.models.CompareResponse
import com.rokudo.xpense.data.api.models.ForecastResponse
import com.rokudo.xpense.data.api.models.MtdProjectionResponse
import com.rokudo.xpense.data.api.models.RecurringTransaction
import com.rokudo.xpense.data.api.models.SpendingHistoryResponse
import com.rokudo.xpense.data.repositories.StatisticsRepo
import com.rokudo.xpense.models.StatisticsDoc
import java.util.Date

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val statisticsRepo = StatisticsRepo.instance

    fun getStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.storedStatisticsDoc

    fun listenForStatisticsDoc(walletId: String, date: Date): MutableLiveData<StatisticsDoc?> =
        statisticsRepo.listenForStatisticsDoc(walletId, date)

    fun loadStatisticsDoc(walletId: String, date: Date, isYearSelected: Boolean): MutableLiveData<StatisticsDoc> =
        statisticsRepo.loadStatisticsDoc(walletId, date, isYearSelected)

    fun refreshStatistics(walletId: String, date: Date) =
        statisticsRepo.refreshStatistics(walletId, date)

    fun getHomeStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.homeStatisticsDoc

    fun setHomeStoredStatisticsDoc(doc: StatisticsDoc?) {
        statisticsRepo.setHomeStoredStatisticsDoc(doc)
    }

    fun setAnalyticsStoredStatisticsDoc(doc: StatisticsDoc?) {
        statisticsRepo.setAnalyticsStoredStatisticsDoc(doc)
    }

    fun getAnalyticsStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.analyticsStoredDoc

    // ── Prediction / Analytics API ──

    fun getCompareStatistics(
        walletId: String,
        year: Int? = null,
        month: Int? = null,
        type: String = "MONTHLY"
    ): MutableLiveData<CompareResponse?> =
        statisticsRepo.getCompareStatistics(walletId, year, month, type)

    fun getNextMonthForecast(walletId: String): MutableLiveData<ForecastResponse?> =
        statisticsRepo.getNextMonthForecast(walletId)

    fun getEndOfMonthProjection(walletId: String): MutableLiveData<MtdProjectionResponse?> =
        statisticsRepo.getEndOfMonthProjection(walletId)

    fun getCategoryForecasts(): MutableLiveData<List<CategoryForecast>?> =
        statisticsRepo.getCategoryForecasts()

    fun getRecurringTransactions(): MutableLiveData<List<RecurringTransaction>?> =
        statisticsRepo.getRecurringTransactions()

    fun getAnomalies(): MutableLiveData<List<Anomaly>?> =
        statisticsRepo.getAnomalies()

    fun getSpendingHistory(
        walletId: String,
        periodType: String = "MONTHLY",
        months: Int = 12
    ): MutableLiveData<SpendingHistoryResponse?> =
        statisticsRepo.getSpendingHistory(walletId, periodType, months)
}
