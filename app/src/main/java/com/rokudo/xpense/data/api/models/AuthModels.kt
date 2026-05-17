package com.rokudo.xpense.data.api.models

import com.rokudo.xpense.models.User

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String?,
    val refreshToken: String?,
    val user: User?,
    val message: String?
)

data class FcmTokenRequest(
    val fcmToken: String
)

