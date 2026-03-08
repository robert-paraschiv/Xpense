package com.rokudo.xpense.utils

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rokudo.xpense.models.User

object DatabaseUtils {
    var currentUser: User? = null

    val usersRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("Users")

    val walletsRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("Wallets")

    val invitationsRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("Invitations")

    val bAccountsRef: CollectionReference =
        FirebaseFirestore.getInstance().collection("BAccounts")

    val userPicturesRef: StorageReference =
        FirebaseStorage.getInstance().getReference("UserPictures")

    fun getYearReference(walletId: String, year: String): DocumentReference {
        require(walletId.isNotEmpty()) { "Wallet ID cannot be null or empty" }
        return walletsRef.document(walletId)
            .collection("Statistics")
            .document(year)
    }

    fun getMonthsReference(walletId: String, year: String): CollectionReference {
        require(walletId.isNotEmpty()) { "Wallet ID cannot be null or empty" }
        return walletsRef.document(walletId)
            .collection("Statistics")
            .document(year)
            .collection("Months")
    }

    fun getTransactionsRef(walletId: String): CollectionReference {
        require(walletId.isNotEmpty()) { "Wallet ID cannot be null or empty" }
        return walletsRef.document(walletId).collection("Transactions")
    }
}

