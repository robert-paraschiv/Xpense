package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.BAccountsRepo;
import com.rokudo.xpense.models.BAccount;

import java.util.List;

public class BAccountsViewModel extends AndroidViewModel {
    private BAccountsRepo repo;

    public BAccountsViewModel(@NonNull Application application) {
        super(application);
        repo = BAccountsRepo.getInstance();
    }

    public MutableLiveData<List<BAccount>> getWalletAccounts(String walletId) {
        return repo.getWalletAccounts(walletId);
    }

    public MutableLiveData<List<BAccount>> getUserBAccounts() {
        return repo.getUserBAccounts();
    }

    public void addBAccount(BAccount bAccount) {
        repo.addBAccount(bAccount);
    }
}
