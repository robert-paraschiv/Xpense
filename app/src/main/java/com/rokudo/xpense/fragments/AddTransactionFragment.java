package com.rokudo.xpense.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.firestore.DocumentReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAddTransactionBinding;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;

import java.util.Date;
import java.util.Objects;

public class AddTransactionFragment extends Fragment {
    private static final String TAG = "AddTransactionFragment";

    FragmentAddTransactionBinding binding;
    private String walletId;
    private String currency;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        binding.transactionCategoryDropDown.setText("Groceries");
        getWalletId();
        initOnClicks();

        return binding.getRoot();
    }

    private void getWalletId() {
        AddTransactionFragmentArgs args = AddTransactionFragmentArgs.fromBundle(requireArguments());
        walletId = args.getWalletId();
        currency = args.getCurrency();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
        binding.saveTransactionBtn.setOnClickListener(v -> addTransactionToDb());
    }

    private void addTransactionToDb() {
        DocumentReference documentReference = DatabaseUtils.transactionsRef.document();
        Transaction transaction = new Transaction();
        transaction.setId(documentReference.getId());
        transaction.setWalletId(walletId);
        transaction.setAmount(Double.valueOf(Objects.requireNonNull(binding.transactionAmount.getText()).toString()));
        transaction.setCurrency(currency);
        transaction.setDate(new Date());
        transaction.setPicUrl(DatabaseUtils.getCurrentUser().getPictureUrl());
        transaction.setUser_id(DatabaseUtils.getCurrentUser().getUid());
        transaction.setCategory(binding.transactionCategoryDropDown.getText().toString());
        transaction.setTitle(Objects.requireNonNull(binding.transactionTitle.getText()).toString());
        transaction.setType(binding.chipLayout.getCheckedChipId() == 0 ? "Income" : "Expense");
        TransactionViewModel viewModel =
                new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        viewModel.addTransaction(walletId, transaction).observe(getViewLifecycleOwner(), result -> {
            if (result.equals("Success")) {
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        materialContainerTransform.setElevationShadowEnabled(true);
        materialContainerTransform.setAllContainerColors(Color.TRANSPARENT);
        materialContainerTransform.setScrimColor(Color.TRANSPARENT);
        materialContainerTransform.setDrawingViewId(R.id.nav_host_fragment);
        setSharedElementEnterTransition(materialContainerTransform);
        super.onCreate(savedInstanceState);
    }
}