package com.rokudo.xpense.data.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.rokudo.xpense.data.api.ApiClient
import com.rokudo.xpense.data.api.ApiServiceProvider
import com.rokudo.xpense.data.api.models.FcmTokenRequest
import com.rokudo.xpense.data.api.models.LoginRequest
import com.rokudo.xpense.data.api.models.RegisterRequest
import com.rokudo.xpense.models.User
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.PrefsUtils
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * Register a new user account via the backend API.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String
    ): Result<User> {
        return try {
            val request = RegisterRequest(name, email, password, phoneNumber)
            val response = ApiServiceProvider.authService.register(request)

            if (response.isSuccessful) {
                val body = response.body()!!
                handleAuthSuccess(body.token, body.refreshToken, body.user)
                Result.success(body.user!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                Log.e(TAG, "register: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "register: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Login with email & password via the backend API.
     */
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = ApiServiceProvider.authService.login(request)

            if (response.isSuccessful) {
                val body = response.body()!!
                handleAuthSuccess(body.token, body.refreshToken, body.user)
                Result.success(body.user!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed"
                Log.e(TAG, "login: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "login: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh the JWT token.
     */
    suspend fun refreshToken(): Result<String> {
        return try {
            val currentToken = PrefsUtils.getJwtToken(context) ?: return Result.failure(Exception("No token"))
            val response = ApiServiceProvider.authService.refresh("Bearer $currentToken")

            if (response.isSuccessful) {
                val newToken = response.body()?.token ?: return Result.failure(Exception("Empty token"))
                ApiClient.setToken(newToken)
                PrefsUtils.saveJwtToken(context, newToken)
                Result.success(newToken)
            } else {
                Result.failure(Exception("Token refresh failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "refreshToken: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Load the current user profile from the backend.
     */
    suspend fun loadCurrentUser(): Result<User> {
        return try {
            val response = ApiServiceProvider.userService.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()!!
                DatabaseUtils.currentUser = user
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to load user"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadCurrentUser: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Restore a session from saved JWT tokens on app startup.
     * Returns true if a valid session was restored.
     */
    fun restoreSession(): Boolean {
        val token = PrefsUtils.getJwtToken(context)
        return if (token != null) {
            ApiClient.setToken(token)
            true
        } else {
            false
        }
    }

    /**
     * Logout — clear tokens and current user.
     */
    fun logout() {
        ApiClient.clearToken()
        PrefsUtils.clearAuthTokens(context)
        DatabaseUtils.currentUser = null
    }

    // ── Private helpers ──

    private suspend fun handleAuthSuccess(token: String?, refreshToken: String?, user: User?) {
        // 1. Store tokens
        token?.let {
            ApiClient.setToken(it)
            PrefsUtils.saveJwtToken(context, it)
        }
        refreshToken?.let {
            PrefsUtils.saveRefreshToken(context, it)
        }

        // 2. Set current user in memory
        user?.let {
            DatabaseUtils.currentUser = it
        }

        // 3. Register FCM token with the backend
        registerFcmToken()
    }

    private suspend fun registerFcmToken() {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            ApiServiceProvider.userService.updateFcmToken(FcmTokenRequest(fcmToken))
            Log.d(TAG, "FCM token registered with backend")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register FCM token: ${e.message}")
        }
    }
}

