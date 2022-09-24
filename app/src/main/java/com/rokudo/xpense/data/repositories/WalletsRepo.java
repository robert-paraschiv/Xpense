package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;

public class WalletsRepo {
    private static final String TAG = "WalletsRepo";

    private static WalletsRepo instance;

    private ListenerRegistration walletsListener;

    private final MutableLiveData<ArrayList<Wallet>> allWallets;
    private final ArrayList<Wallet> walletList;

    public static WalletsRepo getInstance() {
        if (instance == null) {
            instance = new WalletsRepo();
        }
        return instance;
    }

    public WalletsRepo() {
        this.allWallets = new MutableLiveData<>();
        this.walletList = new ArrayList<>();
    }

    public MutableLiveData<ArrayList<Wallet>> getWallets() {
        if (walletsListener != null) {
            walletsListener.remove();
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            walletsListener = DatabaseUtils.walletsRef
                    .whereArrayContains("users", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) {
                            Log.e(TAG, "getWallets: empty or: ", error);
                        } else {
                            for (DocumentSnapshot documentSnapshot : value) {
                                Wallet wallet = documentSnapshot.toObject(Wallet.class);
                                if (wallet != null) {
                                    wallet.setId(documentSnapshot.getId());
                                    handleWalletEvent(wallet);
                                }
                            }
                            allWallets.setValue(walletList);
                        }
                    });
        }
        return allWallets;
    }

    private void handleWalletEvent(Wallet wallet) {
        if (walletList.contains(wallet)) {
            // TODO: 9/24/2022 Check if something is different
            walletList.set(walletList.indexOf(wallet), wallet);
        } else {
            walletList.add(wallet);
        }
    }
}
