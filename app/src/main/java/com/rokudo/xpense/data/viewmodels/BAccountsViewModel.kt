package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.BAccountsRepo
import com.rokudo.xpense.models.BAccount

class BAccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BAccountsRepo.instance

    fun getWalletAccounts(walletId: String): MutableLiveData<List<BAccount>> =
        repo.getWalletAccounts(walletId)

    fun getUserBAccounts(): MutableLiveData<List<BAccount>> = repo.getUserBAccounts()

    fun addBAccount(bAccount: BAccount) = repo.addBAccount(bAccount)
}

