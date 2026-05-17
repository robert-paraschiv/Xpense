package com.rokudo.xpense.models

import com.rokudo.xpense.utils.DatabaseUtils
import java.io.Serializable

data class WalletUser(
    var userId: String? = null,
    var userPic: String? = null,
    var userName: String? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WalletUser) return false
        return userId == other.userId
    }

    override fun hashCode(): Int = userId?.hashCode() ?: 0

    companion object {
        fun getOtherUserProfilePic(walletUsers: List<WalletUser>?): String? {
            if (walletUsers == null) return null
            val currentUid = DatabaseUtils.currentUser?.uid ?: return null
            return walletUsers.firstOrNull { it.userId != currentUid }?.userPic
        }

        fun getOtherWalletUser(walletUsers: List<WalletUser>?): WalletUser? {
            if (walletUsers == null) return null
            val currentUid = DatabaseUtils.currentUser?.uid ?: return null
            return walletUsers.firstOrNull { it.userId != currentUid }
        }
    }
}
