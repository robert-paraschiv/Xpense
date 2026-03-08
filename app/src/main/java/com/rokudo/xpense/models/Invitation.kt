package com.rokudo.xpense.models

import java.util.Date

data class Invitation(
    var id: String? = null,
    var creator_id: String? = null,
    var creator_name: String? = null,
    var wallet_title: String? = null,
    var invited_person_phone_number: String? = null,
    var creator_pic_url: String? = null,
    var date: Date? = null,
    var status: String? = null
) {
    companion object {
        const val STATUS_SENT = "Sent"
        const val STATUS_ACCEPTED = "Accepted"
        const val STATUS_DECLINED = "Declined"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Invitation) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
