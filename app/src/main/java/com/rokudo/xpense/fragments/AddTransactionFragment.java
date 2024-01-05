package com.rokudo.xpense.fragments;

import static com.rokudo.xpense.models.Transaction.EXPENSE_TYPE;
import static com.rokudo.xpense.models.Transaction.INCOME_TYPE;
import static com.rokudo.xpense.models.Transaction.TRANSFER_TYPE;
import static com.rokudo.xpense.utils.TransactionUtils.isTransactionDifferent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.firestore.DocumentReference;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAddTransactionBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.dialogs.CategoryDialog;
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
        if (binding == null) {

            binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

            postponeEnterTransition();

            viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

            getWalletId();
            initOnClicks();
            handleArgs();

            startPostponedEnterTransition();
        }

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void handleArgs() {
        AddTransactionFragmentArgs args = AddTransactionFragmentArgs.fromBundle(requireArguments());
        if (args.getTransaction() == null) {

            MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
            materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            materialContainerTransform.setElevationShadowEnabled(true);
            materialContainerTransform.setAllContainerColors(Color.TRANSPARENT);
            materialContainerTransform.setScrimColor(Color.TRANSPARENT);
            materialContainerTransform.setDrawingViewId(R.id.nav_host_fragment);
            setSharedElementEnterTransition(materialContainerTransform);

            binding.getRoot().setTransitionName(requireContext().getResources().getString(R.string.transition_name_add_transaction));
            binding.addTransToolbarTitle.setText(requireContext().getString(R.string.add_transaction_fragment_toolbar_new));
            selectedCategory = CategoriesUtil.expenseCategoryList.get(0);
            binding.simpleDatePicker.setMaxDate(new Date().getTime());

        } else {
            if (args.getTransaction() == null) {
                return;
            }

            MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            setEnterTransition(enter);
            setReturnTransition(exit);

            binding.deleteTransBtn.setVisibility(View.VISIBLE);
            mTransaction = args.getTransaction();
            binding.getRoot().setTransitionName("adjustBalance");
            if (args.getEditMode()) {
                binding.addTransToolbarTitle.setText(requireContext().getString(R.string.add_transaction_fragment_toolbar_edit));
            } else {
                binding.addTransToolbarTitle.setText(requireContext().getString(R.string.add_transaction_fragment_toolbar_new));
            }
            binding.transactionAmount.setText(mTransaction.getAmount().toString());
            binding.transactionTitle.setText(mTransaction.getTitle());

            if (mTransaction.getDate() != null) {
                LocalDate localDate = mTransaction.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                binding.simpleDatePicker.updateDate(localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
            }

            binding.saveTransactionBtn.setText(requireContext().getString(R.string.add_transaction_update_btn));


            if (mTransaction.getType().equals(INCOME_TYPE)) {
                binding.expenseChip.setChecked(false);
                binding.incomeChip.setChecked(true);
                selectedCategory = new ExpenseCategory("Income");
                binding.selectedCategoryCard.setVisibility(View.GONE);
                binding.cashSwitch.setVisibility(View.GONE);
            } else {
                binding.expenseChip.setChecked(true);
                binding.incomeChip.setChecked(false);

                binding.selectedCategoryCard.setVisibility(View.VISIBLE);
                binding.cashSwitch.setVisibility(View.VISIBLE);

                if (mTransaction.getCategory() == null) {
                    return;
                }

                ExpenseCategory transactionExpenseCategory = new ExpenseCategory(mTransaction.getCategory());
                try {
                    selectedCategory = CategoriesUtil.expenseCategoryList.get(
                            CategoriesUtil.expenseCategoryList
                                    .indexOf(transactionExpenseCategory));

                    binding.selectedCategoryChip.setText(selectedCategory.getName());
                    binding.selectedCategoryChip.setChipIconTint(ColorStateList.valueOf(selectedCategory.getColor()));
                    binding.selectedCategoryChip.setChipIcon(ContextCompat.getDrawable(requireContext(), selectedCategory.getResourceId()));
                } catch (IndexOutOfBoundsException ignored) {
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
        binding.incomeChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = new ExpenseCategory("Income");
                binding.selectedCategoryCard.setVisibility(View.GONE);
                binding.cashSwitch.setVisibility(View.GONE);
            }
        });
        binding.expenseChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = null;
                binding.selectedCategoryCard.setVisibility(View.VISIBLE);
                binding.cashSwitch.setVisibility(View.VISIBLE);
            }
        });
        binding.transferChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = new ExpenseCategory("Transfer");
                binding.selectedCategoryCard.setVisibility(View.GONE);
                binding.cashSwitch.setVisibility(View.GONE);
            }
        });

        binding.selectedCategoryCard.setOnClickListener(view -> {
            CategoryDialog categoryDialog = new CategoryDialog(selectedCategory);
            categoryDialog.showNow(requireFragmentManager(), "transactionCategoryDialog");
            categoryDialog.setClickListener(expenseCategory -> {
                selectedCategory = expenseCategory;
                binding.selectedCategoryChip.setText(expenseCategory.getName());
                binding.selectedCategoryChip.setChipIconTint(ColorStateList.valueOf(expenseCategory.getColor()));
                binding.selectedCategoryChip.setChipIcon(ContextCompat.getDrawable(requireContext(), expenseCategory.getResourceId()));
                categoryDialog.dismiss();
            });
        });
    }

    private void addTransactionToDb() {
        if (selectedCategory == null) {
            binding.pleaseSelectTv.setVisibility(View.VISIBLE);
            binding.getRoot().postDelayed(() -> {
                binding.pleaseSelectTv.setVisibility(View.GONE);
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
        String transactionType = null;
        if (binding.incomeChip.isChecked()) {
            transactionType = INCOME_TYPE;
        } else if (binding.expenseChip.isChecked()) {
            transactionType = EXPENSE_TYPE;
        } else if (binding.transferChip.isChecked()) {
            transactionType = TRANSFER_TYPE;
        }
        transaction.setType(transactionType);

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
            if (isTransactionDifferent(mTransaction, transaction)) {

                if (mTransaction.getId() == null || mTransaction.getId().equals("NOTPROVIDED")) {
                    DocumentReference documentReference = DatabaseUtils.getTransactionsRef(walletId).document();
                    transaction.setId(documentReference.getId());
                } else {
                    transaction.setId(mTransaction.getId());
                }
                UploadingDialog uploadingDialog = new UploadingDialog("Please Wait...");
                uploadingDialog.show(getParentFragmentManager(), "wait");
                viewModel.updateTransaction(transaction)
                        .observe(getViewLifecycleOwner(), result -> {
                            if (result != null && result.equals("Success")) {
                                uploadingDialog.dismiss();
                                Navigation.findNavController(binding.getRoot())
                                        .popBackStack(R.id.homeFragment, false);
                            }
                        });
            } else {
                Navigation.findNavController(binding.getRoot())
                        .popBackStack(R.id.homeFragment, false);
            }
        }
    }
}