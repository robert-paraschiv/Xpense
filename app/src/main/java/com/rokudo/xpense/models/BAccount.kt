package com.rokudo.xpense.models

import java.io.Serializable
import java.util.Date

data class BAccount(
    var id: String? = null,
    var owner_id: String? = null,
    var EUA_id: String? = null,
    var requisition_id: String? = null,
    var institutionId: String? = null,
    var bankName: String? = null,
    var bankPic: String? = null,
    var accounts: List<String>? = null,
    var walletIds: List<String>? = null,
    var linked_acc_id: String? = null,
    var linked_acc_iban: String? = null,
    var linked_acc_currency: String? = null,
    var EUA_EndDate: Date? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BAccount) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
