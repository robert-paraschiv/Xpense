package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepo {
    private static final String TAG = "TransactionRepo";

    public static TransactionRepo instance;

    private ListenerRegistration transactionListener;

    private final MutableLiveData<List<Transaction>> allTransactionList;
    private final MutableLiveData<Transaction> latestTransaction;
    private final MutableLiveData<String> addTransactionStatus;

    public static TransactionRepo getInstance() {
        if (instance == null) {
            instance = new TransactionRepo();
        }
        return instance;
    }

    public TransactionRepo() {
        this.allTransactionList = new MutableLiveData<>();
        this.addTransactionStatus = new MutableLiveData<>();
        this.latestTransaction = new MutableLiveData<>();
    }

    public MutableLiveData<List<Transaction>> loadTransactions(String walletId) {
        if (transactionListener != null) {
            transactionListener.remove();
        }
        transactionListener = DatabaseUtils.transactionsRef
                .whereEqualTo("walletId", walletId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        Log.e(TAG, "loadTransactions: null or error: ", error);
                    } else {
                        List<Transaction> transactionList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : value) {
                            Transaction transaction = documentSnapshot.toObject(Transaction.class);
                            if (transaction != null) {
                                transaction.setId(documentSnapshot.getId());
                                transactionList.add(transaction);
                            }
                        }
                        latestTransaction.setValue(transactionList.get(0));
                        allTransactionList.setValue(transactionList);
                    }
                });

        return allTransactionList;
    }

    public MutableLiveData<String> addTransaction(Transaction transaction) {

        DatabaseUtils.transactionsRef.document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(result -> {
                    addTransactionStatus.setValue("Success");
                });

        return addTransactionStatus;
    }

    public MutableLiveData<Transaction> loadLatestTransaction() {
        return latestTransaction;
    }
}
