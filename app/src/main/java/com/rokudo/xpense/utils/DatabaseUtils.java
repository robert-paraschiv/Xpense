package com.rokudo.xpense.utils;

import com.google.firebase.firestore.CollectionReference;
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
    public static CollectionReference transactionsRef = FirebaseFirestore.getInstance()
            .collection("TestTransactions");
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
