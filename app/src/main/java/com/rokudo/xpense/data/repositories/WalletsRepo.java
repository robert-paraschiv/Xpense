package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.Objects;

public class WalletsRepo {
    private static final String TAG = "WalletsRepo";

    private static WalletsRepo instance;

    private ListenerRegistration walletsListener;
    private ListenerRegistration walletListener;

    private final MutableLiveData<ArrayList<Wallet>> allWallets;
    private final ArrayList<Wallet> walletList;
    private final MutableLiveData<Wallet> walletMutableLiveData;

    public static WalletsRepo getInstance() {
        if (instance == null) {
            instance = new WalletsRepo();
        }
        return instance;
    }

    public WalletsRepo() {
        this.allWallets = new MutableLiveData<>();
        this.walletList = new ArrayList<>();
        this.walletMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> addWallet(Wallet wallet) {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseUtils.walletsRef.document(wallet.getId()).set(wallet).addOnSuccessListener(documentReference ->
                    Log.d(TAG, "onSuccess: added wallet " + wallet));
            mutableLiveData.setValue("Success");
        }
        return mutableLiveData;
    }

    public MutableLiveData<Wallet> loadWallet(String walletId) {
        if (walletListener != null) {
            walletListener.remove();
        }
        if (walletId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                DatabaseUtils.walletsRef.
                        whereArrayContains("users",
                                FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .limit(1)
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            Wallet wallet = queryDocumentSnapshots
                                    .getDocuments().get(0).toObject(Wallet.class);
                            if (wallet != null) {
                                walletMutableLiveData.setValue(wallet);
                            }
                        });
            }
        } else {
            walletListener = DatabaseUtils.walletsRef.document(walletId)
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) {
                            Log.e(TAG, "getWallet: empty or: ", error);
                        } else {
                            Wallet wallet = value.toObject(Wallet.class);
                            if (wallet != null) {
                                wallet.setId(value.getId());
                                walletMutableLiveData.setValue(wallet);
                            }
                        }
                    });
        }
        return walletMutableLiveData;
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
