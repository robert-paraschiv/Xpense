package com.rokudo.xpense.data.api

import com.rokudo.xpense.models.Wallet
import retrofit2.Response
import retrofit2.http.*

interface WalletApiService {

    @POST("wallets")
    suspend fun createWallet(@Body wallet: Wallet): Response<Wallet>

    @GET("wallets")
    suspend fun getUserWallets(): Response<List<Wallet>>

    @GET("wallets/{walletId}")
    suspend fun getWallet(@Path("walletId") walletId: String): Response<Wallet>

    @PUT("wallets/{walletId}")
    suspend fun updateWallet(
        @Path("walletId") walletId: String,
        @Body wallet: Wallet
    ): Response<Wallet>

    @DELETE("wallets/{walletId}")
    suspend fun deleteWallet(@Path("walletId") walletId: String): Response<String>

    @POST("wallets/{walletId}/users/{userIdToAdd}")
    suspend fun addUserToWallet(
        @Path("walletId") walletId: String,
        @Path("userIdToAdd") userIdToAdd: String
    ): Response<String>

    @DELETE("wallets/{walletId}/users/{userIdToRemove}")
    suspend fun removeUserFromWallet(
        @Path("walletId") walletId: String,
        @Path("userIdToRemove") userIdToRemove: String
    ): Response<String>
}

