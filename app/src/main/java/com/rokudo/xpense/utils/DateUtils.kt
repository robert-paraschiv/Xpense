package com.rokudo.xpense.utils

import com.rokudo.xpense.models.TransEntry
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

object DateUtils {
    val monthYearFormat = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun getLast7Days(dayOfMonthFormat: SimpleDateFormat): List<TransEntry> {
        val transEntryList = mutableListOf<TransEntry>()
        val calendar = Calendar.getInstance().apply {
            set(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH), 0, 0)
        }
        val today = calendar.time

        for (i in 0 until 7) {
            val date = Date(today.time - Duration.ofDays(i.toLong()).toMillis())
            transEntryList.add(TransEntry(dayOfMonthFormat.format(date), date, 0f))
        }
        return transEntryList
    }
}

