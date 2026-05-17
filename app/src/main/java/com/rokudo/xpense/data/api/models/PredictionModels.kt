package com.rokudo.xpense.data.api.models

import com.google.gson.annotations.SerializedName

// ── Forecast (next-month) ──

data class ForecastResponse(
    val walletId: String?,
    val currency: String?,
    @SerializedName("period_start")
    val periodStart: String?,
    @SerializedName("period_end")
    val periodEnd: String?,
    @SerializedName("point_estimate")
    val pointEstimate: Double?,
    @SerializedName("lower_bound")
    val lowerBound: Double?,
    @SerializedName("upper_bound")
    val upperBound: Double?,
    @SerializedName("confidence_level")
    val confidenceLevel: Double?,
    @SerializedName("model_name")
    val modelName: String?,
    @SerializedName("data_points_used")
    val dataPointsUsed: Int?,
    val breakdown: List<CategoryForecast>?
)

// ── End-of-month projection ──

data class MtdProjectionResponse(
    val walletId: String?,
    val currency: String?,
    @SerializedName("current_month")
    val currentMonth: String?,
    @SerializedName("days_elapsed")
    val daysElapsed: Int?,
    @SerializedName("days_in_month")
    val daysInMonth: Int?,
    @SerializedName("mtd_spend")
    val mtdSpend: Double?,
    @SerializedName("naive_projection")
    val naiveProjection: Double?,
    @SerializedName("blended_projection")
    val blendedProjection: Double?,
    @SerializedName("historical_mean")
    val historicalMean: Double?,
    @SerializedName("lower_bound")
    val lowerBound: Double?,
    @SerializedName("upper_bound")
    val upperBound: Double?,
    @SerializedName("confidence_level")
    val confidenceLevel: Double?
)

// ── Category forecast ──

data class CategoryForecast(
    val category: String?,
    @SerializedName("point_estimate")
    val pointEstimate: Double?,
    @SerializedName("lower_bound")
    val lowerBound: Double?,
    @SerializedName("upper_bound")
    val upperBound: Double?,
    @SerializedName("data_points_used")
    val dataPointsUsed: Int?,
    @SerializedName("model_name")
    val modelName: String?
)

// ── Recurring transactions ──

data class RecurringTransaction(
    val id: String?,
    @SerializedName("wallet_id")
    val walletId: String?,
    val category: String?,
    val label: String?,
    @SerializedName("avg_amount")
    val avgAmount: Double?,
    val currency: String?,
    @SerializedName("cadence_days")
    val cadenceDays: Int?,
    @SerializedName("cadence_label")
    val cadenceLabel: String?,
    @SerializedName("last_seen_date")
    val lastSeenDate: String?,
    @SerializedName("next_expected_date")
    val nextExpectedDate: String?,
    val occurrences: Int?,
    val confidence: Double?
)

// ── Anomaly ──

data class Anomaly(
    val category: String?,
    val currency: String?,
    @SerializedName("wallet_id")
    val walletId: String?,
    @SerializedName("current_spend")
    val currentSpend: Double?,
    @SerializedName("historical_mean")
    val historicalMean: Double?,
    @SerializedName("historical_std_dev")
    val historicalStdDev: Double?,
    @SerializedName("z_score")
    val zScore: Double?,
    val severity: String?,
    val message: String?
)

// ── Spending history ──

data class SpendingHistoryResponse(
    @SerializedName("wallet_id")
    val walletId: String?,
    val currency: String?,
    @SerializedName("period_type")
    val periodType: String?,
    val data: List<SpendingPeriod>?
)

data class SpendingPeriod(
    @SerializedName("period_start")
    val periodStart: String?,
    @SerializedName("total_expense")
    val totalExpense: Double?,
    @SerializedName("total_income")
    val totalIncome: Double?,
    @SerializedName("tx_count")
    val txCount: Int?,
    val category: String?
)

