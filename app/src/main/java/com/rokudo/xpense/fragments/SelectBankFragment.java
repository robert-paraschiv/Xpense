package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.utils.NordigenUtils.TOKEN_PREFS_NAME;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.adapters.BanksAdapter;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.data.retrofit.GetDataService;
import com.rokudo.xpense.data.retrofit.RetrofitClientInstance;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.databinding.FragmentConnectToBankBinding;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.utils.NordigenUtils;
import com.rokudo.xpense.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectBankFragment extends Fragment implements BanksAdapter.OnBankTapListener {
    private static final String TAG = "SelectBankFragment";

    private FragmentConnectToBankBinding binding;
    private final List<Institution> bankList = new ArrayList<>();
    private BanksAdapter adapter;
    private BAccount bAccount = new BAccount();
    private String walletId;
    private BankApiViewModel bankApiViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentConnectToBankBinding.inflate(inflater, container, false);

        bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);

        initOnClicks();
        handleArgsPassed();
        buildRv();
        getInstitutionsList();

        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
    }

    private void handleArgsPassed() {
        SelectBankFragmentArgs args = SelectBankFragmentArgs.fromBundle(requireArguments());
        walletId = args.getWalletId();
        bAccount.setWalletIds(Collections.singletonList(walletId));
    }

    private void buildRv() {
        adapter = new BanksAdapter(bankList);
        binding.banksRV.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.banksRV.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    private void getInstitutionsList() {
        String token = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString(TOKEN_PREFS_NAME, "");
        if (token.isEmpty()) {
            getToken();
        } else {
            //get Institutions List
            //get EUA for user

            bankApiViewModel.getInstitutionList().observe(getViewLifecycleOwner(), institutions -> {
                if (institutions == null ) {
                    getToken();
                    Log.d(TAG, "getInstitutionsList: null or empty");
                } else {
                    binding.banksRV.setVisibility(View.VISIBLE);
                    binding.progressIndicator.setVisibility(View.GONE);

                    for (Institution institution : institutions) {
                        bankList.add(institution);
                        adapter.notifyItemInserted(bankList.size() - 1);
                    }
                }
            });
        }
    }

    private void getToken() {
        bankApiViewModel.getToken().observe(getViewLifecycleOwner(), token -> {
            if (token == null) {
                Log.d(TAG, "getToken: null");
            } else {
                NordigenUtils.TOKEN_VAL = token.getAccess();
                PrefsUtils.setToken(requireContext(), NordigenUtils.TOKEN_VAL);
                getInstitutionsList();
            }
        });
    }

    @Override
    public void onClick(Institution institution) {
        bAccount.setInstitutionId(institution.getId());
        bAccount.setBankName(institution.getName());
        bAccount.setBankPic(institution.getLogo());
        String EUA_ID = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("EUA" + institution.getId(), "");
        if (EUA_ID.isEmpty()) {
            //Create EUA

            bankApiViewModel.createEUA(institution.getId()).observe(getViewLifecycleOwner(), endUserAgreement -> {
                if (endUserAgreement == null) {
                    Log.d(TAG, "onClick: EUA Null");
                } else {
                    PrefsUtils.setString(requireContext(),
                            "EUA" + institution.getId(),
                            endUserAgreement.getId());
                    bAccount.setEUA_id(endUserAgreement.getId());
                    getRequisition(institution, endUserAgreement.getId());
                }
            });

        } else {
            bAccount.setEUA_id(EUA_ID);
            getRequisition(institution, EUA_ID);
        }
    }

    private void getRequisition(Institution institution, String EUA_ID) {
        String REQUISITION = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("REQUISITION" + institution.getId(), "");
        GetDataService service = RetrofitClientInstance.geInstance().create(GetDataService.class);
        if (REQUISITION.isEmpty()) {
            bankApiViewModel.createRequisition(institution.getId(), EUA_ID)
                    .observe(getViewLifecycleOwner(), requisition -> {
                        if (requisition == null) {
                            Log.e(TAG, "onResponse: requisition null ");
                        } else {
                            PrefsUtils.setString(requireContext(), "REQUISITION" + institution.getId(),
                                    requisition.getId());
                            bAccount.setRequisition_id(requisition.getId());
                            getRequisitionDetails(REQUISITION);
                        }
                    });

        } else {
            //Get requisition details
            String ACC_ID = requireContext()
                    .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                    .getString("ACC_ID" + REQUISITION, "");
            bAccount.setRequisition_id(REQUISITION);
            if (ACC_ID.isEmpty()) {
                getRequisitionDetails(REQUISITION);
            } else {
                Map<String, String> accounts = new HashMap<>();
                accounts.put(ACC_ID, ACC_ID);
                bAccount.setAccounts(accounts);
                getAccountDetails(ACC_ID);
            }
        }
    }

    private void getAccountDetails(String acc_id) {
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

    private void getRequisitionDetails(String requisitionID) {
        bankApiViewModel.getRequisitionDetails(requisitionID)
                .observe(getViewLifecycleOwner(), requisition -> {
                    if (requisition == null) {
                        Log.e(TAG, "onFailure: requisition details null");
                    } else {
                        getAccounts(requisition);
                    }
                });
    }

    private void getAccounts(Requisition requisition) {
        if (requisition.getAccounts() != null && requisition.getAccounts().length > 0) {
            Log.d(TAG, "getAccounts: length > 0");
            PrefsUtils.setString(requireContext(), "ACC_ID" + requisition.getId(), requisition.getAccounts()[0]);
            Map<String, String> accounts = new HashMap<>();
            for (int i = 0; i < requisition.getAccounts().length; i++) {
                accounts.put(requisition.getAccounts()[i], requisition.getAccounts()[i]);
            }
            bAccount.setAccounts(accounts);
            getAccountDetails(requisition.getAccounts()[0]);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requisition.getLink())));
            Log.e(TAG, "getAccounts: no accounts");
            Toast.makeText(requireContext(), "No accounts", Toast.LENGTH_SHORT).show();
        }
    }
}