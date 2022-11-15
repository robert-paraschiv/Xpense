package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;
import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.retrofit.models.Token;
import com.rokudo.xpense.data.retrofit.models.TransactionsResponse;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.NordigenUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankApiRepo {
    private static final String TAG = "BankApiRepo";

    private static BankApiRepo instance;
    GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
    private final MutableLiveData<Token> tokenMutableLiveData = new MutableLiveData<>();
    private String requisitionError;

    public String getRequisitionError() {
        return requisitionError;
    }

    public static BankApiRepo getInstance() {
        if (instance == null) {
            instance = new BankApiRepo();
        }
        return instance;
    }

    public MutableLiveData<Token> getToken() {
        service.getToken(NordigenUtils.NORDIGEN_SECRET_KEY_ID, NordigenUtils.NORDIGEN_SECRET_KEY)
                .enqueue(new Callback<Token>() {
                    @Override
                    public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                        Log.d(TAG, "onResponse: ");
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                NordigenUtils.TOKEN_VAL = response.body().getAccess();
                                tokenMutableLiveData.setValue(response.body());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Token> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });

        return tokenMutableLiveData;
    }

    public MutableLiveData<List<Institution>> getInstitutionList() {
        MutableLiveData<List<Institution>> institutionLiveData = new MutableLiveData<>();

        service.getAllInstitutions()
                .enqueue(new Callback<List<Institution>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Institution>> call, @NonNull Response<List<Institution>> response) {
                        Log.d(TAG, "onResponse: ");
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                institutionLiveData.setValue(response.body());
                            }
                        } else {
                            institutionLiveData.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Institution>> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        institutionLiveData.setValue(null);
                    }
                });

        return institutionLiveData;
    }

    public MutableLiveData<EndUserAgreement> createEUA(String institutionId) {
        MutableLiveData<EndUserAgreement> agreementMutableLiveData = new MutableLiveData<>();

        List<String> scopeList =
                new ArrayList<>(Arrays.asList("balances", "details", "transactions"));
        service.createEUA(institutionId, scopeList)
                .enqueue(new Callback<EndUserAgreement>() {
                    @Override
                    public void onResponse(@NonNull Call<EndUserAgreement> call, @NonNull Response<EndUserAgreement> response) {
                        Log.d(TAG, "onResponse: ");
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse: ");
                            agreementMutableLiveData.setValue(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EndUserAgreement> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        agreementMutableLiveData.setValue(null);
                    }
                });

        return agreementMutableLiveData;
    }

    public MutableLiveData<Requisition> createRequisition(String institutionID, String EUA_ID) {
        MutableLiveData<Requisition> requisitionMutableLiveData = new MutableLiveData<>();

        service.createRequisition(institutionID,
                        "https://xpense/launch",
                        EUA_ID,
                        "EN",
                        DatabaseUtils.getCurrentUser().getPhoneNumber() + "_" + EUA_ID,
                        true,
                        true)
                .enqueue(new Callback<Requisition>() {
                    @Override
                    public void onResponse(@NonNull Call<Requisition> call, @NonNull Response<Requisition> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse: success ");
                            requisitionError = null;
                            requisitionMutableLiveData.setValue(response.body());
                        } else {
                            Log.e(TAG, "onResponse: failed " + response.message());
                            requisitionError = response.errorBody() != null ? response.errorBody().toString() : null;
                            requisitionMutableLiveData.setValue(null);
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<Requisition> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        requisitionError = t.getMessage();
                        requisitionMutableLiveData.setValue(null);
                    }
                });

        return requisitionMutableLiveData;
    }

    public MutableLiveData<Requisition> getRequisitionDetails(String requisitionID) {
        MutableLiveData<Requisition> requisitionMutableLiveData = new MutableLiveData<>();

        service.getRequisitionById(requisitionID).enqueue(new Callback<Requisition>() {
            @Override
            public void onResponse(@NonNull Call<Requisition> call, @NonNull Response<Requisition> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: success ");
                    if (response.body() != null) {
                        requisitionMutableLiveData.setValue(response.body());
                    }

                } else {
                    Log.e(TAG, "onResponse: failed " + response.message());
                    requisitionMutableLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisition> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
                requisitionMutableLiveData.setValue(null);
            }
        });

        return requisitionMutableLiveData;
    }

    public MutableLiveData<AccountDetails> getAccountDetails(String account_id) {
        MutableLiveData<AccountDetails> accountDetailsMutableLiveData = new MutableLiveData<>();

        service.getAccountDetails(account_id).enqueue(new Callback<AccountDetails>() {
            @Override
            public void onResponse(@NonNull Call<AccountDetails> call, @NonNull Response<AccountDetails> response) {
                Log.d(TAG, "onResponse: ");
                if (response.isSuccessful()) {
                    accountDetailsMutableLiveData.setValue(response.body());
                } else {
                    Log.e(TAG, "onResponse: " + response.message());
                    accountDetailsMutableLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccountDetails> call, @NonNull Throwable t) {
                Log.e(TAG, "onResponse: " + t.getMessage());
                accountDetailsMutableLiveData.setValue(null);
            }
        });

        return accountDetailsMutableLiveData;
    }

    public MutableLiveData<TransactionsResponse> getAccountTransactions(String account_id) {
        MutableLiveData<TransactionsResponse> accountTransactionsLiveData = new MutableLiveData<>();

        service.getAccountTransactions(account_id)
                .enqueue(new Callback<TransactionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TransactionsResponse> call, @NonNull Response<TransactionsResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse: " + response.body());
                            accountTransactionsLiveData.setValue(response.body());
                        } else {
                            Log.e(TAG, "onResponse: " + response.message());
                            accountTransactionsLiveData.setValue(null);
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                Log.e(TAG, "onResponse: " + jsonObject);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "onResponse: " + t.getMessage());
                        accountTransactionsLiveData.setValue(null);
                    }
                });

        return accountTransactionsLiveData;
    }
}
