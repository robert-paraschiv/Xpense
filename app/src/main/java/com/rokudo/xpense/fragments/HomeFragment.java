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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.WalletsViewModel;
import com.rokudo.xpense.databinding.FragmentHomeBinding;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.User;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.DatabaseUtils;
import com.rokudo.xpense.utils.PrefsUtils;
import com.rokudo.xpense.utils.dialogs.AdjustBalanceDialog;
import com.rokudo.xpense.utils.dialogs.WalletListDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private TransactionsAdapter adapter;

    private ListenerRegistration userDetailsListenerRegistration;
    private final List<Transaction> transactionList = new ArrayList<>();
    private Wallet wallet;
    private WalletsViewModel walletsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (binding == null) {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            walletsViewModel = new ViewModelProvider(requireActivity()).get(WalletsViewModel.class);
            initOnClicks();
            buildRecyclerView();
            initializeDummyRv();

            setupBarChart();

            setupPieChart();
            loadPieChartData();

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
                handleWalletsUpdate(wallet);
                this.wallet = wallet;
            }
        });
    }

    private void handleWalletsUpdate(Wallet wallet) {
        updateWalletUI(wallet);
    }

    @SuppressLint("SetTextI18n")
    private void updateWalletUI(Wallet wallet) {
        binding.walletTitle.setText(wallet.getTitle());
        binding.walletAmount.setText(wallet.getAmount().toString());
        binding.walletCurrency.setText(wallet.getCurrency());
    }

    private void setupPieChart() {
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setTouchEnabled(false);
        binding.pieChart.setUsePercentValues(false);
        binding.pieChart.setHighlightPerTapEnabled(true);
        binding.pieChart.setEntryLabelTextSize(8);
        binding.pieChart.setEntryLabelColor(Color.BLACK);
        binding.pieChart.setCenterText("45123 Lei");
        binding.pieChart.setCenterTextSize(12f);
        binding.pieChart.setHoleRadius(48);
        binding.pieChart.setCenterTextColor(new TextView(requireContext()).getCurrentTextColor());
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.getLegend().setEnabled(false);
    }

    private void loadPieChartData() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(20f, "Groceries"));
        entries.add(new PieEntry(15f, "Transport"));
        entries.add(new PieEntry(10f, "Bills"));
        entries.add(new PieEntry(30f, "Housing"));
        entries.add(new PieEntry(25f, "Other"));

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Category");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(6f);
        data.setValueTextColor(Color.BLACK);

        binding.pieChart.setData(data);
        binding.pieChart.invalidate();

        binding.pieChart.animateY(1400);
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

        ArrayList<BarEntry> valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(18, 821); // Jan
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry(19, 334); // Feb
        valueSet1.add(v1e2);

        ArrayList<BarEntry> valueSet2 = new ArrayList<>();
        BarEntry v2e1 = new BarEntry(20, 1179); // Jan
        valueSet2.add(v2e1);
        BarEntry v2e2 = new BarEntry(21, 714); // Jan
        valueSet2.add(v2e2);
        BarEntry v2e3 = new BarEntry(22, 245); // Jan
        valueSet2.add(v2e3);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Transport");
//        barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet1.setDrawValues(true);
        barDataSet1.setValueTextColor(new TextView(requireContext()).getCurrentTextColor());

        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Myeah");
//        barDataSet2.setColor(Color.rgb(0, 155, 155));
        barDataSet2.setDrawValues(true);
        barDataSet2.setValueTextSize(7);
        barDataSet2.setValueTextColor(new TextView(requireContext()).getCurrentTextColor());

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);
        return dataSets;
    }


    @SuppressLint("NotifyDataSetChanged")
    private void initializeDummyRv() {
        transactionList.add(new Transaction("Transport", 70.23, new Date(), "https://images.pexels.com/photos/1704488/pexels-photo-1704488.jpeg?cs=srgb&dl=pexels-suliman-sallehi-1704488.jpg&fm=jpg"));
        transactionList.add(new Transaction("Transport", 23.25, new Date(), "https://shotkit.com/wp-content/uploads/2021/06/cool-profile-pic-matheus-ferrero.jpeg"));
        transactionList.add(new Transaction("Transport", 10.35, new Date(), "https://learn.microsoft.com/answers/storage/attachments/209536-360-f-364211147-1qglvxv1tcq0ohz3fawufrtonzz8nq3e.jpg"));
        transactionList.add(new Transaction("Transport", 50.25, new Date(), "https://i.etsystatic.com/36532523/r/il/97ae46/4078306713/il_340x270.4078306713_n74s.jpg"));
        transactionList.add(new Transaction("Transport", 80.25, new Date(), "https://upload.wikimedia.org/wikipedia/commons/5/5f/Alberto_conversi_profile_pic.jpg"));
        transactionList.add(new Transaction("Transport", 30.35, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        transactionList.add(new Transaction("Transport", 6.23, new Date(), ""));
        adapter.notifyDataSetChanged();
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList);
        binding.recentTransactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        binding.recentTransactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
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