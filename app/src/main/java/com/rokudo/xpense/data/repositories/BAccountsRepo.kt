package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.rokudo.xpense.models.BAccount
import com.rokudo.xpense.utils.DatabaseUtils

class BAccountsRepo {
    companion object {
        private const val TAG = "BAccountsRepo"
        val instance by lazy { BAccountsRepo() }
    }

    private var bAccountListener: ListenerRegistration? = null
    private val bAccountLiveData = MutableLiveData<List<BAccount>>()
    private val userBAccountLiveData = MutableLiveData<List<BAccount>>()

    fun addBAccount(bAccount: BAccount) {
        val id = DatabaseUtils.bAccountsRef.document().id
        bAccount.id = id
        DatabaseUtils.bAccountsRef.document(id).set(bAccount)
            .addOnSuccessListener { Log.d(TAG, "onSuccess: added") }
    }

    fun getUserBAccounts(): MutableLiveData<List<BAccount>> {
        val currentUser = DatabaseUtils.currentUser ?: return userBAccountLiveData
        DatabaseUtils.bAccountsRef
            .whereEqualTo("owner_id", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots == null || snapshots.isEmpty) {
                    bAccountLiveData.value = emptyList()
                } else {
                    bAccountLiveData.value = snapshots.documents.mapNotNull { doc ->
                        doc.toObject(BAccount::class.java)?.also { it.id = doc.id }
                    }
                }
            }
        return userBAccountLiveData
    }

    fun getWalletAccounts(walletId: String): MutableLiveData<List<BAccount>> {
        bAccountListener?.remove()
        bAccountListener = DatabaseUtils.bAccountsRef
            .whereArrayContains("walletIds", walletId)
            .addSnapshotListener { value, _ ->
                if (value == null || value.isEmpty) {
                    bAccountLiveData.value = emptyList()
                } else {
                    bAccountLiveData.value = value.documents.mapNotNull { doc ->
                        doc.toObject(BAccount::class.java)?.also { it.id = doc.id }
                    }
                }
            }
        return bAccountLiveData
    }
}

