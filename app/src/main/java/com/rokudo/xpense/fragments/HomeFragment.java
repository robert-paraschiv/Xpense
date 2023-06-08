package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.models.WalletUser.getOtherUserProfilePic;
import static com.rokudo.xpense.utils.BarChartUtils.setupBarChart;
import static com.rokudo.xpense.utils.BarChartUtils.updateBarchartData;
import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;
import static com.rokudo.xpense.utils.PieChartUtils.updatePieChartData;
import static com.rokudo.xpense.utils.SpentMostUtils.updateSpentMostOn;
import static com.rokudo.xpense.utils.TransactionUtils.updateLatestTransactionUI;
import static com.rokudo.xpense.utils.UserUtils.checkIfUserPicIsDifferent;
import static com.rokudo.xpense.utils.dialogs.DialogUtils.getCircularProgressDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.transition.Hold;
import com.google.android.material.transition.MaterialFadeThrough;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.SpentMostAdapter;
import com.rokudo.xpense.data.viewmodels.BankApiViewModel;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentHomeBinding;
import com.rokudo.xpense.models.BAccount;
import com.rokudo.xpense.models.SpentMostItem;
import com.rokudo.xpense.models.StatisticsDoc;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.ShimmerUtils;
import com.rokudo.xpense.utils.dialogs.AdjustBalanceDialog;
import com.rokudo.xpense.utils.dialogs.DialogUtils;
import com.rokudo.xpense.utils.dialogs.PersonInfoDialogFragment;
import com.rokudo.xpense.utils.dialogs.WalletListDialog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;

    private ListenerRegistration userDetailsListenerRegistration;
    private Wallet mWallet;
    private WalletsViewModel walletsViewModel;
    private StatisticsViewModel statisticsViewModel;
    private TransactionViewModel transactionViewModel;
    private BankApiViewModel bankApiViewModel;
    private SpentMostAdapter adapter;
    boolean firstPictureLoad = true;
    private final List<SpentMostItem> spentMostItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            binding.bottomNavView.setItemIconTintList(null);
            walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
            transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
            statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);
            bankApiViewModel = new ViewModelProvider(requireActivity()).get(BankApiViewModel.class);
            initOnClicks();

            setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor(), true);
            setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), true);

            initSpentMostOn();
        }


        loadWalletDetails();

        return binding.getRoot();
    }

    private void initSpentMostOn() {
        RecyclerView spentMostRv = binding.spentMostRv;
//        spentMostRv.setHasFixedSize(false);
        adapter = new SpentMostAdapter(spentMostItems);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        spentMostRv.setLayoutManager(linearLayoutManager);
        spentMostRv.setAdapter(adapter);
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(spentMostRv);

        spentMostRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(linearLayoutManager);
                    int pos;
                    if (centerView != null) {
                        pos = linearLayoutManager.getPosition(centerView);
                        switch (pos) {
                            case 0:
                                binding.radioBtn1.setChecked(true);
                                break;
                            case 1:
                                binding.radioBtn2.setChecked(true);
                                break;
                            case 2:
                                binding.radioBtn3.setChecked(true);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });

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
                loadBankAccountBalance(wallet.getbAccount());
                mWallet = wallet;
            }
            Log.d(TAG, "loadWalletDetails: wallet change observed");
        });
    }

    private void loadBankAccountBalance(BAccount bAccount) {
        bankApiViewModel.getAccountBalances(bAccount.getLinked_acc_id())
                .observe(getViewLifecycleOwner(), balances -> {
                    Log.d(TAG, "loadBankAccountBalance: ");
                    binding.bankAmount.setText(balances.getBalances()[0].getBalanceAmount().get("amount"));
                    binding.bankCurrency.setText(balances.getBalances()[0].getBalanceAmount().get("currency"));
                    ShimmerUtils.transitionShimmerLayoutToFinalView(
                            binding.bankAccCardShimmer,
                            binding.bankAccBalanceLayout,
                            requireContext());
                });
    }


    private void loadLast7DaysTransactions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                23, 59);
        Date end = calendar.getTime();

        Date start = new Date(end.getTime() - Duration.ofDays(7).toMillis());
        LocalDateTime localDateTime = start
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        calendar.set(localDateTime.getYear(),
                localDateTime.getMonth().getValue() - 1,
                localDateTime.getDayOfMonth(),
                0, 0);

        start = calendar.getTime();

        loadTransactions(start, end);
    }

    private void loadTransactions(Date start, Date end) {
        transactionViewModel
                .loadTransactionsDateInterval(mWallet.getId(), start, end)
                .observe(getViewLifecycleOwner(), values -> {
                    if (values == null || values.isEmpty()) {
                        Log.e(TAG, "loadTransactions: empty");
                    } else {
                        updateBarchartData(binding.barChart,
                                values,
                                new TextView(requireContext()).getCurrentTextColor());
                    }
                });
    }


    private void loadTransactions(String id) {
        statisticsViewModel
                .listenForStatisticsDoc(id, new Date())
                .observe(getViewLifecycleOwner(), val -> {
                    Log.d(TAG, "loadStatistics: statistics change observed " + val);
                    if (val == null) {
                        return;
                    }

                    boolean shouldUpdateStatistics = isShouldUpdateStatistics(val);


                    if (shouldUpdateStatistics
                            || binding.pieChart.getData() == null
                            || binding.barChart.getData() == null) {
                        statisticsViewModel.setHomeStoredStatisticsDoc(val);
                        updatePieChartData(binding.pieChart,
                                mWallet == null ? "" : mWallet.getCurrency(),
                                val.getAmountByCategory(),
                                val.getTotalAmountSpent(),
                                true);

                        updateBarchartData(binding.barChart,
                                new ArrayList<>(val.getTransactions().values()),
                                new TextView(requireContext()).getCurrentTextColor());

                        updateSpentMostOn(new ArrayList<>(val.getTransactions().values()),
                                mWallet == null ? "" : mWallet.getCurrency(),
                                adapter, requireActivity());
                    }
                });

        transactionViewModel.loadLatestTransaction(id).observe(getViewLifecycleOwner(), value -> {
            if (value != null) {
                updateLatestTransactionUI(value, binding, requireContext());
            }
        });
    }

    private boolean isShouldUpdateStatistics(StatisticsDoc val) {
        if (statisticsViewModel.getHomeStoredStatisticsDoc() == null) {
            return true;
        }
        boolean shouldUpdateStatistics = statisticsViewModel.getHomeStoredStatisticsDoc().getLatestUpdateTime() == null
                || !statisticsViewModel.getHomeStoredStatisticsDoc().getLatestUpdateTime().equals(val.getLatestUpdateTime());

        if (!statisticsViewModel.getHomeStoredStatisticsDoc().getDocPath().equals(val.getDocPath())) {
            return true;
        }

        if (!statisticsViewModel.getHomeStoredStatisticsDoc().getAmountByCategory().equals(val.getAmountByCategory())) {
            return true;
        }


        return shouldUpdateStatistics;
    }

    @SuppressLint("SetTextI18n")
    private void updateWalletUI(Wallet wallet) {
        binding.walletTitle.setText(wallet.getTitle());
        binding.walletAmount.setText(wallet.getAmount().toString());
        binding.walletCurrency.setText(wallet.getCurrency());
        if (wallet.getWalletUsers() == null
                || wallet.getWalletUsers().isEmpty()
                || wallet.getWalletUsers().size() < 2) {
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

        if (wallet.getbAccount() == null) {
            binding.bankAccountChip.setText("+ Bank Account");
            binding.bankAccountChip
                    .setChipIconTint(ColorStateList.valueOf(
                            getResources().
                                    getColor(R.color.sharedIconTintColor, requireContext().getTheme())));
            binding.bankAccountChip
                    .setChipIcon(AppCompatResources
                            .getDrawable(requireContext(), R.drawable.ic_baseline_local_atm_24));
        } else {
            binding.bankAccountChip.setText(wallet.getbAccount().getBankName());
            Glide.with(binding.bankAccountChip)
                    .asDrawable()
                    .load(wallet.getbAccount().getBankPic())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(requireContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            binding.bankAccountChip.setChipIcon(resource);
                            binding.bankAccountChip.setChipIconTint(null);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });

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
                userDetailsListenerRegistration = usersRef.document(FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getPhoneNumber())
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
            Log.d(TAG, "userDetailsEventListener: got data");
        }
    };

    private void updateUserDetailsUI(User user) {
//        binding.welcomeTv.setText(String.format("Welcome, %s", user.getName()));

        if (firstPictureLoad || checkIfUserPicIsDifferent(user, DatabaseUtils.getCurrentUser())) {
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(requireContext());
            Glide.with(requireContext())
                    .load(user.getPictureUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            binding.bottomNavView.getMenu().getItem(3).setIcon(resource);
                            firstPictureLoad = false;
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            binding.bottomNavView.getMenu().getItem(3).setIcon(placeholder);
                            firstPictureLoad = false;
                        }
                    });
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void initOnClicks() {
//        binding.profileImage.setOnClickListener(view -> navigateToSettings());
        binding.addWalletBtn.setOnClickListener(view -> handleAddWalletBtnClick());
        binding.walletDropDownBtn.setOnClickListener(view -> showWalletList());
        binding.walletTitle.setOnClickListener(view -> showWalletList());
        binding.addTransactionBtn.setOnClickListener(view -> navigateToAddTransaction());
        binding.seeAllTransactionsBtn.setOnClickListener(view -> navigateToTransactionsListFragment());
        binding.latestTransactionCard.setOnClickListener(v -> navigateToTransactionsListFragment());
        binding.adjustBalanceBtn.setOnClickListener(view -> handleAdjustBalanceBtnClick());
        binding.walletAmountCard.setOnClickListener(view -> handleAdjustBalanceBtnClick());
        binding.barChartCard.setOnClickListener(view -> navigateToBarDetails(false));
        binding.pieChartCard.setOnClickListener(view -> navigateToPieDetails());
//        binding.openBankFab.setOnClickListener(v -> navigateToBankFragment());
        binding.bankAccountChip.setOnClickListener(v -> handleBankChipClick());
        binding.bankAmountCard.setOnClickListener(v -> handleBankChipClick());
        binding.sharedWithIcon.setOnClickListener(v -> showPersonInfo());
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    break;
                case R.id.action_analytics:
                    navigateToBarDetails(true);
                    break;
                case R.id.action_bank:
                    navigateToBankAccountDetails(true);
                    break;
                case R.id.action_profile:
                    navigateToSettings();
                    break;
                default:
                    return false;
            }
            return false;
        });
    }

    private void showPersonInfo() {
        User user = new User();
        mWallet.getWalletUsers().forEach(walletUser -> {
            if (!walletUser.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                user.setPictureUrl(walletUser.getUserPic());
                user.setUid(walletUser.getUserId());
                user.setName(walletUser.getUserName());
            }
        });
        PersonInfoDialogFragment personInfoDialogFragment = new PersonInfoDialogFragment(user);
        personInfoDialogFragment.show(getParentFragmentManager(), "SharedWithDialog");
    }

    private void handleBankChipClick() {
        if (mWallet.getbAccount() == null) {
            navigateToBankFragment();
        } else {
            navigateToBankAccountDetails(false);
        }
    }

    private void navigateToBankAccountDetails(boolean bottomNavAction) {
        binding.bankAmountCard.setTransitionName("bankAccountDetailsTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.bankAmountCard, "bankAccountDetailsTransition")
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToBAccountDetailsFragment(mWallet.getbAccount(), bottomNavAction);


        if (bottomNavAction) {

            MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            setExitTransition(exit);
            setReenterTransition(reenter);

            Navigation.findNavController(binding.getRoot()).navigate(navDirections);
        } else {

            Hold hold = new Hold();
            hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            setExitTransition(hold);
            setReenterTransition(hold);

            Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
        }
    }

    private void navigateToBankFragment() {
        Navigation.findNavController(binding.getRoot())
                .navigate(HomeFragmentDirections
                        .actionHomeFragmentToConnectToBankFragment(mWallet.getId()));
    }

    private void navigateToBarDetails(boolean bottomNavAction) {
        binding.barChartCard.setTransitionName("barChartCard");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.barChartCard, "barChartCard")
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAnalyticsFragment("bar", mWallet, bottomNavAction);

        if (bottomNavAction) {

            MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            setExitTransition(exit);
            setReenterTransition(reenter);

            Navigation.findNavController(binding.getRoot()).navigate(navDirections);
        } else {

            Hold hold = new Hold();
            hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            setExitTransition(hold);
            setReenterTransition(hold);

            Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
        }
    }

    private void navigateToPieDetails() {
        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.pieChartCard, "pieChartCard")
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAnalyticsFragment("pie", mWallet, false);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void handleAddWalletBtnClick() {
        binding.addWalletBtn.setTransitionName("editWalletTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(binding.addWalletBtn, "editWalletTransition")
                .build();

        NavDirections navDirections = HomeFragmentDirections.actionHomeFragmentToEditWalletFragment(null);

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

        Navigation.findNavController(binding.getRoot())
                .navigate(HomeFragmentDirections
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
                .actionHomeFragmentToAddTransactionLayout(mWallet.getId(),
                        mWallet.getCurrency(),
                        transaction, false);

        Hold hold = new Hold();
        hold.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(hold);
        setReenterTransition(hold);

        Navigation.findNavController(binding.getRoot()).navigate(navDirections, extras);
    }

    private void navigateToSettings() {
//        binding.profileImage.setTransitionName("settingsTransition");

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
//                .addSharedElement(binding.profileImage, "settingsTransition")
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
                .addSharedElement(binding.addTransactionBtn,
                        getString(R.string.transition_name_add_transaction))
                .build();

        NavDirections navDirections = HomeFragmentDirections
                .actionHomeFragmentToAddTransactionLayout(mWallet.getId(),
                        mWallet.getCurrency(),
                        null, false);

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

        Navigation.findNavController(binding.getRoot())
                .navigate(HomeFragmentDirections
                        .actionHomeFragmentToListTransactionsFragment(mWallet.getId(),
                                mWallet.getCurrency()));
    }

    private void showWalletList() {
        walletsViewModel.loadWallets().observe(getViewLifecycleOwner(), new Observer<ArrayList<Wallet>>() {
            @Override
            public void onChanged(ArrayList<Wallet> wallets) {
                if (wallets == null || wallets.isEmpty()) {
                    return;
                }

                WalletListDialog walletListDialog = new WalletListDialog(wallets);
                walletListDialog.setClickListener(new WalletListDialog.OnClickListener() {
                    @Override
                    public void onWalletClick(Wallet wallet) {
                        walletListDialog.dismiss();
                        mWallet = wallet;
                        PrefsUtils.setSelectedWalletId(requireContext(), wallet.getId());
                        PrefsUtils.saveBAccountToPrefs(requireContext(), wallet.getbAccount());
                        binding.barChart.clear();
                        binding.pieChart.clear();
                        loadWalletDetails();
                        loadTransactions(wallet.getId());
                        loadLast7DaysTransactions();
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