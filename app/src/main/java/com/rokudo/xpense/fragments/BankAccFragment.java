package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.TransactionUtils.isBankTransactionDifferent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialSharedAxis;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.OnTransClickListener;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.BankTransaction;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.databinding.FragmentBankAccBinding;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.ShimmerUtils;
import com.rokudo.xpense.utils.dialogs.AgreementExpiredDialog;
import com.rokudo.xpense.utils.dialogs.BankAccsListDialog;
import com.rokudo.xpense.utils.dialogs.DialogUtils;
import com.rokudo.xpense.utils.dialogs.TimedDialog;
import com.rokudo.xpense.utils.dialogs.UploadingDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BankAccFragment extends Fragment implements OnTransClickListener {
    private static final String TAG = "BAccountDetailsFragment";

    private BankApiViewModel bankApiViewModel;
    private StatisticsViewModel statisticsViewModel;
    private FragmentBankAccBinding binding;
    private BAccount bAccount;

    private final Map<String, String> transByAmountMap = new HashMap<>();

    private TransactionsAdapter adapter;
    private final List<Transaction> transactionList = new ArrayList<>();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) {

            // Inflate the layout for this fragment
            binding = FragmentBankAccBinding.inflate(inflater, container, false);

            bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);
            statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

            populateTransByAmountMap();

            initOnClicks();

            binding.detailsShimer.startShimmer();
            binding.transShimmerLayout.startShimmer();

            buildRecyclerView();
            getArgsPassed();
        }

        return binding.getRoot();
    }

    private void populateTransByAmountMap() {
        if (statisticsViewModel.getHomeStoredStatisticsDoc() == null) {
            return;
        }
        statisticsViewModel.getHomeStoredStatisticsDoc()
                .getTransactions()
                .values()
                .forEach(transaction ->
                        transByAmountMap.put(transaction.getAmount().toString(), transaction.getId())
                );
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
    }

    private void getArgsPassed() {
        BankAccFragmentArgs args = BankAccFragmentArgs.fromBundle(requireArguments());
        bAccount = args.getBAccount();
        if (args.getBottomNavAction()) {
            MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            setEnterTransition(enter);
            setReturnTransition(exit);
        } else {

            MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
            materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            materialContainerTransform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(18));
            materialContainerTransform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(48));
            setSharedElementEnterTransition(materialContainerTransform);
        }
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
        binding.accIBAN.setText(bAccount.getLinked_acc_iban());
        binding.accCurrency.setText(bAccount.getLinked_acc_currency());

        Glide.with(binding.accBankImage)
                .load(bAccount.getBankPic())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                .fallback(R.drawable.ic_baseline_local_atm_24)
                .error(R.drawable.ic_baseline_local_atm_24)
                .transition(withCrossFade())
                .into(binding.accBankImage);

        if ((bAccount.getEUA_EndDate() != null
                && bAccount.getEUA_EndDate().before(new Date()))
                || bankApiViewModel.isEUAExpired()) {

            showExpiredEUADialog(bAccount);

        } else {
            getAccountBalances(bAccount);
            getAccountTransactions(bAccount);
        }

