package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.utils.NordigenUtils.TOKEN_PREFS_NAME;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.adapters.BanksAdapter;
import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.retrofit.models.Token;
import com.rokudo.xpense.databinding.FragmentConnectToBankBinding;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.NordigenUtils;
import com.rokudo.xpense.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectBankFragment extends Fragment implements BanksAdapter.OnBankTapListener {
    private static final String TAG = "ConnectToBankFragment";

    private FragmentConnectToBankBinding binding;
    private final List<Institution> bankList = new ArrayList<>();
    private BanksAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentConnectToBankBinding.inflate(inflater, container, false);

        buildRv();
        callInstitutionsList();

        return binding.getRoot();
    }

    private void buildRv() {
        adapter = new BanksAdapter(bankList);
        binding.banksRV.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.banksRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void callInstitutionsList() {
        String token = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString(TOKEN_PREFS_NAME, "");
        if (token.isEmpty()) {
            getToken();
        } else {
            //get Institutions List
            //get EUA for user

            NordigenUtils.TOKEN_VAL = token;
            GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
            Call<List<Institution>> call = service.getAllInstitutions();

            call.enqueue(new Callback<List<Institution>>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<List<Institution>> call, @NonNull Response<List<Institution>> response) {
                    Log.d(TAG, "onResponse: ");
                    if (response.isSuccessful()) {
                        if (response.body() != null) {

                            binding.banksRV.setVisibility(View.VISIBLE);
                            binding.progressIndicator.setVisibility(View.GONE);

                            for (Institution institution : response.body()) {
                                bankList.add(institution);
                                adapter.notifyItemInserted(bankList.size() - 1);
                            }
                        }
                    } else {
                        getToken();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Institution>> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        }
    }

    private void getToken() {
        GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
        Call<Token> call = service.getToken(NordigenUtils.NORDIGEN_SECRET_KEY_ID, NordigenUtils.NORDIGEN_SECRET_KEY);

        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(@NonNull Call<Token> call, @NonNull Response<Token> response) {
                Log.d(TAG, "onResponse: ");
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        NordigenUtils.TOKEN_VAL = response.body().getAccess();
                        PrefsUtils.setToken(requireContext(), NordigenUtils.TOKEN_VAL);
                        callInstitutionsList();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Token> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    @Override
    public void onClick(Institution institution) {
        String EUA_ID = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("EUA" + institution.getId(), "");
        if (EUA_ID.isEmpty()) {
            //Create EUA

            GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
            List<String> scopeList =
                    new ArrayList<>(Arrays.asList("balances", "details", "transactions"));
            Call<EndUserAgreement> call = service.createEUA(institution.getId(), scopeList);

            call.enqueue(new Callback<EndUserAgreement>() {
                @Override
                public void onResponse(@NonNull Call<EndUserAgreement> call, @NonNull Response<EndUserAgreement> response) {
                    Log.d(TAG, "onResponse: ");
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: ");
                        PrefsUtils.setString(requireContext(),
                                "EUA" + institution.getId(),
                                response.body() != null ? response.body().getId() : "");
                        getRequisition(institution, response.body().getId());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EndUserAgreement> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        } else {
            getRequisition(institution, EUA_ID);
        }

    }

    private void getRequisition(Institution institution, String EUA_ID) {
        String REQUISITION = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("REQUISITION" + institution.getId(), "");
        GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
        if (REQUISITION.isEmpty()) {
            Call<Requisition> call = service.createRequisition(institution.getId(),
                    "http://localhost",
                    EUA_ID,
                    "EN",
                    institution.getId() + DatabaseUtils.getCurrentUser().getUid());

            call.enqueue(new Callback<Requisition>() {
                @Override
                public void onResponse(@NonNull Call<Requisition> call, @NonNull Response<Requisition> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: success ");
                        PrefsUtils.setString(requireContext(), "REQUISITION" + institution.getId(),
                                response.body() != null ? response.body().getId() : "");
                        getRequisitionDetails(REQUISITION, service);

                    } else {
                        Log.e(TAG, "onResponse: failed " + response.message());
                    }

                }

                @Override
                public void onFailure(@NonNull Call<Requisition> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });

        } else {
            //Get requisition details
            String ACC_ID = requireContext()
                    .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                    .getString("ACC_ID" + REQUISITION, "");
            if (ACC_ID.isEmpty()) {
                getRequisitionDetails(REQUISITION, service);
            } else {
                getAccountDetails(ACC_ID, service);
            }

        }
    }

    private void getAccountDetails(String acc_id, GetDataService service) {
//        Call<AccountDetails> call = service.getAccountDetails(acc_id);
//        call.enqueue(new Callback<AccountDetails>() {
//            @Override
//            public void onResponse(@NonNull Call<AccountDetails> call, @NonNull Response<AccountDetails> response) {
//                Log.d(TAG, "onResponse: ");
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "onResponse: " + response.body());
//                } else {
//                    Log.e(TAG, "onResponse: " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AccountDetails> call, @NonNull Throwable t) {
//                Log.e(TAG, "onResponse: " + t.getMessage());
//            }
//        });

        Navigation.findNavController(binding.getRoot())
                .navigate(SelectBankFragmentDirections.actionConnectToBankFragmentToAccTransListFragment(acc_id));


//        Call<TransactionsResponse> call = service.getAccountTransactions(acc_id);
//        call.enqueue(new Callback<TransactionsResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<TransactionsResponse> call, @NonNull Response<TransactionsResponse> response) {
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "onResponse: " + response.body());
//                } else {
//                    Log.e(TAG, "onResponse: " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<TransactionsResponse> call, @NonNull Throwable t) {
//                Log.e(TAG, "onResponse: " + t.getMessage());
//            }
//        });

    }

    private void getRequisitionDetails(String REQUISITION, GetDataService service) {
        service.getRequisitionById(REQUISITION).enqueue(new Callback<Requisition>() {
            @Override
            public void onResponse(@NonNull Call<Requisition> call, @NonNull Response<Requisition> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: success ");
                    if (response.body() != null) {
                        getAccounts(response.body(), service);
                    }

                } else {
                    Log.e(TAG, "onResponse: failed " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Requisition> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void getAccounts(Requisition requisition, GetDataService service) {
        if (requisition.getAccounts() != null && requisition.getAccounts().length > 0) {
            Log.d(TAG, "getAccounts: length > 0");
            PrefsUtils.setString(requireContext(), "ACC_ID" + requisition.getId(), requisition.getAccounts()[0]);
            getAccountDetails(requisition.getAccounts()[0], service);
        } else{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requisition.getLink())));
            Log.e(TAG, "getAccounts: no accounts");
            Toast.makeText(requireContext(), "No accounts", Toast.LENGTH_SHORT).show();
        }
    }
}