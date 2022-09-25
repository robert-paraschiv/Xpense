package com.rokudo.xpense.data.repositories;

import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.List;

public class TransactionRepo {
    private static final String TAG = "TransactionRepo";

    public static TransactionRepo instance;

    private MutableLiveData<List<Transaction>> allTransactionList;
    private MutableLiveData<String> addTransactionStatus;

    public static TransactionRepo getInstance() {
        if (instance == null) {
            instance = new TransactionRepo();
        }
        return instance;
    }

    public TransactionRepo() {
        this.allTransactionList = new MutableLiveData<>();
        this.addTransactionStatus = new MutableLiveData<>();
    }

    public MutableLiveData<List<Transaction>> loadTransactions(String walletId) {


        return allTransactionList;
    }

    public MutableLiveData<String> addTransaction(String walletId, Transaction transaction) {

        DatabaseUtils.transactionsRef.document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(result -> {
                    addTransactionStatus.setValue("Success");
                });

        return addTransactionStatus;
    }
}
