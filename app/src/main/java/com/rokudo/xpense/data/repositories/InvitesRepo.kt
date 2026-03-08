package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.rokudo.xpense.models.Invitation
import com.rokudo.xpense.utils.DatabaseUtils

class InvitesRepo {
    companion object {
        private const val TAG = "InvitesRepo"
        val instance by lazy { InvitesRepo() }
    }

    private val invitesList = MutableLiveData<List<Invitation>>()
    private var invitesListener: ListenerRegistration? = null

    fun updateStatus(id: String, status: String) {
        DatabaseUtils.invitationsRef.document(id).update("status", status)
    }

    fun loadInvites(): MutableLiveData<List<Invitation>> {
        invitesListener?.remove()

        val currentUser = DatabaseUtils.currentUser
        if (currentUser == null || currentUser.phoneNumber.isNullOrEmpty()) {
            Log.d(TAG, "loadInvites: current user or phone number is null")
            return invitesList
        }

        invitesListener = DatabaseUtils.invitationsRef
            .whereEqualTo("invited_person_phone_number", currentUser.phoneNumber)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null || value.isEmpty) {
                    Log.d(TAG, "loadInvites: null or empty")
                    return@addSnapshotListener
                }
                val invitationList = mutableListOf<Invitation>()
                for (doc in value) {
                    val invitation = doc.toObject(Invitation::class.java)
                    val index = invitationList.indexOf(invitation)
                    if (index >= 0) {
                        invitationList[index] = invitation
                    } else {
                        invitationList.add(invitation)
                    }
                }
                invitesList.value = invitationList
            }
        return invitesList
    }
}

