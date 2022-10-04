package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.models.WalletUser.getOtherUserProfilePic;
import static com.rokudo.xpense.utils.BarChartUtils.setupBarChart;
import static com.rokudo.xpense.utils.BarChartUtils.updateBarchartData;
import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;
import static com.rokudo.xpense.utils.PieChartUtils.updatePieChartData;
import static com.rokudo.xpense.utils.TransactionUtils.updateLatestTransactionUI;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
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
import com.rokudo.xpense.utils.dialogs.AdjustBalanceDialog;
import com.rokudo.xpense.utils.dialogs.DialogUtils;
import com.rokudo.xpense.utils.dialogs.WalletListDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    private ListenerRegistration userDetailsListenerRegistration;
    private final List<Transaction> transactionList = new ArrayList<>();
    private Wallet mWallet;
    private WalletsViewModel walletsViewModel;
    private TransactionViewModel transactionViewModel;

    private Boolean gotTransactionsOnce = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
            transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
            initOnClicks();

            setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor(), true);
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
                mWallet = wallet;
            }
        });
    }

    private void loadTransactions(String id) {
        transactionViewModel.loadTransactions(id)
                .observe(getViewLifecycleOwner(), values -> {
                    boolean needUpdate = false;
                    for (Transaction transaction : values) {
                        if (transactionList.contains(transaction)) {
                            if (isTransactionDifferent(transaction, transactionList.get(transactionList.indexOf(transaction)))) {
                                needUpdate = true;
                                transactionList.set(transactionList.indexOf(transaction), transaction);
                            }
                        } else {
                            if (gotTransactionsOnce) {
                                transactionList.add(0, transaction);
                            } else {
                                transactionList.add(transaction);
                            }
                            needUpdate = true;
                        }
                    }
                    if (needUpdate || values.isEmpty()) {
                        updatePieChartData(binding.pieChart, mWallet.getCurrency(), values, true);
                        updateBarchartData(binding.barChart, values, new TextView(requireContext()).getCurrentTextColor(), true);
                    }
                    gotTransactionsOnce = true;
                });
        transactionViewModel.loadLatestTransaction().observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                updateLatestTransactionUI(value, binding, requireContext());
            }
        });
    }

    private boolean isTransactionDifferent(Transaction newTransaction, Transaction oldTransaction) {
        if (!newTransaction.getAmount().equals(oldTransaction.getAmount()))
            return true;
        if (!newTransaction.getCategory().equals(oldTransaction.getCategory()))
            return true;
        return !newTransaction.getType().equals(oldTransaction.getType());
    }

    @SuppressLint("SetTextI18n")
    private void updateWalletUI(Wallet wallet) {
        binding.walletTitle.setText(wallet.getTitle());
        binding.walletAmount.setText(wallet.getAmount().toString());
        binding.walletCurrency.setText(wallet.getCurrency());
        if (wallet.getWalletUsers() == null || wallet.getWalletUsers().isEmpty() || wallet.getWalletUsers().size() < 2) {
            binding.sharedWithLayout.setVisibility(View.GONE);
        } else {
            binding.sharedWithLayout.setVisibility(View.VISIBLE);

            Glide.with(binding.sharedWithIcon)
                    .load(getOtherUserProfilePic(wallet.getWalletUsers()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(binding.sharedWithIcon);
        }

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
        binding.seeAllTransactionsBtn.setOnClickListener(view -> navigateToTransactionsListFragment());
        binding.adjustBalanceBtn.setOnClickListener(view -> handleAdjustBalanceBtnClick());
        binding.barChartCard.setOnClickListener(view -> navigateToBarDetails());
        binding.barDetailsBtn.setOnClickListener(view -> navigateToBarDetails());
        binding.pieChartCard.setOnClickListener(view -> navigateToPieDetails());
        binding.pieDetailsBtn.setOnClickListener(view -> navigateToPieDetails());
        binding.emptyTransactions.setOnClickListener(v -> deleteTransactions());
    }

    private void navigateToBarDetails() {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.barChartCard, "barChartCard")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToBarDetailsFragment(mWallet);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToPieDetails() {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.pieChartCard, "pieChartCard")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToPieDetailsFragment(mWallet);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void deleteTransactions() {
        DatabaseUtils.transactionsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                documentSnapshot.getReference().delete();
            }
        });
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

    private void handleEditWalletBtn(Wallet wallet) {
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);

        Navigation.findNavController(binding.getRoot()).navigate(HomeFragmentDirections
                .actionHomeFragmentToEditWalletFragment(wallet));
    }

    private void handleAdjustBalanceBtnClick() {
        AdjustBalanceDialog adjustBalanceDialog = new AdjustBalanceDialog(mWallet.getAmount().toString());
        adjustBalanceDialog.setOnDialogClicks(new AdjustBalanceDialog.OnAdjustBalanceDialogClickListener() {
            @Override
            public void onApplyClick(String amount) {
                adjustBalanceDialog.dismiss();
                if (Double.parseDouble(amount) != mWallet.getAmount())
                    navigateToAddTransaction(amount);
            }

            @Override
            public void onCancelClick() {
                adjustBalanceDialog.dismiss();
            }
        });
        adjustBalanceDialog.show(getParentFragmentManager(), "adjustBalanceDialog");
    }

    private void navigateToAddTransaction(String amount) {
        binding.adjustBalanceBtn.setTransitionName("adjustBalance");
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.adjustBalanceBtn, "adjustBalance")
                .build();

        Transaction transaction = new Transaction();
        if (Double.parseDouble(amount) > mWallet.getAmount()) {
            transaction.setType("Income");
            transaction.setAmount(Double.parseDouble(amount) - mWallet.getAmount());
        } else {
            transaction.setType("Expense");
            transaction.setAmount(mWallet.getAmount() - Double.parseDouble(amount));
        }

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAddTransactionLayout(mWallet.getId(), mWallet.getCurrency(), transaction);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToSettings() {
        binding.profileImage.setTransitionName("settingsTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.profileImage, "settingsTransition")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToSettingsFragment();

        MaterialFadeThrough materialFadeThrough = new MaterialFadeThrough();
        materialFadeThrough.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(materialFadeThrough);
        setReenterTransition(materialFadeThrough);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToAddTransaction() {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.addTransactionBtn, getString(R.string.transition_name_add_transaction))
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAddTransactionLayout(mWallet.getId(), mWallet.getCurrency(), null);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToTransactionsListFragment() {

        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);

        Navigation.findNavController(binding.getRoot()).navigate(HomeFragmentDirections
                .actionHomeFragmentToListTransactionsFragment(mWallet.getId(), mWallet.getCurrency()));
    }

    private void showWalletList() {
        walletsViewModel.loadWallets().observe(getViewLifecycleOwner(), new Observer<ArrayList<Wallet>>() {
            @Override
            public void onChanged(ArrayList<Wallet> wallets) {
                WalletListDialog walletListDialog = new WalletListDialog(wallets);
                walletListDialog.setClickListener(new WalletListDialog.OnClickListener() {
                    @Override
                    public void onWalletClick(Wallet wallet) {
                        walletListDialog.dismiss();
                        mWallet = wallet;
                        PrefsUtils.setSelectedWalletId(requireContext(), wallet.getId());
                        transactionList.clear();
                        binding.barChart.clear();
                        binding.pieChart.clear();
                        loadWalletDetails();
                        loadTransactions(wallet.getId());
                    }

                    @Override
                    public void onAddClick() {
                        walletListDialog.dismiss();
                        handleAddWalletBtnClick();
                    }

                    @Override
                    public void onEditClick(Wallet wallet) {
                        walletListDialog.dismiss();
                        handleEditWalletBtn(wallet);
                    }
                });
                walletListDialog.show(getParentFragmentManager(), "walletListDialog");
                walletsViewModel.loadWallets().removeObserver(this);
            }
        });
    }
}