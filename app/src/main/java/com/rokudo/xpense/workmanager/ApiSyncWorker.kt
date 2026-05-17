package com.rokudo.xpense.workmanager

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rokudo.xpense.data.retrofit.GetDataService
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance
import com.rokudo.xpense.data.retrofit.models.Balances
import com.rokudo.xpense.utils.GsonHelper
import com.rokudo.xpense.utils.PrefsUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiSyncWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object {
        private const val TAG = "ApiSyncWorker"
    }

    override fun doWork(): Result {
        val bAccount = PrefsUtils.getSavedObjectFromPreference(applicationContext)
        val service = RetrofitClientInstance.getInstance().create(GetDataService::class.java)

        if (bAccount != null && !bAccount.accounts.isNullOrEmpty()) {
            service.getAccountBalances(bAccount.accounts!![0]).enqueue(object : Callback<Balances> {
                override fun onResponse(call: Call<Balances>, response: Response<Balances>) {
                    if (response.isSuccessful) {
                        val output = Data.Builder()
                            .putString("response", GsonHelper.serializeBalancesToJson(response.body()))
                            .build()
                        Result.success(output)
                    }
                }
                override fun onFailure(call: Call<Balances>, t: Throwable) {
                    Log.e(TAG, "onFailure: ", t)
                }
            })
        }
        return Result.success()
    }
}

