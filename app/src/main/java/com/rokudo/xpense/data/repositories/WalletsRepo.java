package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;

public class WalletsRepo {
    private static final String TAG = "WalletsRepo";

    private static WalletsRepo instance;
    private ListenerRegistration walletListener;

    private final MutableLiveData<ArrayList<Wallet>> allWallets;
    private ArrayList<Wallet> walletList;
    private final MutableLiveData<Wallet> walletMutableLiveData;

    private String lastLoadedWalletId;

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

    public void removeAllWalletsData() {
        if (walletListener != null) {
            walletListener.remove();
        }
        allWallets.setValue(null);
        walletList = new ArrayList<>();
        walletMutableLiveData.setValue(null);
    }

    public MutableLiveData<String> addWallet(Wallet wallet) {
        MutableLiveData<String> mutableLiveData = new MutableLiveData<>();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseUtils.walletsRef.document(wallet.getId()).set(wallet).addOnSuccessListener(documentReference -> {
                Log.d(TAG, "onSuccess: added wallet " + wallet);
                mutableLiveData.setValue("Success");
            });
        }
        return mutableLiveData;
    }

    public MutableLiveData<Wallet> loadWallet(String walletId) {

        if (walletId.isEmpty()) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseFirestore.getInstance().collection("Wallets")
                        .whereArrayContains("users",
                                FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderBy("creation_date", Query.Direction.DESCENDING)
                        .limit(1)
                        .get().addOnSuccessListener(queryDocumentSnapshots -> {
                            Log.d(TAG, "loadWallet: got wallet");
                            if (queryDocumentSnapshots.getDocuments().size() == 0) {
                                return;
                            }
                            Wallet wallet = queryDocumentSnapshots
                                    .getDocuments().get(0).toObject(Wallet.class);
                            if (wallet != null) {
                                walletMutableLiveData.postValue(wallet);
                                setListenerForWallet(wallet.getId());
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e));
            }
        } else {
            setListenerForWallet(walletId);
        }
        return walletMutableLiveData;
    }

    private void setListenerForWallet(String walletId) {

        if (walletListener == null || lastLoadedWalletId == null || !lastLoadedWalletId.equals(walletId)) {
            lastLoadedWalletId = walletId;
            if (walletListener != null) {
                walletListener.remove();
                walletMutableLiveData.setValue(null);
            }
            walletListener = DatabaseUtils.walletsRef.document(walletId)
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) {
                            Log.e(TAG, "getWallet: empty or: ", error);
                        } else {
                            Wallet wallet = value.toObject(Wallet.class);
                            if (wallet != null) {
                                wallet.setId(value.getId());
                                walletMutableLiveData.postValue(wallet);
                            }
                        }
                    });
        }
    }

    public MutableLiveData<ArrayList<Wallet>> getWallets() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseUtils.walletsRef
                    .whereArrayContains("users", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                            Log.e(TAG, "getWallets: empty or null");
                        } else {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
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
