package com.rokudo.xpense.utils;

import com.google.gson.Gson;
import com.rokudo.xpense.data.retrofit.models.Balances;

public class GsonHelper {
    public static String serializeBalancesToJson(Balances bmp) {
        Gson gson = new Gson();
        return gson.toJson(bmp);
    }

    // Deserialize to single object.
    public static Balances deserializeBalancesFromJson(String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, Balances.class);
    }
}
