package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.NordigenUtils.TOKEN_PREFS_NAME;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.retrofit.models.BankTransaction;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.databinding.FragmentBAccountDetailsBinding;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.NordigenUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BAccountDetailsFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {
    private static final String TAG = "BAccountDetailsFragment";

    BankApiViewModel bankApiViewModel;
    FragmentBAccountDetailsBinding binding;

    private TransactionsAdapter adapter;
    private final List<BankTransaction> bankTransactionList = new ArrayList<>();
    private final List<Transaction> transactionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBAccountDetailsBinding.inflate(inflater, container, false);

        bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);

        buildRecyclerView();
        getArgsPassed();

        return binding.getRoot();
    }

    private void getArgsPassed() {
        BAccountDetailsFragmentArgs args = BAccountDetailsFragmentArgs.fromBundle(requireArguments());
        updateBankAccDetailsUI(args.getBAccount());
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList, false, this);
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.transactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    private void updateBankAccDetailsUI(BAccount bAccount) {
        binding.accBankName.setText(bAccount.getBankName());

        Glide.with(binding.accBankImage)
                .load(bAccount.getBankPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_local_atm_24)
                .error(R.drawable.ic_baseline_local_atm_24)
                .transition(withCrossFade())
                .into(binding.accBankImage);

        getBankAccountDetails(bAccount);
    }

    private void getBankAccountDetails(BAccount bAccount) {
        String token = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString(TOKEN_PREFS_NAME, "");
        NordigenUtils.TOKEN_VAL = token;
        if (token.isEmpty()) {
            getToken(bAccount);
        } else {
            bankApiViewModel.getAccountDetails(bAccount.getAccounts().get(0))
                    .observe(getViewLifecycleOwner(), accountDetails -> {
                        if (accountDetails == null || accountDetails.getAccount() == null) {
                            Log.e(TAG, "getBankAccountDetails: null acc details");
                        } else {
                            binding.accIBAN.setText(accountDetails.getAccount().getIban());
                            binding.accCurrency.setText(accountDetails.getAccount().getCurrency());
                        }
                    });

            bankApiViewModel.getAccountBalances(bAccount.getAccounts().get(0))
                    .observe(getViewLifecycleOwner(), balances -> {
                        if (balances == null) {
                            Log.e(TAG, "onChanged: null balances");
                        } else {
                            Log.d(TAG, "onChanged: " + balances);
                            binding.accAmount.setText(balances.getBalances()[0].getBalanceAmount().get("amount"));
                        }
                    });

            String date_from = "2022-11-01";
            bankApiViewModel.getAccountTransactions(bAccount.getAccounts().get(0), date_from)
                    .observe(getViewLifecycleOwner(), transactionsResponse -> {
                        if (transactionsResponse == null || transactionsResponse.getTransactions() == null) {
                            Log.e(TAG, "onResponse: null trans response");
                        } else {

                            binding.progressIndicator.setVisibility(View.GONE);

                            bankTransactionList.addAll(Arrays.asList(transactionsResponse.getTransactions().getBooked()));
                            bankTransactionList.addAll(Arrays.asList(transactionsResponse.getTransactions().getPending()));
                            for (int i = 0; i < bankTransactionList.size(); i++) {
                                BankTransaction bankTransaction = bankTransactionList.get(i);

                                Transaction transaction = new Transaction();
                                transaction.setId(bankTransaction.getTransactionId());
                                transaction.setAmount(bankTransaction.getTransactionAmount().getAmount().doubleValue());
                                transaction.setCurrency(bankTransaction.getTransactionAmount().getCurrency());
                                transaction.setTitle(bankTransaction.getRemittanceInformationUnstructured());

                                transactionList.add(transaction);
                                adapter.notifyItemInserted(transactionList.indexOf(transaction));
                            }
                        }

                    });

        }
    }

    private void getToken(BAccount bAccount) {
        bankApiViewModel.getToken().observe(getViewLifecycleOwner(), token -> {
            if (token == null) {
                Log.d(TAG, "getToken: null");
            } else {
                NordigenUtils.TOKEN_VAL = token.getAccess();
                PrefsUtils.setToken(requireContext(), NordigenUtils.TOKEN_VAL);
                getBankAccountDetails(bAccount);
            }
        });
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        materialContainerTransform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(18));
        materialContainerTransform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(48));
        setSharedElementEnterTransition(materialContainerTransform);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(Transaction transaction) {

    }
}