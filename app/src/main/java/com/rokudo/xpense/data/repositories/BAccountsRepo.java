package com.rokudo.xpense.data.repositories;

import static com.rokudo.xpense.utils.DatabaseUtils.bAccountsRef;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

public class BAccountsRepo {
    private static final String TAG = "BAccountsRepo";

    private static BAccountsRepo instance;

    private ListenerRegistration bAccountListener;

    private final MutableLiveData<List<BAccount>> bAccountLiveData;
    private final MutableLiveData<List<BAccount>> userBAccountLiveData;

    public BAccountsRepo() {
        this.bAccountLiveData = new MutableLiveData<>();
        this.userBAccountLiveData = new MutableLiveData<>();
    }

    public static BAccountsRepo getInstance() {
        if (instance == null) {
            instance = new BAccountsRepo();
        }
        return instance;
    }

    public void addBAccount(BAccount bAccount){
        bAccount.setId(bAccountsRef.document().getId());
        bAccountsRef.document(bAccount.getId()).set(bAccount)
                .addOnSuccessListener(unused -> Log.d(TAG, "onSuccess: added"));
    }

    public MutableLiveData<List<BAccount>> getUserBAccounts() {
        bAccountsRef
                .whereEqualTo("owner_id", DatabaseUtils.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        bAccountLiveData.setValue(new ArrayList<>());
                    } else {
                        List<BAccount> bAccountList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            BAccount bAccount = documentSnapshot.toObject(BAccount.class);
                            if (bAccount != null) {
                                bAccount.setId(documentSnapshot.getId());
                                bAccountList.add(bAccount);
                            }
                        }
                        userBAccountLiveData.setValue(bAccountList);
                    }
                });

        return userBAccountLiveData;
    }

    public MutableLiveData<List<BAccount>> getWalletAccounts(String walletId) {
        if (bAccountListener != null) {
            bAccountListener.remove();
        }
        bAccountListener = bAccountsRef
                .whereArrayContains("walletIds", walletId)
                .addSnapshotListener((value, error) -> {
                    if (value == null || value.isEmpty()) {
                        bAccountLiveData.setValue(new ArrayList<>());
                    } else {
                        List<BAccount> bAccountList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : value) {
                            BAccount bAccount = documentSnapshot.toObject(BAccount.class);
                            if (bAccount != null) {
                                bAccount.setId(documentSnapshot.getId());
                                bAccountList.add(bAccount);
                            }
                        }
                        bAccountLiveData.setValue(bAccountList);
                    }
                });

        return bAccountLiveData;
    }

}
