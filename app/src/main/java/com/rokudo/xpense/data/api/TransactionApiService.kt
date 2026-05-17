package com.rokudo.xpense.data.api

import com.rokudo.xpense.models.Transaction
import retrofit2.Response
import retrofit2.http.*

interface TransactionApiService {

    @POST("wallets/{walletId}/transactions")
    suspend fun createTransaction(
        @Path("walletId") walletId: String,
        @Body transaction: Transaction
    ): Response<Transaction>

    @GET("wallets/{walletId}/transactions")
    suspend fun getWalletTransactions(
        @Path("walletId") walletId: String
    ): Response<List<Transaction>>

    @GET("wallets/{walletId}/transactions/{transactionId}")
    suspend fun getTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String
    ): Response<Transaction>

    @PUT("wallets/{walletId}/transactions/{transactionId}")
    suspend fun updateTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String,
        @Body transaction: Transaction
    ): Response<Transaction>

    @DELETE("wallets/{walletId}/transactions/{transactionId}")
    suspend fun deleteTransaction(
        @Path("walletId") walletId: String,
        @Path("transactionId") transactionId: String
    ): Response<String>
}

