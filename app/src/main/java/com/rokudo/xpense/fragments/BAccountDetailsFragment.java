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
import androidx.navigation.Navigation;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BAccountDetailsFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {
    private static final String TAG = "BAccountDetailsFragment";

    BankApiViewModel bankApiViewModel;
    FragmentBAccountDetailsBinding binding;
    BAccount bAccount;

    private TransactionsAdapter adapter;
    private final List<BankTransaction> bankTransactionList = new ArrayList<>();
    private final List<Transaction> transactionList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBAccountDetailsBinding.inflate(inflater, container, false);

        bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);

        initOnClicks();

        binding.detailsShimer.startShimmer();
        binding.transShimmerLayout.startShimmer();

        buildRecyclerView();
        getArgsPassed();

        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(v ->
                Navigation.findNavController(binding.getRoot()).popBackStack());
    }

    private void getArgsPassed() {
        BAccountDetailsFragmentArgs args = BAccountDetailsFragmentArgs.fromBundle(requireArguments());
        bAccount = args.getBAccount();
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
            checkToken(bAccount, token);
        }

    }

    private void checkToken(BAccount bAccount, String token) {
        bankApiViewModel.refreshToken(token).observe(getViewLifecycleOwner(), s -> {
            Log.d(TAG, "getBankAccountDetails: " + s);
            if (s.contains("Token is invalid or expired")) {
                getToken(bAccount);
            } else {
                getAccountBalances(bAccount);
                getAccountTransactions(bAccount);
            }
        });
    }

    private void getAccountTransactions(BAccount bAccount) {
        String date_from = "2022-11-10";
        bankApiViewModel.getAccountTransactions(bAccount.getAccounts().get(0), date_from)
                .observe(getViewLifecycleOwner(), transactionsResponse -> {
                    if (transactionsResponse == null || transactionsResponse.getTransactions() == null) {
                        Log.e(TAG, "onResponse: null trans response");
                    } else {

                        transactionList.clear();
                        bankTransactionList.clear();
                        adapter.notifyDataSetChanged();
                        bankTransactionList.addAll(Arrays.asList(transactionsResponse.getTransactions().getBooked()));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
                        bankTransactionList.sort((o1, o2) -> {
                            if (o1.getBookingDate() != null && o2.getBookingDate() != null) {
                                try {
                                    return Objects.requireNonNull(simpleDateFormat.parse(o1.getBookingDate()))
                                            .compareTo(simpleDateFormat.parse(o2.getBookingDate()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (o1.getBookingDate() == null && o2.getBookingDate() != null) {
                                return 1;
                            } else {
                                return -1;
                            }
                        });

                        for (BankTransaction bankTransaction : transactionsResponse.getTransactions().getPending()) {
                            bankTransactionList.add(0, bankTransaction);
                        }

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


                        binding.transactionNested.setVisibility(View.VISIBLE);
                        binding.transShimmerLayout.stopShimmer();
                        binding.transShimmerLayout.setVisibility(View.INVISIBLE);
                    }
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
                        binding.accDetails.setVisibility(View.VISIBLE);
                        binding.detailsShimer.stopShimmer();
                        binding.detailsShimer.setVisibility(View.INVISIBLE);
                    }
                });
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
        if (transaction.getAmount().toString().startsWith("-")) {
            transaction.setType(Transaction.EXPENSE_TYPE);
        } else {
            transaction.setType(Transaction.INCOME_TYPE);
        }
        transaction.setAmount(Math.abs(transaction.getAmount()));

        Navigation.findNavController(binding.getRoot())
                .navigate(BAccountDetailsFragmentDirections
                        .actionBAccountDetailsFragmentToAddTransactionFragment(bAccount.getWalletIds().get(0),
                                bAccount.getLinked_acc_currency(),
                                transaction));
    }
}