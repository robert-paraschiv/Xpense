package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.WalletsRepo;
import com.rokudo.xpense.models.Wallet;

import java.util.ArrayList;

public class WalletsViewModel extends AndroidViewModel {
    private final WalletsRepo repo;

    public WalletsViewModel(@NonNull Application application) {
        super(application);
        repo = WalletsRepo.getInstance();
    }

    public MutableLiveData<ArrayList<Wallet>> loadWallets() {
        return repo.getWallets();
    }

    public MutableLiveData<Wallet> loadWallet(String walletId) {
        return repo.loadWallet(walletId);
    }

    public MutableLiveData<String> addWallet(Wallet wallet) {
        return repo.addWallet(wallet);
    }
}
