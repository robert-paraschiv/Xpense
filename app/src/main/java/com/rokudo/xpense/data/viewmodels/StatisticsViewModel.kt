package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.StatisticsRepo
import com.rokudo.xpense.models.StatisticsDoc
import java.util.Date

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {
    private val statisticsRepo = StatisticsRepo.instance

    fun getStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.storedStatisticsDoc

    fun listenForStatisticsDoc(walletId: String, date: Date): MutableLiveData<StatisticsDoc> =
        statisticsRepo.listenForStatisticsDoc(walletId, date)

    fun loadStatisticsDoc(walletId: String, date: Date, isYearSelected: Boolean): MutableLiveData<StatisticsDoc> =
        statisticsRepo.loadStatisticsDoc(walletId, date, isYearSelected)

    fun getHomeStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.homeStatisticsDoc

    fun setHomeStoredStatisticsDoc(doc: StatisticsDoc?) {
        statisticsRepo.setHomeStoredStatisticsDoc(doc)
    }

    fun setAnalyticsStoredStatisticsDoc(doc: StatisticsDoc?) {
        statisticsRepo.setAnalyticsStoredStatisticsDoc(doc)
    }

    fun getAnalyticsStoredStatisticsDoc(): StatisticsDoc? = statisticsRepo.analyticsStoredDoc
}

