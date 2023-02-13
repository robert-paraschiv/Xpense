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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BAccountDetailsFragment extends Fragment implements TransactionsAdapter.OnTransactionClickListener {
    private static final String TAG = "BAccountDetailsFragment";

    private BankApiViewModel bankApiViewModel;
    private FragmentBAccountDetailsBinding binding;
    private BAccount bAccount;

    private TransactionsAdapter adapter;
    private final List<Transaction> transactionList = new ArrayList<>();
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (binding == null) {

            // Inflate the layout for this fragment
            binding = FragmentBAccountDetailsBinding.inflate(inflater, container, false);

            bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);

            initOnClicks();

            binding.detailsShimer.startShimmer();
            binding.transShimmerLayout.startShimmer();

            buildRecyclerView();
            getArgsPassed();
        }

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
            if (s == null || s.contains("Token is invalid or expired")) {
                getToken(bAccount);
            } else {
                getAccountBalances(bAccount);
                getAccountTransactions(bAccount);
            }
        });
    }

    private void getAccountTransactions(BAccount bAccount) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        String date_from = simpleDateFormat.format(calendar.getTime());
        bankApiViewModel.getAccountTransactions(bAccount.getAccounts().get(0), date_from)
                .observe(getViewLifecycleOwner(), transactionsResponse -> {
                    if (transactionsResponse == null || transactionsResponse.getTransactions() == null) {
                        Log.e(TAG, "onResponse: null trans response");
                    } else {
                        List<BankTransaction> bankTransactionList = new ArrayList<>(Arrays.asList(transactionsResponse.getTransactions().getBooked()));

                        sortBankTransactionListByDate(bankTransactionList, simpleDateFormat);

                        int pendingTransCounter = 0;
                        for (BankTransaction bankTransaction : transactionsResponse.getTransactions().getPending()) {
                            bankTransaction.setInternalTransactionId("" + pendingTransCounter++);
                            bankTransactionList.add(0, bankTransaction);
                        }

                        for (int i = 0; i < bankTransactionList.size(); i++) {
                            BankTransaction bankTransaction = bankTransactionList.get(i);

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

                            if (transactionList.contains(transaction)) {
                                transactionList.set(transactionList.indexOf(transaction), transaction);
                                adapter.notifyItemChanged(transactionList.indexOf(transaction));
                            } else {
                                transactionList.add(transaction);
                                adapter.notifyItemInserted(transactionList.indexOf(transaction));
                            }
                        }


                        if (binding.transactionNested.getVisibility() == View.INVISIBLE) {

                            AlphaAnimation shimmerLayoutFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
                            shimmerLayoutFadeAnimation.setDuration(500);
                            shimmerLayoutFadeAnimation.setRepeatCount(0);

                            shimmerLayoutFadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    binding.transShimmerLayout.hideShimmer();
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    binding.transShimmerLayout.setVisibility(View.INVISIBLE);
                                    binding.transactionNested.setVisibility(View.VISIBLE);
                                    binding.transactionNested.startAnimation(
                                            AnimationUtils
                                                    .loadAnimation(requireContext(), R.anim.item_animation_fade_in));
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });

                            binding.transShimmerLayout.startAnimation(shimmerLayoutFadeAnimation);
                        }
                    }
                });
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


                        AlphaAnimation shimmerLayoutFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
                        shimmerLayoutFadeAnimation.setDuration(500);
                        shimmerLayoutFadeAnimation.setRepeatCount(0);

                        shimmerLayoutFadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                binding.detailsShimer.hideShimmer();
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                binding.accDetails.setVisibility(View.VISIBLE);
                                binding.detailsShimer.setVisibility(View.INVISIBLE);
                                binding.accDetails.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.item_animation_fade_in));
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        binding.detailsShimer.startAnimation(shimmerLayoutFadeAnimation);

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

        //Check if id is one assigned manually
        try {
            Integer.parseInt(transaction.getId());
            transaction.setId(null);
        } catch (NumberFormatException ignored) {
        }

        transaction.setAmount(Math.abs(transaction.getAmount()));

        Navigation.findNavController(binding.getRoot())
                .navigate(BAccountDetailsFragmentDirections
                        .actionBAccountDetailsFragmentToAddTransactionFragment(bAccount.getWalletIds().get(0),
                                bAccount.getLinked_acc_currency(),
                                transaction));
    }
}