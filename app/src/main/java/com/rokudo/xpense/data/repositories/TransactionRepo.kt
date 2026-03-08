package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rokudo.xpense.models.Transaction
import com.rokudo.xpense.utils.DatabaseUtils
import java.util.*

class TransactionRepo {
    companion object {
        private const val TAG = "TransactionRepo"
        val instance by lazy { TransactionRepo() }
    }

    private var transactionListener: ListenerRegistration? = null
    private var latestTransListener: ListenerRegistration? = null
    private val allTransactionList = MutableLiveData<List<Transaction>>()
    private val storedTransactionList = mutableListOf<Transaction>()
    private val latestTransaction = MutableLiveData<Transaction>()
    private val addTransactionStatus = MutableLiveData<String>()
    private val updateTransactionStatus = MutableLiveData<String>()
    private var storedWalletId: String? = null

    fun removeAllTransactionsData() {
        transactionListener?.remove()
        latestTransListener?.remove()
        allTransactionList.value = null
        storedTransactionList.clear()
        addTransactionStatus.value = null
        latestTransaction.value = null
        updateTransactionStatus.value = null
    }

    fun loadTransactions(): MutableLiveData<List<Transaction>> = allTransactionList

    fun loadTransactions(walletId: String, startDate: Date): MutableLiveData<List<Transaction>> {
        val calendar = Calendar.getInstance().apply {
            set(get(Calendar.YEAR), get(Calendar.MONTH), getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59)
        }
        val end = calendar.time

        transactionListener?.remove()
        transactionListener = DatabaseUtils.getTransactionsRef(walletId)
            .whereGreaterThan("date", startDate)
            .whereLessThan("date", end)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null || value.isEmpty) {
                    Log.e(TAG, "loadTransactions: null or error: ", error)
                    allTransactionList.value = emptyList()
                    storedTransactionList.clear()
                    latestTransaction.value = Transaction()
                } else {
                    val list = value.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.also { it.id = doc.id }
                    }
                    latestTransaction.value = if (list.isEmpty()) Transaction() else list[0]
                    allTransactionList.value = list
                    storedTransactionList.clear()
                    storedTransactionList.addAll(list)
                }
            }
        return allTransactionList
    }

    fun addTransaction(transaction: Transaction): MutableLiveData<String> {
        DatabaseUtils.getTransactionsRef(transaction.walletId ?: "").document(transaction.id ?: "")
            .set(transaction)
            .addOnSuccessListener { addTransactionStatus.value = "Success" }
        return addTransactionStatus
    }

    fun loadLatestTransaction(walletId: String): MutableLiveData<Transaction> {
        if (latestTransListener == null || storedWalletId == null || storedWalletId != walletId) {
            latestTransListener?.remove()
            storedWalletId = walletId
            latestTransListener = DatabaseUtils.getTransactionsRef(walletId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { value, _ ->
                    if (value == null || value.isEmpty) return@addSnapshotListener
                    val transaction = value.documents[0].toObject(Transaction::class.java)
                        ?: return@addSnapshotListener
                    latestTransaction.value = transaction
                }
        }
        return latestTransaction
    }

    fun loadTransactionsDateInterval(walletId: String, start: Date, end: Date): MutableLiveData<List<Transaction>> {
        val data = MutableLiveData<List<Transaction>>()
        DatabaseUtils.getTransactionsRef(walletId)
            .whereGreaterThan("date", start)
            .whereLessThan("date", end)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { value ->
                if (value == null || value.isEmpty) {
                    data.value = emptyList()
                } else {
                    data.value = value.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.also { it.id = doc.id }
                    }
                }
            }
        return data
    }

    fun getStoredTransactionList(): List<Transaction> = storedTransactionList

    fun updateTransaction(transaction: Transaction): MutableLiveData<String> {
        DatabaseUtils.getTransactionsRef(transaction.walletId ?: "").document(transaction.id ?: "")
            .set(transaction)
            .addOnSuccessListener { updateTransactionStatus.value = "Success" }
        return updateTransactionStatus
    }

    fun deleteTransaction(transactionId: String, walletId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        DatabaseUtils.getTransactionsRef(walletId)
            .document(transactionId)
            .delete()
            .addOnCompleteListener { task -> result.value = task.isSuccessful }
        return result
    }
}

