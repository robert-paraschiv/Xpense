package com.rokudo.xpense.data.repositories

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.rokudo.xpense.models.StatisticsDoc
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.DatabaseUtils
import java.text.SimpleDateFormat
import java.util.*

class StatisticsRepo {
    companion object {
        val instance by lazy { StatisticsRepo() }
    }

    private var statisticsDocListener: ListenerRegistration? = null
    var storedStatisticsDoc: StatisticsDoc? = null
        private set
    var homeStatisticsDoc: StatisticsDoc? = null
    var analyticsStoredDoc: StatisticsDoc? = null

    private val statisticsLiveData = MutableLiveData<StatisticsDoc>()
    private var walletId: String? = null
    private var storedDate: String? = null

    fun loadStatisticsDoc(wallet: String, date: Date, isYearStatisticsDoc: Boolean): MutableLiveData<StatisticsDoc> {
        val result = MutableLiveData<StatisticsDoc>()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)

        if (isYearStatisticsDoc) {
            DatabaseUtils.getYearReference(wallet, year)
                .collection("Months")
                .get()
                .addOnSuccessListener { snapshots ->
                    if (snapshots.isEmpty) return@addOnSuccessListener
                    val yearDoc = StatisticsDoc(
                        totalAmountSpent = 0.0,
                        categories = mutableMapOf(),
                        amountByCategory = mutableMapOf(),
                        transactions = mutableMapOf()
                    )
                    for (doc in snapshots) {
                        val monthDoc = doc.toObject(StatisticsDoc::class.java) ?: continue
                        yearDoc.totalAmountSpent = (yearDoc.totalAmountSpent ?: 0.0) + (monthDoc.totalAmountSpent ?: 0.0)
                        monthDoc.transactions?.let { yearDoc.transactions?.putAll(it) }
                        monthDoc.categories?.forEach { (key, value) ->
                            if (yearDoc.categories!!.containsKey(key)) {
                                yearDoc.categories!![key]?.putAll(value)
                            } else {
                                yearDoc.categories!![key] = value
                            }
                        }
                        monthDoc.amountByCategory?.forEach { (category, amount) ->
                            yearDoc.amountByCategory!![category] =
                                (yearDoc.amountByCategory!![category] ?: 0.0) + amount
                        }
                    }
                    result.value = yearDoc
                }
        } else {
            DatabaseUtils.getMonthsReference(wallet, year)
                .document(month)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val statisticsDoc = documentSnapshot?.toObject(StatisticsDoc::class.java)
                        ?: return@addOnSuccessListener
                    result.value = statisticsDoc
                }
        }
        return result
    }

    fun listenForStatisticsDoc(wallet: String, date: Date): MutableLiveData<StatisticsDoc> {
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(date)

        if (statisticsDocListener == null
            || walletId == null || walletId != wallet
            || storedDate == null || storedDate != year + month
        ) {
            statisticsDocListener?.remove()
            statisticsLiveData.value = null
            walletId = wallet
            storedDate = year + month
            statisticsDocListener = DatabaseUtils.getMonthsReference(wallet, year)
                .document(month)
                .addSnapshotListener { value, _ ->
                    if (value == null) return@addSnapshotListener
                    val statisticsDoc = value.toObject(StatisticsDoc::class.java)
                        ?: return@addSnapshotListener
                    statisticsDoc.docPath = value.reference.path
                    statisticsLiveData.postValue(statisticsDoc)
                    storedStatisticsDoc = statisticsDoc
                }
        }
        return statisticsLiveData
    }

    fun setHomeStoredStatisticsDoc(doc: StatisticsDoc?) {
        homeStatisticsDoc = doc
    }

    fun setAnalyticsStoredStatisticsDoc(doc: StatisticsDoc?) {
        analyticsStoredDoc = doc
    }
}

