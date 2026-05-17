package com.rokudo.xpense.data.api

import com.rokudo.xpense.data.api.models.StatisticsResponse
import com.rokudo.xpense.data.api.models.YearlyStatisticsResponse
import com.rokudo.xpense.data.api.models.CompareResponse
import com.rokudo.xpense.models.Transaction
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StatisticsApiService {

    @GET("wallets/{walletId}/statistics")
    suspend fun getMonthlyStatistics(
        @Path("walletId") walletId: String,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null
    ): Response<StatisticsResponse>

    @GET("wallets/{walletId}/statistics/yearly")
    suspend fun getYearlyStatistics(
        @Path("walletId") walletId: String,
        @Query("year") year: Int? = null
    ): Response<YearlyStatisticsResponse>

    @GET("wallets/{walletId}/statistics/compare")
    suspend fun getCompareStatistics(
        @Path("walletId") walletId: String,
        @Query("year") year: Int? = null,
        @Query("month") month: Int? = null,
        @Query("type") type: String = "MONTHLY"
    ): Response<CompareResponse>

    @GET("wallets/{walletId}/transactions/recent")
    suspend fun getRecentTransactions(
        @Path("walletId") walletId: String,
        @Query("limit") limit: Int = 5
    ): Response<List<Transaction>>

    @GET("wallets/{walletId}/transactions/latest")
    suspend fun getLatestTransaction(
        @Path("walletId") walletId: String
    ): Response<Transaction>
}

