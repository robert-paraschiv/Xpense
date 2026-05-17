package com.rokudo.xpense.utils

import android.annotation.SuppressLint
import com.rokudo.xpense.models.TransEntry
import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsBarUtils {

    @SuppressLint("SimpleDateFormat")
    private val dayOfMonthFormat = SimpleDateFormat("EEE dd")

    fun getTransEntryArrayList(transactionList: List<Transaction>, isYearMode: Boolean): List<TransEntry> {
        val transEntryArrayList = mutableListOf<TransEntry>()
        for (transaction in transactionList) {
            if (transaction.type == Transaction.INCOME_TYPE) continue

            val dateString = if (isYearMode) {
                SimpleDateFormat("MMMM", Locale.getDefault()).format(transaction.date!!)
            } else {
                dayOfMonthFormat.format(transaction.date!!)
            }

            val transEntry = TransEntry(
                dateString,
                transaction.date,
                (transaction.amount ?: 0.0).toFloat()
            )

            val existingIndex = transEntryArrayList.indexOf(transEntry)
            if (existingIndex >= 0) {
                transEntryArrayList[existingIndex].amount += (transaction.amount ?: 0.0).toFloat()
            } else {
                transEntryArrayList.add(transEntry)
            }
        }
        transEntryArrayList.sortBy { it.date?.time ?: 0 }
        return transEntryArrayList
    }
}
