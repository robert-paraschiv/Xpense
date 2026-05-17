package com.rokudo.xpense.data.api

import com.rokudo.xpense.data.api.models.AuthResponse
import com.rokudo.xpense.data.api.models.LoginRequest
import com.rokudo.xpense.data.api.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Header("Authorization") token: String): Response<AuthResponse>
}

