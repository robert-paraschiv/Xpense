package com.rokudo.xpense.utils

import com.google.gson.Gson
import com.rokudo.xpense.data.retrofit.models.Balances

object GsonHelper {
    fun serializeBalancesToJson(balances: Balances?): String = Gson().toJson(balances)

    fun deserializeBalancesFromJson(jsonString: String?): Balances? {
        return if (jsonString != null) Gson().fromJson(jsonString, Balances::class.java) else null
    }
}

