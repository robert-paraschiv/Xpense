package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepo {
    private static final String TAG = "TransactionRepo";

    public static TransactionRepo instance;

    private ListenerRegistration transactionListener;

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
                        allTransactionList.setValue(transactionList);
                    }
                });

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
