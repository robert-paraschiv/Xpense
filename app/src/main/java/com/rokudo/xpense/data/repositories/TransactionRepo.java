package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.CollectionUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TransactionRepo {
    private static final String TAG = "TransactionRepo";

    public static TransactionRepo instance;

    private ListenerRegistration transactionListener;
    private ListenerRegistration latestTransListener;

    private final MutableLiveData<List<Transaction>> allTransactionList;
    private final List<Transaction> storedTransactionList;
    private final MutableLiveData<Transaction> latestTransaction;
    private final MutableLiveData<String> addTransactionStatus;
    private final MutableLiveData<String> updateTransactionStatus;

    private String storedWalletId;

    public static TransactionRepo getInstance() {
        if (instance == null) {
            instance = new TransactionRepo();
        }
        return instance;
    }

    public TransactionRepo() {
        this.allTransactionList = new MutableLiveData<>();
        this.storedTransactionList = new ArrayList<>();
        this.addTransactionStatus = new MutableLiveData<>();
        this.latestTransaction = new MutableLiveData<>();
        this.updateTransactionStatus = new MutableLiveData<>();
    }

    public void removeAllTransactionsData() {
        if (transactionListener != null) {
            transactionListener.remove();
        }
        if (latestTransListener != null) {
            latestTransListener.remove();
        }
        allTransactionList.setValue(null);
        storedTransactionList.clear();
        addTransactionStatus.setValue(null);
        latestTransaction.setValue(null);
        updateTransactionStatus.setValue(null);
    }

    public MutableLiveData<List<Transaction>> loadTransactions() {
        return allTransactionList;
    }

    public MutableLiveData<List<Transaction>> loadTransactions(String walletId, Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                23, 59);
        Date end = calendar.getTime();

        if (transactionListener != null) {
            transactionListener.remove();
        }
        transactionListener = DatabaseUtils.getTransactionsRef(walletId)
                .whereGreaterThan("date", startDate)
                .whereLessThan("date", end)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) {
                        Log.e(TAG, "loadTransactions: null or error: ", error);
                        allTransactionList.setValue(new ArrayList<>());
                        storedTransactionList.clear();
                        latestTransaction.setValue(new Transaction());
                    } else {
                        List<Transaction> transactionList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : value) {
                            Transaction transaction = documentSnapshot.toObject(Transaction.class);
                            if (transaction != null) {
                                transaction.setId(documentSnapshot.getId());
                                transactionList.add(transaction);
                            }
                        }
                        latestTransaction.setValue(transactionList.size() == 0 ? new Transaction() : transactionList.get(0));
                        allTransactionList.setValue(transactionList);
                        storedTransactionList.clear();
                        storedTransactionList.addAll(transactionList);
                    }
                });

        return allTransactionList;
    }

    public MutableLiveData<String> addTransaction(Transaction transaction) {

        DatabaseUtils.getTransactionsRef(transaction.getWalletId()).document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(result -> addTransactionStatus.setValue("Success"));

        return addTransactionStatus;
    }

    public MutableLiveData<Transaction> loadLatestTransaction(String walletId) {
        if (latestTransListener == null || storedWalletId == null || !storedWalletId.equals(walletId)) {
            if (latestTransListener != null) {
                latestTransListener.remove();
            }

            storedWalletId = walletId;

            latestTransListener = DatabaseUtils.getTransactionsRef(walletId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(1)
                    .addSnapshotListener((value, error) -> {
                        if (value == null || value.isEmpty()) {
                            return;
                        }
                        Transaction transaction = value.getDocuments().get(0).toObject(Transaction.class);
                        if (transaction == null) {
                            return;
                        }
                        latestTransaction.setValue(transaction);
                    });
        }
        return latestTransaction;
    }

    public MutableLiveData<List<Transaction>> loadTransactionsDateInterval(String walletId, Date start, Date end) {
        MutableLiveData<List<Transaction>> data = new MutableLiveData<>();
        DatabaseUtils.getTransactionsRef(walletId)
                .whereGreaterThan("date", start)
                .whereLessThan("date", end)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(value -> {
                    if (value == null || value.isEmpty()) {
                        data.setValue(new ArrayList<>());
                    } else {
                        List<Transaction> transactionList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : value) {
                            Transaction transaction = documentSnapshot.toObject(Transaction.class);
                            if (transaction != null) {
                                transaction.setId(documentSnapshot.getId());
                                transactionList.add(transaction);
                            }
                        }
                        data.setValue(transactionList);
                    }
                });

        return data;
    }

    public List<Transaction> getStoredTransactionList() {
        return storedTransactionList;
    }

    public MutableLiveData<String> updateTransaction(Transaction transaction) {
        DatabaseUtils.getTransactionsRef(transaction.getWalletId()).document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(result -> updateTransactionStatus.setValue("Success"));

        return updateTransactionStatus;
    }

    public MutableLiveData<Boolean> deleteTransaction(String transactionId, String walletId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        DatabaseUtils.getTransactionsRef(walletId)
                .document(transactionId)
                .delete()
                .addOnCompleteListener(task -> result.setValue(task.isSuccessful()));

        return result;
    }
}
