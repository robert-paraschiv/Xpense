package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.TransactionRepo;
import com.rokudo.xpense.models.Transaction;

import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepo repo;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repo = TransactionRepo.getInstance();
    }

    public MutableLiveData<List<Transaction>> loadTransactions(String walletId, Date date) {
        return repo.loadTransactions(walletId, date);
    }

    public MutableLiveData<List<Transaction>> loadTransactions() {
        return repo.loadTransactions();
    }

    public MutableLiveData<List<Transaction>> loadTransactionsDateInterval(String walletId, Date start, Date end) {
        return repo.loadTransactionsDateInterval(walletId, start, end);
    }

    public MutableLiveData<String> addTransaction(Transaction transaction) {
        return repo.addTransaction(transaction);
    }

    public MutableLiveData<Transaction> loadLatestTransaction() {
        return repo.loadLatestTransaction();
    }

    public List<Transaction> getStoredTransactionList() {
        return repo.getStoredTransactionList();
    }

    public MutableLiveData<String> updateTransaction(Transaction transaction) {
        return repo.updateTransaction(transaction);
    }

    public void removeAllData() {
        repo.removeAllTransactionsData();
    }
}
