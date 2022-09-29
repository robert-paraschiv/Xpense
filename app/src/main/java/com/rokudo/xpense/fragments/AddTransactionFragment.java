package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.models.Transaction.EXPENSE_TYPE;
import static com.rokudo.xpense.models.Transaction.INCOME_TYPE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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

import java.util.Calendar;
import java.util.Objects;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private String walletId;
    private String currency;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        binding.transactionCategoryDropDown.setText("Groceries", false);
        binding.transactionTitle.requestFocus();
        binding.transactionTitle.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }, 250);
        binding.transactionCategoryDropDown.setOnFocusChangeListener((view, b) -> {
            hideKeyboard(view);
            binding.transactionCategoryDropDown.showDropDown();
        });

        getWalletId();
        initOnClicks();

        return binding.getRoot();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void getWalletId() {
        AddTransactionFragmentArgs args = AddTransactionFragmentArgs.fromBundle(requireArguments());
        walletId = args.getWalletId();
        currency = args.getCurrency();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> {
            hideKeyboard(view);
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });
        binding.saveTransactionBtn.setOnClickListener(v -> addTransactionToDb());
    }

    private void addTransactionToDb() {
        DocumentReference documentReference = DatabaseUtils.transactionsRef.document();
        Transaction transaction = new Transaction();
        transaction.setId(documentReference.getId());
        transaction.setWalletId(walletId);
        transaction.setAmount(Double.valueOf(Objects.requireNonNull(binding.transactionAmount.getText()).toString()));
        transaction.setCurrency(currency);
        Calendar calendar = Calendar.getInstance();
        calendar.set(binding.simpleDatePicker.getYear(),
                binding.simpleDatePicker.getMonth(),
                binding.simpleDatePicker.getDayOfMonth());
        transaction.setDate(calendar.getTime());
        transaction.setPicUrl(DatabaseUtils.getCurrentUser().getPictureUrl());
        transaction.setUser_id(DatabaseUtils.getCurrentUser().getUid());
        transaction.setUserName(DatabaseUtils.getCurrentUser().getName());
        transaction.setCategory(binding.transactionCategoryDropDown.getText().toString());
        transaction.setTitle(Objects.requireNonNull(binding.transactionTitle.getText()).toString());
        transaction.setType(binding.incomeChip.isChecked() ? INCOME_TYPE : EXPENSE_TYPE);
        TransactionViewModel viewModel =
                new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        viewModel.addTransaction(transaction).observe(getViewLifecycleOwner(), result -> {
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