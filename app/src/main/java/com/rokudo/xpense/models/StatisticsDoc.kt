package com.rokudo.xpense.models

import java.util.Date

data class StatisticsDoc(
    var totalAmountSpent: Double? = null,
    var amountByCategory: MutableMap<String, Double>? = null,
    var categories: MutableMap<String, MutableMap<String, Transaction>>? = null,
    var transactions: MutableMap<String, Transaction>? = null,
    var transactionsByDay: Map<String, Map<String, Transaction>>? = null,
    var docPath: String? = null,
    var latestUpdateTime: Date? = null
)
