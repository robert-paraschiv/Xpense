package com.rokudo.xpense.data.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.Balances;
import com.rokudo.xpense.data.retrofit.models.DeleteResponse;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.retrofit.models.RequisitionsResult;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankApiRepo {
    private static final String TAG = "BankApiRepo";

    private static BankApiRepo instance;
    GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
    private final MutableLiveData<Token> tokenMutableLiveData = new MutableLiveData<>();
    private String requisitionError;
    private boolean EUA_expired = false;
    //    private final MutableLiveData<List<Institution>> institutionLiveData;
//    private final MutableLiveData<AccountDetails> accountDetailsMutableLiveData;
    private final MutableLiveData<Balances> balancesMutableLiveData;
//    private final MutableLiveData<TransactionsResponse> accountTransactionsLiveData;

    public BankApiRepo() {
//        this.institutionLiveData = new MutableLiveData<>();
        this.balancesMutableLiveData = new MutableLiveData<>();
//        this.accountTransactionsLiveData = new MutableLiveData<>();
//        this.accountDetailsMutableLiveData = new MutableLiveData<>();
    }

    public String getRequisitionError() {
        return requisitionError;
    }

    public boolean isEUA_expired() {
        return EUA_expired;
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

    public MutableLiveData<String> refreshToken(String token) {
        MutableLiveData<String> tokenLiveData = new MutableLiveData<>();

        service.refreshToken(token)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        Log.d(TAG, "onResponse: ");
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                tokenLiveData.setValue(response.body());
                            }
                        } else {
                            tokenLiveData.setValue(getErrorMessage(response.errorBody()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: " + t.getMessage());
                        tokenLiveData.setValue(null);
                    }
                });

        return tokenLiveData;
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

    public MutableLiveData<Requisition> createRequisition(String institutionID, String EUA_ID, Boolean account_selection) {
        MutableLiveData<Requisition> requisitionMutableLiveData = new MutableLiveData<>();

        service.createRequisition(institutionID,
                        "https://xpense/launch",
                        EUA_ID,
                        "EN",
                        DatabaseUtils.getCurrentUser().getPhoneNumber() + "_" + institutionID,
                        account_selection,
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
                            requisitionError = getErrorMessage(response.errorBody());
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
                    AccountDetails accountDetails = response.body();
                    if (accountDetails != null) {
                        accountDetails.setAccount_id(account_id);
                    }
                    accountDetailsMutableLiveData.setValue(accountDetails);
                } else {
                    Log.e(TAG, "onResponse: " + getErrorMessage(response.errorBody()));
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

    public MutableLiveData<Balances> getAccountBalances(String account_id) {
//        MutableLiveData<Balances> balancesMutableLiveData = new MutableLiveData<>();

        service.getAccountBalances(account_id).enqueue(new Callback<Balances>() {
            @Override
            public void onResponse(@NonNull Call<Balances> call, @NonNull Response<Balances> response) {
                if (response.isSuccessful()) {
                    balancesMutableLiveData.setValue(response.body());
                } else {
                    balancesMutableLiveData.setValue(null);
                    Log.e(TAG, "onResponse: " + getErrorMessage(response.errorBody()));
                    if (response.code() == 409) {
                        EUA_expired = true;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Balances> call, @NonNull Throwable t) {
                balancesMutableLiveData.setValue(null);
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return balancesMutableLiveData;
    }

    public void setBalances(Balances balances) {
        balancesMutableLiveData.setValue(balances);
    }

    public MutableLiveData<TransactionsResponse> getAccountTransactions(String account_id, String date_from) {
        MutableLiveData<TransactionsResponse> accountTransactionsLiveData = new MutableLiveData<>();
        service.getAccountTransactions(account_id, date_from)
                .enqueue(new Callback<TransactionsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<TransactionsResponse> call, @NonNull Response<TransactionsResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse: " + response.body());
                            requisitionError = null;
                            accountTransactionsLiveData.setValue(response.body());
                        } else {
                            Log.e(TAG, "onResponse: " + response.message());
                            requisitionError = getErrorMessage(response.errorBody());
                            accountTransactionsLiveData.setValue(null);
                            if (response.code() == 409) {
                                EUA_expired = true;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure: ", t);
                        requisitionError = t.getMessage();
                        accountTransactionsLiveData.setValue(null);
                    }
                });

        return accountTransactionsLiveData;
    }

    private String getErrorMessage(ResponseBody response) {
        if (response == null)
            return null;

        String errorMessage = null;
        try {
            JSONObject jsonObject = new JSONObject(response.string());
            errorMessage = jsonObject.toString();
            Log.e(TAG, "onResponse: " + jsonObject);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

    public MutableLiveData<RequisitionsResult> getAllRequisitions() {
        MutableLiveData<RequisitionsResult> requisitionsResultMutableLiveData = new MutableLiveData<>();

        service.getRequisitions().enqueue(new Callback<RequisitionsResult>() {
            @Override
            public void onResponse(@NonNull Call<RequisitionsResult> call, @NonNull Response<RequisitionsResult> response) {
                if (response.isSuccessful()) {
                    requisitionsResultMutableLiveData.setValue(response.body());
                } else {
                    requisitionsResultMutableLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RequisitionsResult> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });

        return requisitionsResultMutableLiveData;
    }

    public void deleteRequisition(String requisition_id) {
        service.deleteRequisition(requisition_id).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteResponse> call, @NonNull Response<DeleteResponse> response) {
                Log.d(TAG, "onResponse: " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<DeleteResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }

    public void deleteEua(String eua_id) {
        service.deleteEua(eua_id).enqueue(new Callback<DeleteResponse>() {
            @Override
            public void onResponse(@NonNull Call<DeleteResponse> call, @NonNull Response<DeleteResponse> response) {
                Log.d(TAG, "onResponse: " + response.message());
            }

            @Override
            public void onFailure(@NonNull Call<DeleteResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);
            }
        });
    }
}
