package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.models.Transaction.EXPENSE_TYPE;
import static com.rokudo.xpense.models.Transaction.INCOME_TYPE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.firebase.firestore.DocumentReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAddTransactionBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PieChartUtils;

import java.util.Calendar;
import java.util.Objects;

@SuppressLint("SetTextI18n")
public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private String walletId;
    private String currency;
    private ExpenseCategory selectedCategory = CategoriesUtil.categoryList.get(0);

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        handleArgs();

        getWalletId();
        initOnClicks();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        final ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                viewGroup.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void buildCategoriesRv(int indexToCheck) {
        for (int i = 0; i < CategoriesUtil.categoryList.size(); i++) {
            ExpenseCategory category = CategoriesUtil.categoryList.get(i);
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_category, binding.categoryChipGroup, false);
            chip.setText(category.getName());
            chip.setChipIconTint(ColorStateList.valueOf(PieChartUtils.PIE_COLORS.get(i)));
            chip.setChipIcon(getResources().getDrawable(category.getResourceId(), requireContext().getTheme()));
            chip.setChecked(indexToCheck == i);

            binding.categoryChipGroup.addView(chip);
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleArgs() {
        AddTransactionFragmentArgs args = AddTransactionFragmentArgs.fromBundle(requireArguments());
        if (args.getTransaction() == null) {
            binding.getRoot().setTransitionName(requireContext().getResources().getString(R.string.transition_name_add_transaction));
            binding.selectedTextDummy.setText("Expense Category");
            selectedCategory = CategoriesUtil.categoryList.get(0);
            buildCategoriesRv(0);
        } else {
            Transaction transaction = args.getTransaction();
            binding.getRoot().setTransitionName("adjustBalance");
            binding.transactionAmount.setText(transaction.getAmount().toString());

            if (transaction.getType().equals("Income")) {
                binding.expenseChip.setChecked(false);
                binding.incomeChip.setChecked(true);
                binding.selectedTextDummy.setText("Income Category");
                selectedCategory = CategoriesUtil.categoryList.get(CategoriesUtil.categoryList.size() - 1);
                buildCategoriesRv(CategoriesUtil.categoryList.size() - 1);
            } else {
                binding.expenseChip.setChecked(true);
                binding.incomeChip.setChecked(false);
                binding.selectedTextDummy.setText("Expense Category");
                selectedCategory = CategoriesUtil.categoryList.get(0);
                buildCategoriesRv(0);
            }
        }
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

    @SuppressLint("ResourceType")
    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> {
            hideKeyboard(view);
            Navigation.findNavController(binding.getRoot()).popBackStack();
        });
        binding.saveTransactionBtn.setOnClickListener(v -> addTransactionToDb());
        binding.categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip != null)
                selectedCategory = CategoriesUtil.categoryList.get(
                        CategoriesUtil.categoryList.indexOf(
                                new ExpenseCategory(chip.getText().toString(), null)
                        )
                );
        });
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
        transaction.setCategory(selectedCategory.getName());
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