package com.rokudo.xpense.data.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.repositories.BankApiRepo;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.retrofit.models.RequisitionsResult;
import com.rokudo.xpense.data.retrofit.models.Token;
import com.rokudo.xpense.data.retrofit.models.TransactionsResponse;

import java.util.List;

public class BankApiViewModel extends AndroidViewModel {
    private final BankApiRepo repo;

    public BankApiViewModel(@NonNull Application application) {
        super(application);
        repo = BankApiRepo.getInstance();
    }

    public MutableLiveData<Token> getToken() {
        return repo.getToken();
    }

    public MutableLiveData<List<Institution>> getInstitutionList() {
        return repo.getInstitutionList();
    }

    public MutableLiveData<EndUserAgreement> createEUA(String institutionID) {
        return repo.createEUA(institutionID);
    }

    public MutableLiveData<Requisition> createRequisition(String institutionID, String EUA_ID,Boolean account_selection) {
        return repo.createRequisition(institutionID, EUA_ID,account_selection);
    }

    public MutableLiveData<Requisition> getRequisitionDetails(String requisitionID){
        return repo.getRequisitionDetails(requisitionID);
    }

    public MutableLiveData<AccountDetails> getAccountDetails(String account_id){
        return repo.getAccountDetails(account_id);
    }

    public MutableLiveData<TransactionsResponse> getAccountTransactions(String account_id){
        return repo.getAccountTransactions(account_id);
    }

    public String getRequisitionError(){
        return repo.getRequisitionError();
    }

    public MutableLiveData<RequisitionsResult> getAllRequisitions() {
        return repo.getAllRequisitions();
    }

    public void deleteRequisition(String requisition_ID){
        repo.deleteRequisition(requisition_ID);
    }
}
