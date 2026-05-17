package com.rokudo.xpense.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.repositories.BankApiRepo
import com.rokudo.xpense.data.retrofit.models.*

class BankApiViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = BankApiRepo.instance

    fun getToken(): MutableLiveData<Token> = repo.getToken()

    fun refreshToken(token: String): MutableLiveData<String?> = repo.refreshToken(token)

    fun getInstitutionList(): MutableLiveData<List<Institution>?> = repo.getInstitutionList()

    fun createEUA(institutionID: String): MutableLiveData<EndUserAgreement?> = repo.createEUA(institutionID)

    fun createRequisition(institutionID: String, euaId: String, accountSelection: Boolean?): MutableLiveData<Requisition?> =
        repo.createRequisition(institutionID, euaId, accountSelection)

    fun getRequisitionDetails(requisitionID: String): MutableLiveData<Requisition?> =
        repo.getRequisitionDetails(requisitionID)

    fun getAccountDetails(accountId: String): MutableLiveData<AccountDetails?> =
        repo.getAccountDetails(accountId)

    fun getAccountBalances(accountId: String): MutableLiveData<Balances?> =
        repo.getAccountBalances(accountId)

    fun setBalances(balances: Balances?) = repo.setBalances(balances)

    fun getAccountTransactions(accountId: String, dateFrom: String): MutableLiveData<TransactionsResponse?> =
        repo.getAccountTransactions(accountId, dateFrom)

    fun getRequisitionError(): String? = repo.getRequisitionError()

    fun isEUAExpired(): Boolean = repo.isEUA_expired()

    fun getAllRequisitions(): MutableLiveData<RequisitionsResult> = repo.getAllRequisitions()

    fun deleteRequisition(requisitionId: String) = repo.deleteRequisition(requisitionId)

    fun deleteEUA(euaId: String) = repo.deleteEua(euaId)
}

