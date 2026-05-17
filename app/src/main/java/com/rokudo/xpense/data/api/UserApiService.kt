package com.rokudo.xpense.data.api

import com.rokudo.xpense.data.api.models.FcmTokenRequest
import com.rokudo.xpense.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<User>

    @PUT("users/me")
    suspend fun updateCurrentUser(@Body user: User): Response<User>

    @POST("users/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<String>
}

