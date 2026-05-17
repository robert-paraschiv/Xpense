package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.WalletsRepo
import com.rokudo.xpense.models.Wallet

class WalletsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = WalletsRepo.instance

    fun loadWallets(): MutableLiveData<ArrayList<Wallet>?> = repo.getWallets()

    fun loadWallet(walletId: String): MutableLiveData<Wallet?> = repo.loadWallet(walletId)

    fun addWallet(wallet: Wallet): MutableLiveData<String> = repo.addWallet(wallet)

    fun updateWallet(wallet: Wallet): MutableLiveData<Boolean> = repo.updateWallet(wallet)

    fun deleteWallet(walletId: String): MutableLiveData<Boolean> = repo.deleteWallet(walletId)

    fun removeAllData() = repo.removeAllWalletsData()
}

