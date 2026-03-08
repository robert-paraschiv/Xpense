package com.rokudo.xpense.models

import java.io.Serializable
import java.text.DecimalFormat
import java.util.Date

data class Wallet(
    var id: String? = null,
    var title: String? = null,
    var creator_id: String? = null,
    var amount: Double? = null,
    var currency: String? = null,
    var creation_date: Date? = null,
    var users: List<String>? = null,
    var walletUsers: List<WalletUser>? = null,
    var bAccount: BAccount? = null
) : Serializable {

    fun getFormattedAmount(): Double {
        val df = DecimalFormat("0.00")
        return df.format(amount ?: 0.0).toDouble()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallet) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
