package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rokudo.xpense.models.Wallet
import com.rokudo.xpense.utils.DatabaseUtils

class WalletsRepo {
    companion object {
        private const val TAG = "WalletsRepo"
        val instance by lazy { WalletsRepo() }
    }

    private var walletListener: ListenerRegistration? = null
    private val allWallets = MutableLiveData<ArrayList<Wallet>?>()
    private var walletList = ArrayList<Wallet>()
    private val walletMutableLiveData = MutableLiveData<Wallet?>()
    private var lastLoadedWalletId: String? = null

    fun removeAllWalletsData() {
        walletListener?.remove()
        allWallets.value = null
        walletList = ArrayList()
        walletMutableLiveData.value = null
    }

    fun addWallet(wallet: Wallet): MutableLiveData<String> {
        val result = MutableLiveData<String>()
        if (FirebaseAuth.getInstance().currentUser != null) {
            DatabaseUtils.walletsRef.document(wallet.id ?: "").set(wallet)
                .addOnSuccessListener {
                    Log.d(TAG, "onSuccess: added wallet $wallet")
                    result.value = "Success"
                }
        }
        return result
    }

    fun loadWallet(walletId: String): MutableLiveData<Wallet?> {
        if (walletId.isEmpty()) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                DatabaseUtils.walletsRef
                    .whereArrayContains("users", currentUser.uid)
                    .orderBy("creation_date", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { snapshots ->
                        if (snapshots.documents.isEmpty()) return@addOnSuccessListener
                        val wallet = snapshots.documents[0].toObject(Wallet::class.java)
                        if (wallet != null) {
                            walletMutableLiveData.postValue(wallet)
                            setListenerForWallet(wallet.id ?: "")
                        }
                    }
                    .addOnFailureListener { e -> Log.e(TAG, "onFailure: ", e) }
            }
        } else {
            setListenerForWallet(walletId)
        }
        return walletMutableLiveData
    }

    private fun setListenerForWallet(walletId: String) {
        if (walletListener == null || lastLoadedWalletId == null || lastLoadedWalletId != walletId) {
            lastLoadedWalletId = walletId
            walletListener?.remove()
            walletMutableLiveData.value = null
            walletListener = DatabaseUtils.walletsRef.document(walletId)
                .addSnapshotListener { value, error ->
                    if (error != null || value == null) {
                        Log.e(TAG, "getWallet: empty or: ", error)
                    } else {
                        val wallet = value.toObject(Wallet::class.java)
                        if (wallet != null) {
                            wallet.id = value.id
                            walletMutableLiveData.postValue(wallet)
                        }
                    }
                }
        }
    }

    fun getWallets(): MutableLiveData<ArrayList<Wallet>?> {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            DatabaseUtils.walletsRef
                .whereArrayContains("users", currentUser.uid)
                .get()
                .addOnSuccessListener { snapshots ->
                    if (snapshots == null || snapshots.isEmpty) {
                        Log.e(TAG, "getWallets: empty or null")
                    } else {
                        for (doc in snapshots) {
                            val wallet = doc.toObject(Wallet::class.java)
                            wallet.id = doc.id
                            handleWalletEvent(wallet)
                        }
                        allWallets.value = walletList
                    }
                }
        }
        return allWallets
    }

    private fun handleWalletEvent(wallet: Wallet) {
        val index = walletList.indexOf(wallet)
        if (index >= 0) {
            walletList[index] = wallet
        } else {
            walletList.add(wallet)
        }
    }

    fun updateWallet(wallet: Wallet): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        if (FirebaseAuth.getInstance().currentUser != null) {
            DatabaseUtils.walletsRef.document(wallet.id ?: "").set(wallet)
                .addOnSuccessListener {
                    Log.d(TAG, "onSuccess: updated wallet $wallet")
                    result.value = true
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "onFailure: ", e)
                    result.value = false
                }
        }
        return result
    }

    fun deleteWallet(walletId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        if (FirebaseAuth.getInstance().currentUser != null) {
            DatabaseUtils.walletsRef.document(walletId).delete()
                .addOnSuccessListener {
                    val wallet = Wallet(id = walletId)
                    walletList.remove(wallet)
                    allWallets.postValue(walletList)
                    result.value = true
                }
                .addOnFailureListener { result.value = false }
        }
        return result
    }
}

