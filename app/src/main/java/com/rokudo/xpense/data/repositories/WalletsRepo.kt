package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.api.ApiServiceProvider
import com.rokudo.xpense.models.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletsRepo {
    companion object {
        private const val TAG = "WalletsRepo"
        val instance by lazy { WalletsRepo() }
    }

    private val allWallets = MutableLiveData<ArrayList<Wallet>?>()
    private var walletList = ArrayList<Wallet>()
    private val walletMutableLiveData = MutableLiveData<Wallet?>()
    private var lastLoadedWalletId: String? = null

    fun removeAllWalletsData() {
        allWallets.value = null
        walletList = ArrayList()
        walletMutableLiveData.value = null
    }

    fun addWallet(wallet: Wallet): MutableLiveData<String> {
        val result = MutableLiveData<String>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.walletService.createWallet(wallet)
                if (response.isSuccessful) {
                    Log.d(TAG, "onSuccess: added wallet ${response.body()}")
                    result.postValue("Success")
                } else {
                    val error = response.errorBody()?.string() ?: "Creation failed"
                    Log.e(TAG, "addWallet: $error")
                    result.postValue(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "addWallet: ${e.message}", e)
                result.postValue(e.message)
            }
        }
        return result
    }

    fun loadWallet(walletId: String): MutableLiveData<Wallet?> {
        if (walletId.isEmpty()) {
            // Load the first/latest wallet for the current user
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = ApiServiceProvider.walletService.getUserWallets()
                    if (response.isSuccessful) {
                        val wallets = response.body() ?: emptyList()
                        val latest = wallets.maxByOrNull { it.creation_date?.time ?: 0L }
                        if (latest != null) {
                            walletMutableLiveData.postValue(latest)
                            lastLoadedWalletId = latest.id
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "loadWallet: ${e.message}", e)
                }
            }
        } else {
            if (lastLoadedWalletId == null || lastLoadedWalletId != walletId) {
                lastLoadedWalletId = walletId
                walletMutableLiveData.value = null
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiServiceProvider.walletService.getWallet(walletId)
                        if (response.isSuccessful) {
                            walletMutableLiveData.postValue(response.body())
                        } else {
                            Log.e(TAG, "loadWallet: ${response.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "loadWallet: ${e.message}", e)
                    }
                }
            }
        }
        return walletMutableLiveData
    }

    fun getWallets(): MutableLiveData<ArrayList<Wallet>?> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.walletService.getUserWallets()
                if (response.isSuccessful) {
                    val wallets = response.body() ?: emptyList()
                    walletList = ArrayList(wallets)
                    allWallets.postValue(walletList)
                } else {
                    Log.e(TAG, "getWallets: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getWallets: ${e.message}", e)
            }
        }
        return allWallets
    }

    fun updateWallet(wallet: Wallet): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.walletService.updateWallet(
                    wallet.id ?: "", wallet
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "onSuccess: updated wallet ${response.body()}")
                    result.postValue(true)
                } else {
                    Log.e(TAG, "updateWallet: ${response.errorBody()?.string()}")
                    result.postValue(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateWallet: ${e.message}", e)
                result.postValue(false)
            }
        }
        return result
    }

    fun deleteWallet(walletId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.walletService.deleteWallet(walletId)
                if (response.isSuccessful) {
                    val wallet = Wallet(id = walletId)
                    walletList.remove(wallet)
                    allWallets.postValue(walletList)
                    result.postValue(true)
                } else {
                    result.postValue(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteWallet: ${e.message}", e)
                result.postValue(false)
            }
        }
        return result
    }
}
