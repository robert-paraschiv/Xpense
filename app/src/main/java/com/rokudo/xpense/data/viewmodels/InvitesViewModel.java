package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.InvitesRepo;
import com.rokudo.xpense.models.Invitation;

import java.util.List;

public class InvitesViewModel extends AndroidViewModel {
    private final InvitesRepo repo;

    public InvitesViewModel(@NonNull Application application) {
        super(application);
        repo = InvitesRepo.getInstance();
    }

    public MutableLiveData<List<Invitation>> loadInvitations() {
        return repo.loadInvites();
    }
}
