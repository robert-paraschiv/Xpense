package com.rokudo.xpense.models

import java.util.Date

data class TransEntry(
    var day: String = "",
    var date: Date? = null,
    var amount: Float = 0f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransEntry) return false
        return day == other.day
    }

    override fun hashCode(): Int = day.hashCode()
}

