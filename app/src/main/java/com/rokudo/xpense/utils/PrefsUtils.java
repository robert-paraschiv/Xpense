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
}
