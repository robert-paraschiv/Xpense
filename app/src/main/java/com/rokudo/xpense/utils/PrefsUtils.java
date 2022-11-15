package com.rokudo.xpense.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.rokudo.xpense.models.BAccount;

public class PrefsUtils {

    public static void saveBAccountToPrefs(Context context, BAccount bAccount){
        SharedPreferences settings = context
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(bAccount);
        editor.putString("BankAccountToSave", serializedObject);
        editor.apply();
    }

    public static BAccount getSavedObjectFromPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        if (sharedPreferences.contains("BankAccountToSave")) {
            final Gson gson = new Gson();
            return gson.fromJson(sharedPreferences.getString("BankAccountToSave", ""), BAccount.class);
        }
        return null;
    }

    public static void setSelectedWalletId(Context context, String walletId) {
        SharedPreferences settings = context
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("selectedWalletId", walletId);
        editor.apply();
    }

    public static void setToken(Context context, String token) {
        SharedPreferences settings = context
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);
        editor.apply();
    }

    public static void setString(Context context, String id, String value) {
        SharedPreferences settings = context
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(id, value);
        editor.apply();
    }
}
