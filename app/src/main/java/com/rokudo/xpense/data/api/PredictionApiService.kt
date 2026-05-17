package com.rokudo.xpense.data.api

import com.rokudo.xpense.data.api.models.Anomaly
import com.rokudo.xpense.data.api.models.CategoryForecast
import com.rokudo.xpense.data.api.models.ForecastResponse
import com.rokudo.xpense.data.api.models.MtdProjectionResponse
import com.rokudo.xpense.data.api.models.RecurringTransaction
import com.rokudo.xpense.data.api.models.SpendingHistoryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PredictionApiService {

    @GET("predictions/wallet/{walletId}/next-month")
    suspend fun getNextMonthForecast(
        @Path("walletId") walletId: String
    ): Response<ForecastResponse>

    @GET("predictions/wallet/{walletId}/end-of-month-projection")
    suspend fun getEndOfMonthProjection(
        @Path("walletId") walletId: String
    ): Response<MtdProjectionResponse>

    @GET("predictions/user/me/categories")
    suspend fun getCategoryForecasts(): Response<List<CategoryForecast>>

    @GET("predictions/user/me/recurring")
    suspend fun getRecurringTransactions(): Response<List<RecurringTransaction>>

    @GET("predictions/user/me/anomalies")
    suspend fun getAnomalies(): Response<List<Anomaly>>

    @GET("predictions/wallet/{walletId}/history")
    suspend fun getSpendingHistory(
        @Path("walletId") walletId: String,
        @Query("periodType") periodType: String = "MONTHLY",
        @Query("months") months: Int = 12
    ): Response<SpendingHistoryResponse>
}


