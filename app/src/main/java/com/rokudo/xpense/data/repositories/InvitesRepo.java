package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.rokudo.xpense.models.Invitation;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.List;

public class InvitesRepo {
    private static final String TAG = "InvitesRepo";

    public static InvitesRepo instance;

    private final MutableLiveData<List<Invitation>> invitesList;
    private ListenerRegistration invitesListener;

    public InvitesRepo() {
        this.invitesList = new MutableLiveData<>();
    }

    public static InvitesRepo getInstance() {
        if (instance == null) {
            instance = new InvitesRepo();
        }
        return instance;
    }

    public MutableLiveData<List<Invitation>> loadInvites() {
        if (invitesListener != null) {
            invitesListener.remove();
        }
        invitesListener = DatabaseUtils.invitationsRef
                .whereEqualTo("invited_person_phone_number", DatabaseUtils.getCurrentUser().getPhoneNumber())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) {
                        Log.d(TAG, "loadInvites: null or empty");
                        return;
                    }

                    List<Invitation> invitationList = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : value) {
                        Invitation invitation = documentSnapshot.toObject(Invitation.class);
                        if (!invitationList.contains(invitation)) {
                            invitationList.add(invitation);
                        } else {
                            invitationList.set(invitationList.indexOf(invitation), invitation);
                        }
                    }

                    invitesList.setValue(invitationList);
                });

        return invitesList;
    }

}
