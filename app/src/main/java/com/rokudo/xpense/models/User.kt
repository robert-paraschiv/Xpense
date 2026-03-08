package com.rokudo.xpense.models

import java.io.Serializable

data class User(
    var uid: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var pictureUrl: String? = null,
    var token: String? = null
) : Serializable, Comparable<User> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return phoneNumber == other.phoneNumber
    }

    override fun hashCode(): Int = phoneNumber?.hashCode() ?: 0

    override fun compareTo(other: User): Int = (name ?: "").compareTo(other.name ?: "")
}
