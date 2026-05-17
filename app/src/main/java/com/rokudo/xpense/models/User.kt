package com.rokudo.xpense.models

import java.io.Serializable

data class User(
    var uid: String? = null,
    var name: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var pictureUrl: String? = null,
    var fcmToken: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var walletIds: List<String>? = null,
    // Keep legacy field for backward compat
    var token: String? = null
) : Serializable, Comparable<User> {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return uid == other.uid
    }

    override fun hashCode(): Int = uid?.hashCode() ?: 0

    override fun compareTo(other: User): Int = (name ?: "").compareTo(other.name ?: "")
}
