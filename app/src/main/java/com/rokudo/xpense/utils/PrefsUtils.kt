package com.rokudo.xpense.utils

import android.content.Context
import com.google.gson.Gson
import com.rokudo.xpense.models.BAccount

object PrefsUtils {
    private const val PREFS_NAME = "PREFS_NAME"
    private const val KEY_JWT_TOKEN = "jwt_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    // ── JWT Auth Token Management ──

    fun saveJwtToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getJwtToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_JWT_TOKEN, null)
    }

    fun saveRefreshToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearAuthTokens(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    // ── Existing methods ──

    fun saveBAccountToPrefs(context: Context, bAccount: BAccount) {
        val settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val serialized = Gson().toJson(bAccount)
        settings.edit().putString("BankAccountToSave", serialized).apply()
    }

    fun getSavedObjectFromPreference(context: Context): BAccount? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains("BankAccountToSave")) {
            Gson().fromJson(prefs.getString("BankAccountToSave", ""), BAccount::class.java)
        } else null
    }

    fun setSelectedWalletId(context: Context, walletId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("selectedWalletId", walletId).apply()
    }

    fun setToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(GoCardlessUtils.TOKEN_PREFS_NAME, token).apply()
    }

    fun setString(context: Context, id: String, value: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(id, value).apply()
    }

    fun setInt(context: Context, id: String, value: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(id, value).apply()
    }
}
