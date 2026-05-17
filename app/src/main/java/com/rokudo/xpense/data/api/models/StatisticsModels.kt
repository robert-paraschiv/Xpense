package com.rokudo.xpense.data.api.models

import com.google.gson.annotations.SerializedName
import com.rokudo.xpense.models.Transaction

// ── Monthly Statistics Response ──

data class StatisticsResponse(
    val walletId: String?,
    val year: Int?,
    val month: Int?,
    val totalAmountSpent: Double?,
    val totalIncome: Double?,
    val transactionCount: Int?,
    val amountByCategory: Map<String, Double>?,
    val transactionsByCategory: Map<String, List<Transaction>>?,
    val dailyTotals: Map<String, DailyTotal>?,
    val dailyAverage: Double?,
    val biggestExpense: Transaction?,
    val peakSpendDay: String?,
    val savingsRate: Double?
)

data class DailyTotal(
    val expense: Double?,
    val income: Double?,
    val count: Int?
)

// ── Yearly Statistics Response ──

data class YearlyStatisticsResponse(
    val walletId: String?,
    val year: Int?,
    val totalAmountSpent: Double?,
    val totalIncome: Double?,
    val transactionCount: Int?,
    val amountByCategory: Map<String, Double>?,
    val monthlyBreakdown: List<MonthlyBreakdown>?,
    val transactionsByCategory: Map<String, List<Transaction>>?,
    val dailyAverage: Double?,
    val biggestExpense: Transaction?
)

data class MonthlyBreakdown(
    val month: Int?,
    val totalExpense: Double?,
    val totalIncome: Double?,
    val transactionCount: Int?
)

// ── Compare Response ──

data class CompareResponse(
    val walletId: String?,
    val currentPeriod: PeriodSummary?,
    val previousPeriod: PeriodSummary?,
    val spendingChangePercent: Double?,
    val spendingTrendUp: Boolean?,
    val categoryChanges: List<CategoryChange>?
)

data class PeriodSummary(
    val totalExpense: Double?,
    val totalIncome: Double?,
    val transactionCount: Int?
)

data class CategoryChange(
    val category: String?,
    val currentAmount: Double?,
    val previousAmount: Double?,
    val changePercent: Double?,
    val trendUp: Boolean?
)

