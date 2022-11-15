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
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.viewmodels.BAccountsViewModel;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.databinding.FragmentConnectToBankBinding;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.NordigenUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.dialogs.BankAccsListDialog;
import com.rokudo.xpense.utils.dialogs.UploadingDialog;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SelectBankFragment extends Fragment implements BanksAdapter.OnBankTapListener {
    private static final String TAG = "SelectBankFragment";

    private FragmentConnectToBankBinding binding;
    private final List<Institution> bankList = new ArrayList<>();
    private BanksAdapter adapter;
    private final BAccount bAccount = new BAccount();
    private BankApiViewModel bankApiViewModel;
    private BAccountsViewModel bAccountsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentConnectToBankBinding.inflate(inflater, container, false);

        bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);
        bAccountsViewModel = new ViewModelProvider(requireActivity()).get(BAccountsViewModel.class);

        initOnClicks();
        handleArgsPassed();
        buildRv();
        getInstitutionsList();

        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
        binding.deleteRequisitionsFab.setOnClickListener(v -> deleteRequisitions());
    }

    private void deleteRequisitions() {
        bankApiViewModel.getAllRequisitions().observe(getViewLifecycleOwner(), requisitionsResult -> {
            if (requisitionsResult != null && requisitionsResult.getResults() != null && requisitionsResult.getResults().length > 0) {
                for (int i = 0; i < requisitionsResult.getResults().length; i++) {
                    bankApiViewModel.deleteRequisition(requisitionsResult.getResults()[i].getId());
                }
            }
        });
    }

    private void handleArgsPassed() {
        SelectBankFragmentArgs args = SelectBankFragmentArgs.fromBundle(requireArguments());
        String walletId = args.getWalletId();
        bAccount.setWalletIds(Collections.singletonList(walletId));
        bAccount.setOwner_id(DatabaseUtils.getCurrentUser().getUid());
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
                if (institutions == null) {
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
                if (endUserAgreement == null || endUserAgreement.getId() == null) {
                    Log.d(TAG, "onClick: EUA Null");
                } else {
                    PrefsUtils.setString(requireContext(),
                            "EUA" + institution.getId(),
                            endUserAgreement.getId());
                    bAccount.setEUA_id(endUserAgreement.getId());
                    bAccount.setEUA_EndDate(getEuaEndDate(endUserAgreement));
                    getRequisition(institution, endUserAgreement.getId(), true);
                }
            });

        } else {
            bAccount.setEUA_id(EUA_ID);
            getRequisition(institution, EUA_ID, true);
        }
    }

    @NonNull
    private Date getEuaEndDate(EndUserAgreement endUserAgreement) {
        return new Date(new Date().getTime() + Duration.ofDays(endUserAgreement.getAccess_valid_for_days()).toMillis());
    }

    private void getRequisition(Institution institution, String EUA_ID, Boolean account_selection) {
        String REQUISITION = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("REQUISITION" + institution.getId(), "");
        if (REQUISITION.isEmpty()) {
            bankApiViewModel.createRequisition(institution.getId(), EUA_ID, account_selection)
                    .observe(getViewLifecycleOwner(), requisition -> {
                        if (requisition == null || requisition.getId() == null) {
                            Log.e(TAG, "onResponse: requisition null ");
                            if (bankApiViewModel.getRequisitionError() != null && !bankApiViewModel.getRequisitionError().isEmpty()) {
                                Log.e(TAG, "getRequisition: " + bankApiViewModel.getRequisitionError());
                                if (bankApiViewModel.getRequisitionError().contains("Account selection not supported")) {
                                    getRequisition(institution, EUA_ID, false);
                                }
                            }
                        } else {
                            PrefsUtils.setString(requireContext(), "REQUISITION" + institution.getId(),
                                    requisition.getId());
                            bAccount.setRequisition_id(requisition.getId());
                            getAccounts(requisition);
                        }
                    });

        } else {
            //Get requisition details
            bAccount.setRequisition_id(REQUISITION);

            getRequisitionDetails(REQUISITION);
        }
    }

    private void getRequisitionDetails(String requisitionID) {
        bankApiViewModel.getRequisitionDetails(requisitionID)
                .observe(getViewLifecycleOwner(), requisition -> {
                    if (requisition == null || requisition.getId() == null) {
                        Log.e(TAG, "getRequisitionDetails: requisition details null");
                    } else {
                        getAccounts(requisition);
                    }
                });
    }

    private void getAccounts(Requisition requisition) {
        if (requisition.getAccounts() != null && requisition.getAccounts().length > 0) {
            Log.d(TAG, "getAccounts: length > 0");
            PrefsUtils.setString(requireContext(), "ACC_ID" + requisition.getId(), requisition.getAccounts()[0]);
            List<String> accounts = new ArrayList<>(Arrays.asList(requisition.getAccounts()));
            bAccount.setAccounts(accounts);
            getAccountsDetails(Arrays.asList(requisition.getAccounts()));
        } else {
            if (requisition.getLink() == null) {
                Log.e(TAG, "getAccounts: requisition link null");
            } else {
                PrefsUtils.saveBAccountToPrefs(requireContext(), bAccount);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requisition.getLink())));
            }
            Log.e(TAG, "getAccounts: no accounts");
            Toast.makeText(requireContext(), "No accounts", Toast.LENGTH_SHORT).show();
        }
    }


    private void getAccountsDetails(List<String> accounts) {
        UploadingDialog dialog = new UploadingDialog("Retrieving Data...");
        dialog.show(getParentFragmentManager(), "wait");

        //show dialog with accounts
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        for (String acc : accounts) {
            bankApiViewModel.getAccountDetails(acc)
                    .observe(getViewLifecycleOwner(), accountDetails -> {
                        if (accountDetails == null || accountDetails.getAccount() == null) {
                            Log.e(TAG, "getAccountsDetails: empty account or null");
                        } else {
                            accountDetailsList.add(accountDetails);
                            Log.d(TAG, "getAccountsDetails: ");
                        }
                        if (accountDetailsList.size() == accounts.size()) {
                            dialog.dismiss();
                            BankAccsListDialog bankAccsListDialog = new BankAccsListDialog(accountDetailsList);
                            bankAccsListDialog.show(getParentFragmentManager(), "BankAccountListDialog");
                        }
                    });
        }
    }
}