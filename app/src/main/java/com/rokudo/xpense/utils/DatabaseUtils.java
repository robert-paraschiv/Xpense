package com.rokudo.xpense.utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rokudo.xpense.models.User;

public class DatabaseUtils {
    private static User currentUser;
    public static CollectionReference usersRef = FirebaseFirestore.getInstance()
            .collection("Users");
    public static CollectionReference walletsRef = FirebaseFirestore.getInstance()
            .collection("Wallets");

    public static DocumentReference getYearReference(String walletId, String year) {
        return walletsRef.document(walletId)
                .collection("Statistics")
                .document(year);
    }

    public static CollectionReference getMonthsReference(String walletId, String year) {
        return walletsRef.document(walletId)
                .collection("StatisticsV2")
                .document(year)
                .collection("MonthStatistics");
    }

    public static CollectionReference getTransactionsRef(String walletId) {
        return walletsRef.document(walletId).collection("TransactionsV2");
    }

    public static CollectionReference invitationsRef = FirebaseFirestore.getInstance()
            .collection("Invitations");
    public static CollectionReference bAccountsRef = FirebaseFirestore.getInstance()
            .collection("BAccounts");
    public static StorageReference userPicturesRef = FirebaseStorage.getInstance()
            .getReference("UserPictures");


    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        DatabaseUtils.currentUser = currentUser;
    }
}
