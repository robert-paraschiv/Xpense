package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.api.ApiServiceProvider
import com.rokudo.xpense.models.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TransactionRepo {
    companion object {
        private const val TAG = "TransactionRepo"
        val instance by lazy { TransactionRepo() }
    }

    private val allTransactionList: MutableLiveData<List<Transaction>?> = MutableLiveData<List<Transaction>?>()
    private val storedTransactionList = mutableListOf<Transaction>()
    private val latestTransaction: MutableLiveData<Transaction?> = MutableLiveData<Transaction?>()
    private val addTransactionStatus: MutableLiveData<String?> = MutableLiveData<String?>()
    private val updateTransactionStatus: MutableLiveData<String?> = MutableLiveData<String?>()
    private var storedWalletId: String? = null

    fun removeAllTransactionsData() {
        allTransactionList.value = null
        storedTransactionList.clear()
        addTransactionStatus.value = null
        latestTransaction.value = null
        updateTransactionStatus.value = null
    }

    fun loadTransactions(): MutableLiveData<List<Transaction>?> = allTransactionList

    fun loadTransactions(walletId: String, startDate: Date): MutableLiveData<List<Transaction>?> {
        val calendar = Calendar.getInstance().apply {
            set(get(Calendar.YEAR), get(Calendar.MONTH), getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59)
        }
        val end = calendar.time

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.transactionService.getWalletTransactions(walletId)
                if (response.isSuccessful) {
                    val transactions = response.body() ?: emptyList()
                    // Filter by date range client-side
                    val filtered = transactions.filter { t ->
                        val date = t.date
                        date != null && date.after(startDate) && date.before(end)
                    }.sortedByDescending { it.date }

                    latestTransaction.postValue(if (filtered.isEmpty()) Transaction() else filtered[0])
                    allTransactionList.postValue(filtered)
                    storedTransactionList.clear()
                    storedTransactionList.addAll(filtered)
                } else {
                    Log.e(TAG, "loadTransactions: ${response.errorBody()?.string()}")
                    allTransactionList.postValue(emptyList())
                    storedTransactionList.clear()
                    latestTransaction.postValue(Transaction())
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadTransactions: ${e.message}", e)
                allTransactionList.postValue(emptyList())
                storedTransactionList.clear()
                latestTransaction.postValue(Transaction())
            }
        }
        return allTransactionList
    }

    fun addTransaction(transaction: Transaction): MutableLiveData<String?> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.transactionService.createTransaction(
                    transaction.walletId ?: "",
                    transaction
                )
                if (response.isSuccessful) {
                    addTransactionStatus.postValue("Success")
                } else {
                    val error = response.errorBody()?.string() ?: "Creation failed"
                    Log.e(TAG, "addTransaction: $error")
                    addTransactionStatus.postValue(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "addTransaction: ${e.message}", e)
                addTransactionStatus.postValue(e.message)
            }
        }
        return addTransactionStatus
    }

    fun loadLatestTransaction(walletId: String): MutableLiveData<Transaction?> {
        if (storedWalletId == null || storedWalletId != walletId) {
            storedWalletId = walletId
            fetchLatestTransaction(walletId)
        }
        return latestTransaction
    }

    /**
     * Force refresh all transaction data for the current wallet.
     * Call this after adding/updating/deleting a transaction.
     */
    fun refreshData(walletId: String) {
        storedWalletId = walletId
        fetchLatestTransaction(walletId)
    }

    private fun fetchLatestTransaction(walletId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try the dedicated /latest endpoint first
                val response = ApiServiceProvider.statisticsService.getLatestTransaction(walletId)
                if (response.isSuccessful) {
                    latestTransaction.postValue(response.body())
                } else {
                    // Fallback: fetch all and pick the latest
                    val fallback = ApiServiceProvider.transactionService.getWalletTransactions(walletId)
                    if (fallback.isSuccessful) {
                        val transactions = fallback.body() ?: emptyList()
                        val latest = transactions.maxByOrNull { it.date ?: Date(0) }
                        latestTransaction.postValue(latest)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchLatestTransaction: ${e.message}", e)
            }
        }
    }

    fun loadTransactionsDateInterval(walletId: String, start: Date, end: Date): MutableLiveData<List<Transaction>> {
        val data = MutableLiveData<List<Transaction>>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.transactionService.getWalletTransactions(walletId)
                if (response.isSuccessful) {
                    val transactions = response.body() ?: emptyList()
                    val filtered = transactions.filter { t ->
                        val date = t.date
                        date != null && date.after(start) && date.before(end)
                    }.sortedByDescending { it.date }
                    data.postValue(filtered)
                } else {
                    data.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadTransactionsDateInterval: ${e.message}", e)
                data.postValue(emptyList())
            }
        }
        return data
    }

    fun getStoredTransactionList(): List<Transaction> = storedTransactionList

    /**
     * Load the N most recent transactions using the dedicated /recent endpoint.
     */
    fun loadRecentTransactions(walletId: String, limit: Int = 5): MutableLiveData<List<Transaction>> {
        val data = MutableLiveData<List<Transaction>>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.statisticsService.getRecentTransactions(walletId, limit)
                if (response.isSuccessful) {
                    data.postValue(response.body() ?: emptyList())
                } else {
                    // Fallback: fetch all and take the most recent
                    val fallback = ApiServiceProvider.transactionService.getWalletTransactions(walletId)
                    if (fallback.isSuccessful) {
                        val recent = (fallback.body() ?: emptyList())
                            .sortedByDescending { it.date }
                            .take(limit)
                        data.postValue(recent)
                    } else {
                        data.postValue(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadRecentTransactions: ${e.message}", e)
                data.postValue(emptyList())
            }
        }
        return data
    }

    fun updateTransaction(transaction: Transaction): MutableLiveData<String?> {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.transactionService.updateTransaction(
                    transaction.walletId ?: "",
                    transaction.id ?: "",
                    transaction
                )
                if (response.isSuccessful) {
                    updateTransactionStatus.postValue("Success")
                } else {
                    val error = response.errorBody()?.string() ?: "Update failed"
                    Log.e(TAG, "updateTransaction: $error")
                    updateTransactionStatus.postValue(error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateTransaction: ${e.message}", e)
                updateTransactionStatus.postValue(e.message)
            }
        }
        return updateTransactionStatus
    }

    fun deleteTransaction(transactionId: String, walletId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiServiceProvider.transactionService.deleteTransaction(walletId, transactionId)
                result.postValue(response.isSuccessful)
            } catch (e: Exception) {
                Log.e(TAG, "deleteTransaction: ${e.message}", e)
                result.postValue(false)
            }
        }
        return result
    }
}