//        getBankAccountDetails(bAccount);
    }


    private void showExpiredEUADialog(BAccount bAccount) {
        binding.detailsShimer.hideShimmer();
        binding.transShimmerLayout.hideShimmer();
        AgreementExpiredDialog dialog =
                new AgreementExpiredDialog(getCreationDate(bAccount), bAccount.getEUA_EndDate());
        dialog.show(getParentFragmentManager(), "agreement_expired_dialog");
        dialog.setOnBtnClickListener(new AgreementExpiredDialog.onBtnClickListener() {
            @Override
            public void onYesClick() {
                bankApiViewModel.deleteRequisition(bAccount.getRequisition_id());
                bankApiViewModel.deleteEUA(bAccount.getEUA_id());
                createEUA();
            }

            @Override
            public void onNoClick() {
                dialog.dismiss();
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
    }

    private void createEUA() {
        bankApiViewModel.createEUA(bAccount.getInstitutionId()).observe(getViewLifecycleOwner(), endUserAgreement -> {
            if (endUserAgreement == null || endUserAgreement.getId() == null) {
                Log.d(TAG, "onClick: EUA Null");
            } else {
                PrefsUtils.setString(requireContext(),
                        "EUA" + bAccount.getInstitutionId(),
                        endUserAgreement.getId());
                bAccount.setEUA_id(endUserAgreement.getId());
                bAccount.setEUA_EndDate(getEuaEndDate(endUserAgreement));
                createRequisition(bAccount.getInstitutionId(), endUserAgreement.getId(), true);
            }
        });
    }

    private void createRequisition(String institutionId, String id, boolean accountSelection) {
        bankApiViewModel.createRequisition(institutionId, id, accountSelection)
                .observe(getViewLifecycleOwner(), requisition -> {
                    if (requisition == null || requisition.getId() == null) {
                        Log.e(TAG, "onResponse: requisition null ");
                        if (bankApiViewModel.getRequisitionError() != null && !bankApiViewModel.getRequisitionError().isEmpty()) {
                            Log.e(TAG, "getRequisition: " + bankApiViewModel.getRequisitionError());
                            if (bankApiViewModel.getRequisitionError().contains("Account selection not supported")) {
                                createRequisition(institutionId, id, false);
                            }
                        }
                    } else {
                        PrefsUtils.setString(requireContext(), "REQUISITION" + institutionId,
                                requisition.getId());
                        bAccount.setRequisition_id(requisition.getId());
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
                            bankAccsListDialog.setClickListener(position -> {
                                Log.d(TAG, "getAccountsDetails: " + accounts.get(position));
                                bAccount.setAccounts(new ArrayList<>(
                                        Collections.singletonList(accountDetailsList.get(position).getAccount_id())));
                                bAccount.setLinked_acc_id(accountDetailsList.get(position).getAccount_id());
                                bAccount.setLinked_acc_currency(accountDetailsList.get(position).getAccount().getCurrency());
                                bAccount.setLinked_acc_iban(accountDetailsList.get(position).getAccount().getIban());
                                DatabaseUtils.walletsRef.document(bAccount.getWalletIds().get(0))
                                        .update("bAccount", bAccount)
                                        .addOnSuccessListener(unused -> {
                                            Log.d(TAG, "getAccountsDetails: updated wallet with bank account");
                                            bankAccsListDialog.dismiss();
                                            Navigation.findNavController(binding.getRoot())
                                                    .popBackStack();
                                        });
                            });
                        }
                    });
        }
    }


    private Date getCreationDate(BAccount bAccount) {
        if (bAccount.getEUA_EndDate() == null) {
            return null;
        }
        return new Date(bAccount.getEUA_EndDate().getTime() - Duration.ofDays(90).toMillis());
    }

    @NonNull
    private Date getEuaEndDate(EndUserAgreement endUserAgreement) {
        return new Date(new Date().getTime() + Duration.ofDays(endUserAgreement.getAccess_valid_for_days()).toMillis());
    }

    private void getAccountTransactions(BAccount bAccount) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        String date_from = simpleDateFormat.format(calendar.getTime());
        bankApiViewModel
                .getAccountTransactions(bAccount.getAccounts().get(0), date_from)
                .observe(getViewLifecycleOwner(), transactionsResponse -> {
                    if (transactionsResponse == null || transactionsResponse.getTransactions() == null) {
                        Log.e(TAG, "onResponse: null trans response");
                        TimedDialog timedDialog = new TimedDialog("Something went wrong, please try again...", 1500);
                        timedDialog.show(getParentFragmentManager(), "TryAgainDialog");
                        new Handler().postDelayed(() -> {
                            timedDialog.dismiss();
                            Navigation.findNavController(binding.getRoot()).popBackStack();
                        }, 1500);
                    } else {
                        boolean transactionListWasInitiallyEmpty = transactionList.isEmpty();

                        List<BankTransaction> bankTransactionList =
                                new ArrayList<>(Arrays.asList(transactionsResponse.getTransactions().getBooked()));

                        sortBankTransactionListByDate(bankTransactionList, simpleDateFormat);

                        int pendingTransCounter = 0;
                        for (BankTransaction bankTransaction : transactionsResponse.getTransactions().getPending()) {
                            bankTransaction.setInternalTransactionId("" + pendingTransCounter++);
                            bankTransactionList.add(0, bankTransaction);
                        }

                        for (int i = 0; i < bankTransactionList.size(); i++) {
                            BankTransaction bankTransaction = bankTransactionList.get(i);

                            Transaction transaction = getTransactionFromBankTrans(bankTransaction);

                            addOrChangeTrans(transaction);
                        }

                        if (transactionListWasInitiallyEmpty) {
                            ShimmerUtils.transitionShimmerLayoutToFinalView(
                                    binding.transShimmerLayout,
                                    binding.transactionNested,
                                    requireContext()
                            );
                        }
                    }
                });
    }

    private void addOrChangeTrans(Transaction transaction) {
        if (transactionList.contains(transaction)) {
            try {
                if (!isBankTransactionDifferent(transaction, transactionList.get(transactionList.indexOf(transaction)))) {
                    return;
                }
                transactionList.set(transactionList.indexOf(transaction), transaction);
                adapter.notifyItemChanged(transactionList.indexOf(transaction));

            } catch (Exception e) {
                Log.e(TAG, "addOrChangeTrans: ", e);
            }
        } else {
            transactionList.add(transaction);
            adapter.notifyItemInserted(transactionList.indexOf(transaction));
        }
    }

    @NonNull
    private Transaction getTransactionFromBankTrans(BankTransaction bankTransaction) {
        Transaction transaction = new Transaction();
        if (bankTransaction.getBookingDate() != null &&
                !bankTransaction.getBookingDate().isEmpty()) {
            try {
                Date transDate = simpleDateFormat.parse(bankTransaction.getBookingDate());
                transaction.setDate(transDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        transaction.setId(bankTransaction.getInternalTransactionId());
        transaction.setAmount(bankTransaction.getTransactionAmount().getAmount().doubleValue());
        transaction.setCategory(bankTransaction.getProprietaryBankTransactionCode());
        transaction.setCurrency(bankTransaction.getTransactionAmount().getCurrency());
        transaction.setTitle(bankTransaction.getRemittanceInformationUnstructured());

        String transAmount = String.format(Locale.getDefault(), "%.2f", transaction.getAmount()).replace("-", "");
        if (transAmount.endsWith(".00")) {
            transAmount = transAmount.replace(".00", ".0");
        }
        transaction.setAlreadyAdded(transByAmountMap.containsKey(transAmount));

        return transaction;
    }

    private void sortBankTransactionListByDate(List<BankTransaction> bankTransactionList, SimpleDateFormat simpleDateFormat) {
        bankTransactionList.sort((o1, o2) -> {
            Date trans1_date = null;
            Date trans2_date = null;
            try {
                trans1_date = simpleDateFormat.parse(o1.getBookingDate());
                trans2_date = simpleDateFormat.parse(o2.getBookingDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (trans1_date == null) {
                trans1_date = new Date();
            }
            if (trans2_date == null) {
                trans2_date = new Date();
            }

            return trans2_date.compareTo(trans1_date);
        });
    }

    private void getAccountBalances(BAccount bAccount) {
        bankApiViewModel.getAccountBalances(bAccount.getAccounts().get(0))
                .observe(getViewLifecycleOwner(), balances -> {
                    if (balances == null) {
                        Log.e(TAG, "onChanged: null balances");
                    } else {
                        Log.d(TAG, "onChanged: " + balances);
                        binding.accAmount.setText(balances.getBalances()[0].getBalanceAmount().get("amount"));

                        ShimmerUtils.transitionShimmerLayoutToFinalView(binding.detailsShimer, binding.accDetails, requireContext());
                    }
                });
    }

    @Override
    public void onClick(Transaction transaction) {
        if (transaction.getAmount().toString().startsWith("-")) {
            transaction.setType(Transaction.EXPENSE_TYPE);
        } else {
            transaction.setType(Transaction.INCOME_TYPE);
        }

        //Check if id is one assigned manually
        try {
            Integer.parseInt(transaction.getId());
            transaction.setId(null);
        } catch (NumberFormatException ignored) {
        }

        transaction.setAmount(Math.abs(transaction.getAmount()));

        Navigation.findNavController(binding.getRoot())
                .navigate(BankAccFragmentDirections
                        .actionBAccountDetailsFragmentToAddTransactionFragment(
                                bAccount.getWalletIds().get(0),
                                bAccount.getLinked_acc_currency(),
                                transaction, false));
    }
}