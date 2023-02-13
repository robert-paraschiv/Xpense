package com.rokudo.xpense.workmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.Balances;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.utils.GsonHelper;
import com.rokudo.xpense.utils.PrefsUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiSyncWorker extends Worker {
    private static final String TAG = "ApiSyncWorker";

    public ApiSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        BAccount bAccount = PrefsUtils.getSavedObjectFromPreference(getApplicationContext());
        GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
        if (bAccount != null) {
            service.getAccountBalances(bAccount.getAccounts().get(0)).enqueue(new Callback<Balances>() {
                @Override
                public void onResponse(@NonNull Call<Balances> call, @NonNull Response<Balances> response) {
                    if (response.isSuccessful()) {
                        Data output = new Data.Builder()
                                .putString("response", GsonHelper.serializeBalancesToJson(response.body()))
                                .build();
//                        setProgressAsync(response);
                        Result.success(output);
                    } else {
//                        Log.e(TAG, "onResponse: " + getErrorMessage(response.errorBody()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Balances> call, @NonNull Throwable t) {
//                    balancesMutableLiveData.setValue(null);
                    Log.e(TAG, "onFailure: ", t);
                }
            });
        }


//        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE);
//        PrefsUtils.setInt(getApplicationContext(), "work", sharedPreferences.getInt("work", 0) + 1);
        return Result.success();
    }
}
