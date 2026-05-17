package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.TransactionRepo
import com.rokudo.xpense.models.Transaction
import java.util.Date

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TransactionRepo.instance

    fun loadTransactions(walletId: String, date: Date): MutableLiveData<List<Transaction>?> =
        repo.loadTransactions(walletId, date)

    fun loadTransactions(): MutableLiveData<List<Transaction>?> = repo.loadTransactions()

    fun loadTransactionsDateInterval(walletId: String, start: Date, end: Date): MutableLiveData<List<Transaction>> =
        repo.loadTransactionsDateInterval(walletId, start, end)

    fun addTransaction(transaction: Transaction): MutableLiveData<String?> =
        repo.addTransaction(transaction)

    fun loadLatestTransaction(walletId: String): MutableLiveData<Transaction?> =
        repo.loadLatestTransaction(walletId)

    fun getStoredTransactionList(): List<Transaction> = repo.getStoredTransactionList()

    fun loadRecentTransactions(walletId: String, limit: Int = 5): MutableLiveData<List<Transaction>> =
        repo.loadRecentTransactions(walletId, limit)

    fun updateTransaction(transaction: Transaction): MutableLiveData<String?> =
        repo.updateTransaction(transaction)

    fun deleteTransaction(transactionId: String, walletId: String): MutableLiveData<Boolean> =
        repo.deleteTransaction(transactionId, walletId)

    fun refreshData(walletId: String) = repo.refreshData(walletId)

    fun removeAllData() = repo.removeAllTransactionsData()
}

