package com.rokudo.xpense.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.DatabaseUtils.usersRef;
import static com.rokudo.xpense.utils.UserUtils.checkIfUserPicIsDifferent;
import static com.rokudo.xpense.utils.dialogs.DialogUtils.getCircularProgressDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            setupBarChart();

            setupPieChart();
//            loadPieChartData();

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
                    updatePieChartData(values);
                    updateLatestTransactionUI();
                    gotTransactionsOnce = true;
                });
    }

    @SuppressLint("SetTextI18n")
    private void updateLatestTransactionUI() {
        if (transactionList.isEmpty()) {
            binding.lastTransactionLayout.setVisibility(View.GONE);
        } else {
            binding.lastTransactionLayout.setVisibility(View.VISIBLE);
            Transaction transaction = transactionList.get(0);
            String transAmountPrefix;
            if (transaction.getType().equals("Income")) {
                binding.latestTransactionItem.transactionAmount.setTextColor(getResources().getColor(android.R.color.holo_green_dark, requireActivity().getTheme()));
                transAmountPrefix = "+ ";
            } else {
                transAmountPrefix = "- ";
                binding.latestTransactionItem.transactionAmount.setTextColor(getResources().getColor(android.R.color.holo_red_dark, requireActivity().getTheme()));
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

    private void setupPieChart() {
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setTouchEnabled(true);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setHighlightPerTapEnabled(true);
        binding.pieChart.setEntryLabelTextSize(10f);
        binding.pieChart.setEntryLabelColor(Color.BLACK);
        binding.pieChart.setCenterText("45123 Lei");
        binding.pieChart.setCenterTextSize(11f);
        binding.pieChart.setHoleRadius(48f);
        binding.pieChart.setCenterTextColor(new TextView(requireContext()).getCurrentTextColor());
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setDrawEntryLabels(false);
        Legend l = binding.pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);
        binding.pieChart.setTransparentCircleRadius(52f);
    }

    private void updatePieChartData(List<Transaction> transactionList) {
        Map<String, Double> categories = new HashMap<>();
        Double sum = 0.0;
        for (Transaction transaction : transactionList) {
            if (transaction.getType().equals(Transaction.INCOME_TYPE))
                continue;
            if (categories.containsKey(transaction.getCategory())) {
                Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                categories.put(transaction.getCategory(), transaction.getAmount());
            }
            sum += transaction.getAmount();
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        Double finalSum = sum;
        categories.forEach((key, value) -> entries.add(new PieEntry(getPercentageOfCategory(value, finalSum), key)));
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        binding.pieChart.setCenterText(sum + " " + wallet.getCurrency());
        binding.pieChart.setData(data);
        binding.pieChart.invalidate();

        binding.pieChart.animateY(1400);
        Log.d(TAG, "updatePieChartData: ");
    }

    private float getPercentageOfCategory(Double value, Double finalSum) {
        return (float) ((value * 100) / finalSum);
    }

    private void setupBarChart() {
        BarData data = new BarData(getDataSet());
        binding.barChart.setData(data);
        binding.barChart.setMaxVisibleValueCount(60);
        binding.barChart.setPinchZoom(false);
        binding.barChart.setDoubleTapToZoomEnabled(false);
        binding.barChart.setScaleEnabled(false);
//        binding.barChart.setTouchEnabled(false);
        binding.barChart.setDrawBarShadow(false);
        binding.barChart.setDrawGridBackground(false);
        binding.barChart.getLegend().setEnabled(false);
        binding.barChart.setFitBars(true);

        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(new TextView(requireContext()).getCurrentTextColor());

        binding.barChart.getAxisLeft().setEnabled(false);
        binding.barChart.getAxisRight().setEnabled(false);

        binding.barChart.animateY(2000);
        binding.barChart.invalidate();

        binding.barChart.getDescription().setEnabled(false);
    }

    private ArrayList<IBarDataSet> getDataSet() {
        ArrayList<IBarDataSet> dataSets;

        ArrayList<BarEntry> valueSet = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(18, 821); // Jan
        valueSet.add(v1e1);
        BarEntry v1e2 = new BarEntry(19, 334); // Feb
        valueSet.add(v1e2);
        BarEntry v2e1 = new BarEntry(20, 1179); // Jan
        valueSet.add(v2e1);
        BarEntry v2e2 = new BarEntry(21, 714); // Jan
        valueSet.add(v2e2);
        BarEntry v2e3 = new BarEntry(22, 245); // Jan
        valueSet.add(v2e3);

        BarDataSet barDataSet = new BarDataSet(valueSet, "Transport");
//        barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(10);
        barDataSet.setValueTextColor(new TextView(requireContext()).getCurrentTextColor());

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        return dataSets;
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
//                getConversations();
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