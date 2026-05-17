package com.rokudo.xpense.models

import java.io.Serializable
import java.text.DecimalFormat
import java.util.Date

data class Transaction(
    var id: String? = null,
    var walletId: String? = null,
    var type: String? = null,
    var category: String? = null,
    var user_id: String? = null,
    var userName: String? = null,
    var title: String? = null,
    var amount: Double? = null,
    var currency: String? = null,
    var date: Date? = null,
    var picUrl: String? = null,
    var isCashTransaction: Boolean? = null,
    var alreadyAdded: Boolean? = null
) : Serializable {

    companion object {
        const val INCOME_TYPE = "Income"
        const val EXPENSE_TYPE = "Expense"
        const val TRANSFER_TYPE = "Transfer"
    }

    fun getFormattedAmount(): Double {
        val df = DecimalFormat("0.00")
        return df.format(amount ?: 0.0).toDouble()
    }

    val dateLong: Long
        get() = date?.time ?: 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
