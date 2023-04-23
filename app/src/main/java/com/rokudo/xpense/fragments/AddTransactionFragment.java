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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.rokudo.xpense.utils.dialogs.ConfirmationDialog;
import com.rokudo.xpense.utils.dialogs.UploadingDialog;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@SuppressLint("SetTextI18n")
public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private String walletId;
    private String currency;
    private ExpenseCategory selectedCategory;
    private Transaction mTransaction;
    private TransactionViewModel viewModel;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        getWalletId();
        initOnClicks();
        buildCategoriesRv();
        handleArgs();

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

    private void buildCategoriesRv() {
        for (int i = 0; i < CategoriesUtil.expenseCategoryList.size(); i++) {
            ExpenseCategory category = CategoriesUtil.expenseCategoryList.get(i);
            if (category.getName().equals("Income")) {
                continue;
            }
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_category, binding.categoryChipGroup, false);
            chip.setText(category.getName());
            chip.setChipIconTint(ColorStateList.valueOf(CategoriesUtil.expenseCategoryList.get(i).getColor()));
            chip.setChipIcon(ContextCompat.getDrawable(requireContext(), category.getResourceId()));
            chip.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.cards_bg_color, requireActivity().getTheme())));
            chip.setElevation(0);

            binding.categoryChipGroup.addView(chip);
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleArgs() {
        AddTransactionFragmentArgs args = AddTransactionFragmentArgs.fromBundle(requireArguments());
        if (args.getTransaction() == null) {
            binding.getRoot().setTransitionName(requireContext().getResources().getString(R.string.transition_name_add_transaction));
            binding.selectedTextDummy.setText("Expense Category");
            selectedCategory = CategoriesUtil.expenseCategoryList.get(0);
            binding.simpleDatePicker.setMaxDate(new Date().getTime());
        } else {
            if (args.getTransaction() == null) {
                return;
            }
            binding.deleteTransBtn.setVisibility(View.VISIBLE);
            mTransaction = args.getTransaction();
            binding.getRoot().setTransitionName("adjustBalance");
            binding.transactionAmount.setText(mTransaction.getAmount().toString());
            binding.transactionTitle.setText(mTransaction.getTitle());

            if (mTransaction.getDate() != null) {
                LocalDate localDate = mTransaction.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//            binding.simpleDatePicker.setMaxDate(new Date().getTime());
                binding.simpleDatePicker.updateDate(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            }

            binding.saveTransactionBtn.setText("Save");


            if (mTransaction.getType().equals(INCOME_TYPE)) {
                binding.expenseChip.setChecked(false);
                binding.incomeChip.setChecked(true);
                selectedCategory = new ExpenseCategory("Income", null, null);
                binding.selectedTextDummy.setVisibility(View.GONE);
                binding.categoryChipGroup.setVisibility(View.GONE);
            } else {
                binding.expenseChip.setChecked(true);
                binding.incomeChip.setChecked(false);

                binding.selectedTextDummy.setVisibility(View.VISIBLE);
                binding.categoryChipGroup.setVisibility(View.VISIBLE);

                if (mTransaction.getCategory() == null) {
                    return;
                }

                ExpenseCategory transactionExpenseCategory = new ExpenseCategory(mTransaction.getCategory(), null, null);
                try {
                    selectedCategory = CategoriesUtil.expenseCategoryList.get(
                            CategoriesUtil.expenseCategoryList
                                    .indexOf(transactionExpenseCategory));
                } catch (ArrayIndexOutOfBoundsException ignored) {
                }


                if (selectedCategory != null) {
                    for (int i = 0; i < binding.categoryChipGroup.getChildCount(); i++) {
                        Chip chip = (Chip) binding.categoryChipGroup.getChildAt(i);
                        if (i == CategoriesUtil.expenseCategoryList.indexOf(
                                transactionExpenseCategory)) {
                            chip.setChecked(true);
                        }
                    }
                }

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
        binding.deleteTransBtn.setOnClickListener(v -> {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this transaction ?");
            confirmationDialog.show(getParentFragmentManager(), "ConfirmationDialog");
            confirmationDialog.setOnClickListener(() -> {
                confirmationDialog.dismiss();
                viewModel
                        .deleteTransaction(mTransaction.getId(), mTransaction.getWalletId())
                        .observe(getViewLifecycleOwner(), result -> {
                            if (result != null && result) {
                                Toast.makeText(requireContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(binding.getRoot()).popBackStack();
                            }
                        });
            });
        });
        binding.saveTransactionBtn.setOnClickListener(v -> addTransactionToDb());
        binding.categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip != null)
                selectedCategory = CategoriesUtil.expenseCategoryList.get(
                        CategoriesUtil.expenseCategoryList.indexOf(
                                new ExpenseCategory(chip.getText().toString(), null, null)
                        )
                );
        });
        binding.incomeChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = new ExpenseCategory("Income", null, null);
                binding.categoryChipGroup.setVisibility(View.GONE);
                binding.selectedTextDummy.setVisibility(View.GONE);
            }
        });
        binding.expenseChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = null;
                binding.selectedTextDummy.setVisibility(View.VISIBLE);
                binding.categoryChipGroup.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addTransactionToDb() {
        if (selectedCategory == null) {
            binding.pleaseSelectTv.setVisibility(View.VISIBLE);
            binding.selectedTextDummy.setVisibility(View.INVISIBLE);
            binding.getRoot().postDelayed(() -> {
                binding.pleaseSelectTv.setVisibility(View.GONE);
                binding.selectedTextDummy.setVisibility(View.VISIBLE);
            }, 1500);
            return;
        }
        if (binding.transactionAmount.getText() == null
                || binding.transactionAmount.getText().toString().isEmpty()) {
            binding.amountInputLayout.setError("Please input your amount");
            binding.amountInputLayout.postDelayed(() ->
                    binding.amountInputLayout.setError(null), 1500);
            return;
        }

        if (Double.parseDouble(binding.transactionAmount.getText().toString()) <= 0d) {
            binding.amountInputLayout.setError("Amount must be greater than zero");
            binding.amountInputLayout.postDelayed(() ->
                    binding.amountInputLayout.setError(null), 1500);
            return;
        }

        Transaction transaction = new Transaction();

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
        transaction.setCashTransaction(binding.cashSwitch.isChecked());
        transaction.setType(binding.incomeChip.isChecked() ? INCOME_TYPE : EXPENSE_TYPE);

        if (mTransaction == null) {
            DocumentReference documentReference = DatabaseUtils.getTransactionsRef(walletId).document();
            transaction.setId(documentReference.getId());

            UploadingDialog uploadingDialog = new UploadingDialog("Please Wait...");
            uploadingDialog.show(getParentFragmentManager(), "wait");
            viewModel.addTransaction(transaction).observe(getViewLifecycleOwner(), result -> {
                if (result != null && result.equals("Success")) {
                    uploadingDialog.dismiss();
                    Navigation.findNavController(binding.getRoot())
                            .popBackStack(R.id.homeFragment, false);
                }
            });
        } else {
            if (mTransaction.getId() == null || mTransaction.getId().equals("NOTPROVIDED")) {
                DocumentReference documentReference = DatabaseUtils.getTransactionsRef(walletId).document();
                transaction.setId(documentReference.getId());
            } else {
                transaction.setId(mTransaction.getId());
            }
            UploadingDialog uploadingDialog = new UploadingDialog("Please Wait...");
            uploadingDialog.show(getParentFragmentManager(), "wait");
            viewModel.updateTransaction(transaction).observe(getViewLifecycleOwner(), result -> {
                if (result.equals("Success")) {
                    uploadingDialog.dismiss();
                    Navigation.findNavController(binding.getRoot())
                            .popBackStack(R.id.homeFragment, false);
                }
            });
        }
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