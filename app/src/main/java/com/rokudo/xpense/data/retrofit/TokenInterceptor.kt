package com.rokudo.xpense.data.retrofit

import com.rokudo.xpense.utils.GoCardlessUtils
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer ${GoCardlessUtils.TOKEN_VAL}")
            .build()
        return chain.proceed(newRequest)
    }
}

