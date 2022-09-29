package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.BarChartUtils.setupBarChart;
import static com.rokudo.xpense.utils.BarChartUtils.updateBarchartData;
import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;
import static com.rokudo.xpense.utils.PieChartUtils.updatePieChartData;
import static com.rokudo.xpense.utils.UserUtils.checkIfUserPicIsDifferent;
import static com.rokudo.xpense.utils.dialogs.DialogUtils.getCircularProgressDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.R;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentHomeBinding;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.TransactionUtils;
import com.rokudo.xpense.utils.dialogs.AdjustBalanceDialog;
import com.rokudo.xpense.utils.dialogs.WalletListDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    private ListenerRegistration userDetailsListenerRegistration;
    private final List<Transaction> transactionList = new ArrayList<>();
    private Wallet wallet;
    private WalletsViewModel walletsViewModel;
    private Boolean gotTransactionsOnce = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
            initOnClicks();

            setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());
            setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor());
        }

        loadWalletDetails();

        return binding.getRoot();
    }

    private void loadWalletDetails() {
        String selectedWalletId = requireContext()
                .getSharedPreferences("PREFS_NAME", Context.MODE_PRIVATE)
                .getString("selectedWalletId", "");

        walletsViewModel.loadWallet(selectedWalletId).observe(getViewLifecycleOwner(), wallet -> {
            if (wallet == null) {
                Log.e(TAG, "loadWalletsDetails: wallets null");
                binding.walletLayout.setVisibility(View.GONE);
                binding.addWalletLayout.setVisibility(View.VISIBLE);
            } else {
                PrefsUtils.setSelectedWalletId(requireContext(), wallet.getId());
                binding.walletLayout.setVisibility(View.VISIBLE);
                binding.addWalletLayout.setVisibility(View.GONE);
                updateWalletUI(wallet);
                loadTransactions(wallet.getId());
                this.wallet = wallet;
            }
        });
    }

    private void loadTransactions(String id) {
        TransactionViewModel transactionViewModel = new ViewModelProvider(requireActivity())
                .get(TransactionViewModel.class);

        transactionViewModel.loadTransactions(id)
                .observe(getViewLifecycleOwner(), values -> {
                    for (Transaction transaction : values) {
                        if (transactionList.contains(transaction)) {
                            transactionList.set(transactionList.indexOf(transaction), transaction);
                        } else {
                            if (gotTransactionsOnce) {
                                transactionList.add(0, transaction);
                            } else {
                                transactionList.add(transaction);
                            }
                        }
                    }
                    updatePieChartData(binding.pieChart, wallet, values);
                    updateBarchartData(binding.barChart, values, new TextView(requireContext()).getCurrentTextColor());
                    gotTransactionsOnce = true;
                });
        transactionViewModel.loadLatestTransaction().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                updateLatestTransactionUI(value);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateLatestTransactionUI(Transaction transaction) {
        if (transaction.getId() == null) {
            binding.lastTransactionLayout.setVisibility(View.GONE);
        } else {
            binding.lastTransactionLayout.setVisibility(View.VISIBLE);
            String transAmountPrefix;
            if (transaction.getType().equals("Income")) {
                binding.latestTransactionItem.transactionAmount
                        .setTextColor(getResources().getColor(android.R.color.holo_green_dark, requireActivity().getTheme()));
                transAmountPrefix = "+ ";
            } else {
                transAmountPrefix = "- ";
                binding.latestTransactionItem.transactionAmount
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark, requireActivity().getTheme()));
            }
            binding.latestTransactionItem.transactionAmount.setText(transAmountPrefix + transaction.getAmount().toString());
            binding.latestTransactionItem.transactionCategory.setText(transaction.getCategory());
            binding.latestTransactionItem.transactionDate.setText(TransactionUtils.getTransactionDateString(transaction));
            binding.latestTransactionItem.transactionPerson.setText(transaction.getUserName());
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(requireContext());
            Glide.with(requireContext())
                    .load(transaction.getPicUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(binding.latestTransactionItem.transactionImage);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateWalletUI(Wallet wallet) {
        binding.walletTitle.setText(wallet.getTitle());
        binding.walletAmount.setText(wallet.getAmount().toString());
        binding.walletCurrency.setText(wallet.getCurrency());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (userDetailsListenerRegistration != null) {
            userDetailsListenerRegistration.remove();
        }
        getUserDetails();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (userDetailsListenerRegistration != null) {
            userDetailsListenerRegistration.remove();
        }
    }

    private void getUserDetails() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null) {
                userDetailsListenerRegistration = usersRef.document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())
                        .addSnapshotListener(userDetailsEventListener);
            }
        }
    }

    private final EventListener<DocumentSnapshot> userDetailsEventListener = (value, error) -> {
        if (value != null) {
            User user = value.toObject(User.class);
            if (user != null) {
                updateUserDetailsUI(user);
                DatabaseUtils.setCurrentUser(user);
            }
            Log.d(TAG, "onCreateView: got data");
        }
    };

    private void updateUserDetailsUI(User user) {
        binding.welcomeTv.setText(String.format("Welcome, %s", user.getName()));

        if (binding.profileImage.getDrawable() == null || checkIfUserPicIsDifferent(user, DatabaseUtils.getCurrentUser())) {
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(requireContext());
            Glide.with(requireContext())
                    .load(user.getPictureUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(binding.profileImage);
        }
    }

    private void initOnClicks() {
        binding.profileImage.setOnClickListener(view -> navigateToSettings());
        binding.addWalletBtn.setOnClickListener(view -> handleAddWalletBtnClick());
        binding.walletDropDownBtn.setOnClickListener(view -> showWalletList());
        binding.walletTitle.setOnClickListener(view -> showWalletList());
        binding.addTransactionBtn.setOnClickListener(view -> navigateToAddTransaction());
        binding.seeAllTransactionsBtn.setOnClickListener(view -> Toast.makeText(requireContext(), "GET OUT RN", Toast.LENGTH_SHORT).show());
        binding.adjustBalanceBtn.setOnClickListener(view -> handleAdjustBalanceBtnClick());
        binding.barChart.setOnClickListener(view -> Toast.makeText(requireContext(), "bar chart", Toast.LENGTH_SHORT).show());
        binding.pieChart.setOnClickListener(view -> Toast.makeText(requireContext(), "pie pie", Toast.LENGTH_SHORT).show());
    }

    private void handleAddWalletBtnClick() {
        binding.addWalletBtn.setTransitionName("addWalletTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.addWalletBtn, "addWalletTransition")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToAddWalletFragment();

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void handleAdjustBalanceBtnClick() {
        AdjustBalanceDialog adjustBalanceDialog = new AdjustBalanceDialog("1400.00");
        adjustBalanceDialog.setOnDialogClicks(new AdjustBalanceDialog.OnAdjustBalanceDialogClickListener() {
            @Override
            public void onApplyClick(String amount) {
                Toast.makeText(requireContext(), "applied", Toast.LENGTH_SHORT).show();
                binding.walletAmount.setText(amount);
                adjustBalanceDialog.dismiss();
            }

            @Override
            public void onCancelClick() {
                adjustBalanceDialog.dismiss();
            }
        });
        adjustBalanceDialog.show(getParentFragmentManager(), "adjustBalanceDialog");
    }

    private void navigateToSettings() {
        binding.profileImage.setTransitionName("settingsTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.profileImage, "settingsTransition")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToSettingsFragment();

        MaterialFadeThrough hold = new MaterialFadeThrough();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToAddTransaction() {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.addTransactionBtn, getString(R.string.transition_name_add_transaction))
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAddTransactionLayout(wallet.getId(), wallet.getCurrency());

        MaterialElevationScale exit = new MaterialElevationScale(false);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialElevationScale reenter = new MaterialElevationScale(true);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void showWalletList() {
        WalletListDialog walletListDialog = new WalletListDialog(new ArrayList<>());
        walletListDialog.show(getParentFragmentManager(), "walletListDialog");
    }

}