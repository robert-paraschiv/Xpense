package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.TransactionRepo;
import com.rokudo.xpense.models.Transaction;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepo repo;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repo = TransactionRepo.getInstance();
    }

    public MutableLiveData<List<Transaction>> loadTransactions(String walletId) {
        return repo.loadTransactions(walletId);
    }

    public MutableLiveData<String> addTransaction(Transaction transaction) {
        return repo.addTransaction(transaction);
    }

    public MutableLiveData<Transaction> loadLatestTransaction() {
        return repo.loadLatestTransaction();
    }


}
