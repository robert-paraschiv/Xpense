package com.rokudo.xpense.data.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.rokudo.xpense.data.retrofit.GetDataService
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance
import com.rokudo.xpense.data.retrofit.models.*
import com.rokudo.xpense.utils.DatabaseUtils
import com.rokudo.xpense.utils.GoCardlessUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BankApiRepo {
    companion object {
        private const val TAG = "BankApiRepo"
        val instance by lazy { BankApiRepo() }
    }

    private val service: GetDataService = RetrofitClientInstance.getInstance().create(GetDataService::class.java)
    private val tokenMutableLiveData = MutableLiveData<Token>()
    private var requisitionError: String? = null
    private var euaExpired = false
    private val balancesMutableLiveData = MutableLiveData<Balances>()
    private val accountTransactionsLiveData = MutableLiveData<TransactionsResponse>()

    fun getToken(): MutableLiveData<Token> {
        service.getToken(GoCardlessUtils.GOCARDLESS_SECRET_KEY_ID, GoCardlessUtils.GOCARDLESS_SECRET_KEY)
            .enqueue(object : Callback<Token> {
                override fun onResponse(call: Call<Token>, response: Response<Token>) {
                    if (response.isSuccessful && response.body() != null) {
                        GoCardlessUtils.TOKEN_VAL = response.body()!!.access ?: ""
                        tokenMutableLiveData.value = response.body()
                    }
                }
                override fun onFailure(call: Call<Token>, t: Throwable) {
                    Log.e(TAG, "onFailure: ${t.message}")
                }
            })
        return tokenMutableLiveData
    }

    fun refreshToken(token: String): MutableLiveData<String> {
        val tokenLiveData = MutableLiveData<String>()
        service.refreshToken(token).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    tokenLiveData.value = response.body()
                } else {
                    tokenLiveData.value = getErrorMessage(response.errorBody())
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                tokenLiveData.value = null
            }
        })
        return tokenLiveData
    }

    fun getInstitutionList(): MutableLiveData<List<Institution>> {
        val data = MutableLiveData<List<Institution>>()
        service.getAllInstitutions().enqueue(object : Callback<List<Institution>> {
            override fun onResponse(call: Call<List<Institution>>, response: Response<List<Institution>>) {
                data.value = if (response.isSuccessful) response.body() else null
            }
            override fun onFailure(call: Call<List<Institution>>, t: Throwable) {
                data.value = null
            }
        })
        return data
    }

    fun createEUA(institutionId: String): MutableLiveData<EndUserAgreement> {
        val data = MutableLiveData<EndUserAgreement>()
        service.createEUA(institutionId, listOf("balances", "details", "transactions"))
            .enqueue(object : Callback<EndUserAgreement> {
                override fun onResponse(call: Call<EndUserAgreement>, response: Response<EndUserAgreement>) {
                    data.value = if (response.isSuccessful) response.body() else null
                }
                override fun onFailure(call: Call<EndUserAgreement>, t: Throwable) {
                    data.value = null
                }
            })
        return data
    }

    fun createRequisition(institutionID: String, euaId: String, accountSelection: Boolean?): MutableLiveData<Requisition> {
        val data = MutableLiveData<Requisition>()
        service.createRequisition(
            institutionID, "https://xpense/launch", euaId, "EN",
            "${DatabaseUtils.currentUser?.phoneNumber}_$institutionID",
            accountSelection, true
        ).enqueue(object : Callback<Requisition> {
            override fun onResponse(call: Call<Requisition>, response: Response<Requisition>) {
                if (response.isSuccessful) {
                    requisitionError = null
                    data.value = response.body()
                } else {
                    requisitionError = getErrorMessage(response.errorBody())
                    data.value = null
                }
            }
            override fun onFailure(call: Call<Requisition>, t: Throwable) {
                requisitionError = t.message
                data.value = null
            }
        })
        return data
    }

    fun getRequisitionDetails(requisitionID: String): MutableLiveData<Requisition> {
        val data = MutableLiveData<Requisition>()
        service.getRequisitionById(requisitionID).enqueue(object : Callback<Requisition> {
            override fun onResponse(call: Call<Requisition>, response: Response<Requisition>) {
                data.value = if (response.isSuccessful) response.body() else null
            }
            override fun onFailure(call: Call<Requisition>, t: Throwable) {
                data.value = null
            }
        })
        return data
    }

    fun getAccountDetails(accountId: String): MutableLiveData<AccountDetails> {
        val data = MutableLiveData<AccountDetails>()
        service.getAccountDetails(accountId).enqueue(object : Callback<AccountDetails> {
            override fun onResponse(call: Call<AccountDetails>, response: Response<AccountDetails>) {
                if (response.isSuccessful) {
                    val details = response.body()
                    details?.account_id = accountId
                    data.value = details
                } else {
                    data.value = null
                }
            }
            override fun onFailure(call: Call<AccountDetails>, t: Throwable) {
                data.value = null
            }
        })
        return data
    }

    fun getAccountBalances(accountId: String): MutableLiveData<Balances> {
        if (GoCardlessUtils.TOKEN_VAL.isEmpty()) {
            service.getToken(GoCardlessUtils.GOCARDLESS_SECRET_KEY_ID, GoCardlessUtils.GOCARDLESS_SECRET_KEY)
                .enqueue(object : Callback<Token> {
                    override fun onResponse(call: Call<Token>, response: Response<Token>) {
                        if (response.isSuccessful && response.body() != null) {
                            GoCardlessUtils.TOKEN_VAL = response.body()!!.access ?: ""
                            tokenMutableLiveData.value = response.body()
                            getAccountBalances(accountId)
                        }
                    }
                    override fun onFailure(call: Call<Token>, t: Throwable) {
                        Log.e(TAG, "onFailure: ${t.message}")
                    }
                })
        } else {
            service.getAccountBalances(accountId).enqueue(object : Callback<Balances> {
                override fun onResponse(call: Call<Balances>, response: Response<Balances>) {
                    if (response.isSuccessful) {
                        val currentVal = balancesMutableLiveData.value
                        val newBody = response.body()
                        if (currentVal == null || newBody?.balances == null ||
                            currentVal.balances?.size != newBody.balances?.size
                        ) {
                            balancesMutableLiveData.value = newBody
                        } else {
                            var updateNeeded = false
                            newBody.balances?.forEachIndexed { i, balance ->
                                try {
                                    val old = currentVal.balances?.get(i)?.balanceAmount?.get("amount")
                                    val new_ = balance.balanceAmount?.get("amount")
                                    if (old != null && old != new_) updateNeeded = true
                                } catch (_: Exception) {}
                            }
                            if (updateNeeded) balancesMutableLiveData.value = newBody
                        }
                    } else {
                        balancesMutableLiveData.value = null
                        if (response.code() == 409) euaExpired = true
                    }
                }
                override fun onFailure(call: Call<Balances>, t: Throwable) {
                    balancesMutableLiveData.value = null
                }
            })
        }
        return balancesMutableLiveData
    }

    fun setBalances(balances: Balances?) {
        balancesMutableLiveData.value = balances
    }

    fun getAccountTransactions(accountId: String, dateFrom: String): MutableLiveData<TransactionsResponse> {
        service.getAccountTransactions(accountId, dateFrom).enqueue(object : Callback<TransactionsResponse> {
            override fun onResponse(call: Call<TransactionsResponse>, response: Response<TransactionsResponse>) {
                if (response.isSuccessful) {
                    requisitionError = null
                    accountTransactionsLiveData.value = response.body()
                } else {
                    requisitionError = getErrorMessage(response.errorBody())
                    accountTransactionsLiveData.value = null
                    if (response.code() == 409) euaExpired = true
                }
            }
            override fun onFailure(call: Call<TransactionsResponse>, t: Throwable) {
                requisitionError = t.message
                accountTransactionsLiveData.value = null
            }
        })
        return accountTransactionsLiveData
    }

    private fun getErrorMessage(response: ResponseBody?): String? {
        if (response == null) return null
        return try {
            val json = JSONObject(response.string())
            json.toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getAllRequisitions(): MutableLiveData<RequisitionsResult> {
        val data = MutableLiveData<RequisitionsResult>()
        service.getRequisitions().enqueue(object : Callback<RequisitionsResult> {
            override fun onResponse(call: Call<RequisitionsResult>, response: Response<RequisitionsResult>) {
                data.value = if (response.isSuccessful) response.body() else null
            }
            override fun onFailure(call: Call<RequisitionsResult>, t: Throwable) {
                Log.e(TAG, "onFailure: ", t)
            }
        })
        return data
    }

    fun deleteRequisition(requisitionId: String) {
        service.deleteRequisition(requisitionId).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {}
            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {}
        })
    }

    fun deleteEua(euaId: String) {
        service.deleteEua(euaId).enqueue(object : Callback<DeleteResponse> {
            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {}
            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {}
        })
    }

    fun getRequisitionError(): String? = requisitionError
    fun isEUA_expired(): Boolean = euaExpired
}

