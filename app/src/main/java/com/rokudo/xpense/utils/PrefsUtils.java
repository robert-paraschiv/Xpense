package com.rokudo.xpense.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsUtils {

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
