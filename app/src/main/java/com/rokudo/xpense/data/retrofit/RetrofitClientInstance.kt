package com.rokudo.xpense.data.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientInstance {
    private const val BASE_URL = "https://bankaccountdata.gocardless.com"

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor())
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .build()

        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): Retrofit = retrofit
}

