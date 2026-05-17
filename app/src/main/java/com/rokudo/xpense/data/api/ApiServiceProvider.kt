package com.rokudo.xpense.data.api

object ApiServiceProvider {

    val authService: AuthApiService by lazy {
        ApiClient.retrofit.create(AuthApiService::class.java)
    }

    val userService: UserApiService by lazy {
        ApiClient.retrofit.create(UserApiService::class.java)
    }

    val walletService: WalletApiService by lazy {
        ApiClient.retrofit.create(WalletApiService::class.java)
    }

    val transactionService: TransactionApiService by lazy {
        ApiClient.retrofit.create(TransactionApiService::class.java)
    }

    val predictionService: PredictionApiService by lazy {
        ApiClient.retrofit.create(PredictionApiService::class.java)
    }

    val statisticsService: StatisticsApiService by lazy {
        ApiClient.retrofit.create(StatisticsApiService::class.java)
    }
}
