package com.rokudo.xpense.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialSharedAxis;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.OnTransClickListener;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.databinding.FragmentListTransactionsBinding;
import com.rokudo.xpense.models.Transaction;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ListTransactionsFragment extends Fragment implements OnTransClickListener {

    private FragmentListTransactionsBinding binding;
    private TransactionsAdapter adapter;

    boolean gotTransactionsOnce = false;
    private List<Transaction> transactionList = new ArrayList<>();
    private String walletId;
    private String currency;
    private Date selectedDate;
    private StatisticsViewModel statisticsViewModel;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListTransactionsBinding.inflate(inflater, container, false);

        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        initOnClicks();
        buildRecyclerView();
        ListTransactionsFragmentArgs args = ListTransactionsFragmentArgs.fromBundle(requireArguments());
        walletId = args.getWalletId();
        currency = args.getWalletCurrency();
        loadInitialTransactions();
        buildDatePickerRv();

        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(view).popBackStack());

        binding.monthChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip != null) {
                try {
                    selectedDate = monthYearFormat.parse(chip.getText().toString());
                    if (selectedDate != null) {
                        binding.dateChip.setText(monthYearFormat.format(selectedDate));
                    }
                    loadMonthTransactions(selectedDate);
                    hideChipGroup();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        binding.dateChip.setOnClickListener(v -> {
            MaterialSharedAxis materialContainerTransform = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            materialContainerTransform.setPathMotion(new MaterialArcMotion());
            materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

            binding.dateChipCard.setVisibility(GONE);
            binding.monthCard.setVisibility(VISIBLE);

            binding.monthHorizontalScroll.postDelayed(() -> {
                Chip chip = binding.monthChipGroup.findViewWithTag(monthYearFormat.format(selectedDate.getTime()));
                binding.monthHorizontalScroll.smoothScrollTo(chip.getLeft() - chip.getPaddingLeft(), chip.getTop());
            }, 30);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadMonthTransactions(Date selectedDate) {
        transactionList = new ArrayList<>();
        adapter.setTransactionList(transactionList);

        statisticsViewModel.loadStatisticsDoc(walletId, selectedDate, false)
                .observe(getViewLifecycleOwner(), statisticsDoc -> {

                    List<Transaction> transactions = new ArrayList<>(statisticsDoc.getTransactions().values());
                    transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());

                    transactions.forEach(transaction -> {
                        if (transactionList.contains(transaction)) {
                            transactionList.set(transactionList.indexOf(transaction), transaction);
                            adapter.notifyItemChanged(transactionList.indexOf(transaction));
                        } else {
                            if (gotTransactionsOnce) {
                                transactionList.add(0, transaction);
                                adapter.notifyItemInserted(0);
                            } else {
                                transactionList.add(transaction);
                                adapter.notifyItemInserted(transactionList.size() - 1);
                            }
                        }
                    });
                });
    }

    private void hideChipGroup() {
        MaterialSharedAxis materialContainerTransform = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        materialContainerTransform.setPathMotion(new MaterialArcMotion());
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

        binding.monthCard.setVisibility(GONE);
        binding.dateChipCard.setVisibility(VISIBLE);
    }

    private void buildDatePickerRv() {
        binding.monthChipGroup.removeAllViews();

        Calendar calendar = Calendar.getInstance();

        int yearNow = calendar.get(Calendar.YEAR);

        LocalDate localDate = LocalDate.of(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                1);

        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, 0);

        do {
            for (int i = 0; i < 12; i++) {
                calendar.set(Calendar.MONTH, i);

                LocalDate innerLocalDate = LocalDate.of(calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1,
                        1);
                if (innerLocalDate.isAfter(localDate)) {
                    break;
                }

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_month_picker,
                        binding.monthChipGroup,
                        false);
                chip.setText(monthYearFormat.format(calendar.getTime()));
                chip.setTag(monthYearFormat.format(calendar.getTime()));
                if (monthYearFormat.format(calendar.getTime()).equals(monthYearFormat.format(new Date()))) {
                    chip.setChecked(true);
                    selectedDate = new Date();
                }
                binding.monthChipGroup.addView(chip);
            }


            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

        } while (calendar.get(Calendar.YEAR) <= yearNow);

        binding.dateChip.setText(monthYearFormat.format(new Date()));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        setEnterTransition(enter);
        setReturnTransition(exit);
    }

    private void buildRecyclerView() {
        adapter = new TransactionsAdapter(transactionList, false, this);
        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.transactionsRv.setAdapter(adapter);
//        adapter.setOnItemClickListener();
    }

    private void loadInitialTransactions() {
        if (statisticsViewModel.getStoredStatisticsDoc() == null) {
            return;
        }

        List<Transaction> transactions = new ArrayList<>(statisticsViewModel.getStoredStatisticsDoc().getTransactions().values());
        transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());

        transactions.forEach(transaction -> {
            if (transactionList.contains(transaction)) {
                transactionList.set(transactionList.indexOf(transaction), transaction);
                adapter.notifyItemChanged(transactionList.indexOf(transaction));
            } else {
                if (gotTransactionsOnce) {
                    transactionList.add(0, transaction);
                    adapter.notifyItemInserted(0);
                } else {
                    transactionList.add(transaction);
                    adapter.notifyItemInserted(transactionList.size() - 1);
                }
            }
        });
    }

    @Override
    public void onClick(Transaction transaction) {

        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);

        Navigation.findNavController(binding.getRoot()).navigate(
                ListTransactionsFragmentDirections
                        .actionListTransactionsFragmentToAddTransactionLayout(walletId,
                                currency,
                                transaction,
                                true));
    }
}