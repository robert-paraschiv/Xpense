package com.rokudo.xpense.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.adapters.BanksAdapter;
import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.EUAResponse;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Token;
import com.rokudo.xpense.databinding.FragmentConnectToBankBinding;
import com.rokudo.xpense.utils.NordigenUtils;
import com.rokudo.xpense.utils.PrefsUtils;

import java.util.ArrayList;
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
                .getString("token", "");
        if (token.isEmpty()) {
            getToken();
        } else {
            //get Institutions List
            //get EUA for user

            NordigenUtils.token = token;
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
                        NordigenUtils.token = response.body().getAccess();
                        PrefsUtils.setToken(requireContext(), NordigenUtils.token);
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
                .getString(institution.getId(), "");
        if (EUA_ID.isEmpty()) {
            //Create EUA

            GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
            Call<EndUserAgreement> call = service.createEUA(institution.getId());

            call.enqueue(new Callback<EndUserAgreement>() {
                @Override
                public void onResponse(@NonNull Call<EndUserAgreement> call, @NonNull Response<EndUserAgreement> response) {
                    Log.d(TAG, "onResponse: ");
                    if (response.isSuccessful()) {
                        Log.d(TAG, "onResponse: ");
                        PrefsUtils.setString(requireContext(), institution.getId(), response.body() != null ? response.body().getId() : "");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<EndUserAgreement> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                }
            });
        } else {
            // TODO: 11/3/2022 get Requisition 

        }

    }
}