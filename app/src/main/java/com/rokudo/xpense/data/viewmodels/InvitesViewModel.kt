package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.InvitesRepo
import com.rokudo.xpense.models.Invitation

class InvitesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = InvitesRepo.instance

    fun loadInvitations(): MutableLiveData<List<Invitation>> = repo.loadInvites()

    fun updateStatus(id: String, status: String) = repo.updateStatus(id, status)
}

