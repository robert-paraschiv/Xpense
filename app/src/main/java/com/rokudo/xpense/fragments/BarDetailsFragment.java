package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ExpenseCategoryAdapter;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentBarDetailsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.TransEntry;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.BarDetailsUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BarDetailsFragment extends Fragment {
    private static final String TAG = "BarDetailsFragment";

    private FragmentBarDetailsBinding binding;
    private TransactionViewModel transactionViewModel;
    private ExpenseCategoryAdapter adapter;
    private Wallet mWallet;
    private List<ExpenseCategory> categoryList = new ArrayList<>();
    private List<TransEntry> transEntryList = new ArrayList<>();
    private boolean firstLoad = true;
    Double sum = 0.0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBarDetailsBinding.inflate(inflater, container, false);

        initOnClicks();
        initDateChip();

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        BarDetailsUtils.setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());
        setUpExpenseCategoryRv();

//        loadThisMonthTransactions();
        loadLast7DaysTransactions();

        return binding.getRoot();
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    private void initDateChip() {
        binding.dateChip.setText("This month");
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        view.postDelayed(() -> {
            if (firstLoad) {
                startPostponedEnterTransition();
            }
        }, 3000);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initOnClicks() {
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());
        binding.allMonthChip.setOnClickListener(v -> {
            resetCategoriesRv();
            loadThisMonthTransactions();
        });
        binding.last7DaysChip.setOnClickListener(v -> {
            resetCategoriesRv();
            loadLast7DaysTransactions();
        });
        binding.barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                resetCategoriesRv();
                Log.d(TAG, "onValueSelected: " + e.toString());
                TransEntry transEntry = transEntryList.get((int) e.getX());

                LocalDateTime localDateTime = transEntry
                        .getDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                Calendar calendar = Calendar.getInstance();
                calendar.set(localDateTime.getYear(),
                        localDateTime.getMonth().getValue() - 1,
                        localDateTime.getDayOfMonth(),
                        23, 59);
                Date end = calendar.getTime();

                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                Date start = calendar.getTime();

                loadTransactions(start, end, false);
            }

            @Override
            public void onNothingSelected() {
                resetCategoriesRv();
                if (binding.last7DaysChip.isChecked()) {
                    loadLast7DaysTransactions();
                } else if (binding.allMonthChip.isChecked()) {
                    loadThisMonthTransactions();
                }
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetCategoriesRv() {
        categoryList = new ArrayList<>();
        adapter = new ExpenseCategoryAdapter(categoryList, mWallet.getCurrency());
        binding.categoriesRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setUpExpenseCategoryRv() {
        BarDetailsFragmentArgs args = BarDetailsFragmentArgs.fromBundle(requireArguments());
        mWallet = args.getWallet();
        adapter = new ExpenseCategoryAdapter(categoryList, mWallet.getCurrency());
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    private void loadLast7DaysTransactions() {
        Date end = new Date();

        Date start = new Date(end.getTime() - Duration.ofDays(7).toMillis());
        LocalDateTime localDateTime = start
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Calendar calendar = Calendar.getInstance();
        calendar.set(localDateTime.getYear(),
                localDateTime.getMonth().getValue() - 1,
                localDateTime.getDayOfMonth(),
                0, 0);

        start = calendar.getTime();

        loadTransactions(start, end, true);
    }

    private void loadThisMonthTransactions() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                0, 0);
        Date end = new Date();

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH),
                0, 0);
        Date start = calendar.getTime();

        loadTransactions(start, end, true);
    }

    private void loadTransactions(Date start, Date end, Boolean updateBar) {
        transactionViewModel
                .loadTransactionsDateInterval(mWallet.getId(), start, end)
                .observe(getViewLifecycleOwner(), values -> {
                    if (values == null || values.isEmpty()) {
                        Log.e(TAG, "loadTransactions: empty");
                    } else {
                        sortTransactions(values);
                        if (updateBar) {
                            BarDetailsUtils.updateBarchartData(binding.barChart,
                                    values,
                                    new TextView(requireContext()).getCurrentTextColor());
                            transEntryList = BarDetailsUtils.getTransEntryArrayList(values);
                        }
                        if (firstLoad) {
                            startPostponedEnterTransition();
                            firstLoad = false;
                        }
                        binding.categoriesRv.scheduleLayoutAnimation();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void sortTransactions(List<Transaction> values) {
        Map<String, Double> categories = new HashMap<>();
        Map<String, List<Transaction>> transactionsByCategory = new HashMap<>();
        for (Transaction transaction : values) {
            if (transaction.getType().equals(Transaction.INCOME_TYPE))
                continue;

            if (transactionsByCategory.containsKey(transaction.getCategory())) {
                Objects.requireNonNull(transactionsByCategory
                                .get(transaction.getCategory()))
                        .add(transaction);
            } else {
                transactionsByCategory.put(transaction.getCategory(),
                        new ArrayList<>(Collections.singleton(transaction)));
            }

            if (categories.containsKey(transaction.getCategory())) {
                Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                categories.put(transaction.getCategory(),
                        amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                categories.put(transaction.getCategory(), transaction.getAmount());
            }
        }
        categories = MapUtil.sortByValue(categories);

        sum = 0.0;
        categories.forEach((key, value) -> {
            sum += value;
            ExpenseCategory expenseCategory = new ExpenseCategory(key,
                    transactionsByCategory.get(key),
                    null,
                    value);
            if (CategoriesUtil.expenseCategoryList.contains(expenseCategory)) {
                expenseCategory.setResourceId(
                        CategoriesUtil.expenseCategoryList.get(
                                        CategoriesUtil.expenseCategoryList.indexOf(expenseCategory))
                                .getResourceId());
                if (!categoryList.contains(expenseCategory)) {
                    categoryList.add(expenseCategory);
                    adapter.notifyItemInserted(categoryList.size() - 1);
                }
            }
        });

        binding.totalAmountTv.setText(mWallet.getCurrency() + " " + new DecimalFormat("0.00").format(sum));
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
}