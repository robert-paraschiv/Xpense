package com.rokudo.xpense.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.BarDetailsUtils.setupBarChart;
import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialSharedAxis;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ExpenseCategoryAdapter;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAnalyticsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.BarDetailsUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.PieChartUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnalyticsFragment extends Fragment {
    private static final String TAG = "AnalyticsFragment";

    private FragmentAnalyticsBinding binding;
    private TransactionViewModel transactionViewModel;
    private StatisticsViewModel statisticsViewModel;
    private Double sum = 0.0;
    private Date selectedDate = new Date();
    private Wallet wallet;
    private ExpenseCategoryAdapter adapter;
    private List<ExpenseCategory> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater);

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        initOnClicks();
        initDateChip();
        buildDatePickerRv();
        getArgsPassed();
        setUpExpenseCategoryRv();

        getInitialTransactionData();
        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), false);
        setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        final ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                viewGroup.getViewTreeObserver().removeOnPreDrawListener(this);
                startPostponedEnterTransition();
                return true;
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void initOnClicks() {
        binding.analyticsTypeImage.setOnClickListener(v -> toggleChartsVisibility());

        binding.monthChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip chip = group.findViewById(checkedIds.get(0));
            if (chip != null) {
                try {
                    selectedDate = monthYearFormat.parse(chip.getText().toString());
                    BarDetailsUtils.setBarLabelRotation(binding.barChart, true);
                    resetCategoriesRv();
                    loadMonthTransactions(selectedDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        binding.dateChip.setOnClickListener(v -> {
            MaterialSharedAxis materialContainerTransform = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            materialContainerTransform.setPathMotion(new MaterialArcMotion());
            materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

            BarDetailsUtils.setBarLabelRotation(binding.barChart, true);
            TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

            binding.dateChipCard.setVisibility(GONE);
            binding.monthCard.setVisibility(VISIBLE);

            binding.monthHorizontalScroll.postDelayed(() -> {
                Chip chip = binding.monthChipGroup.findViewWithTag(monthYearFormat.format(selectedDate.getTime()));
                binding.monthHorizontalScroll.smoothScrollTo(chip.getLeft() - chip.getPaddingLeft(), chip.getTop());
            }, 30);
        });
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetCategoriesRv() {
        categoryList = new ArrayList<>();
        adapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency());
        binding.categoriesRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void toggleChartsVisibility() {
        if (binding.barChart.getVisibility() == View.VISIBLE) {
            binding.barChart.setVisibility(View.GONE);
            binding.pieChart.setVisibility(View.VISIBLE);
            binding.analyticsTypeImage
                    .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_bar_chart_24));
        } else {
            binding.barChart.setVisibility(View.VISIBLE);
            binding.pieChart.setVisibility(View.GONE);
            binding.analyticsTypeImage
                    .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_pie_chart_24));
        }
    }

    private void setUpExpenseCategoryRv() {
        adapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency());
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void sortTransactions(List<Transaction> values) {
        Map<String, Double> categories = new HashMap<>();
        Map<String, List<Transaction>> transactionsByCategory = new HashMap<>();
        for (Transaction transaction : values) {
            if (transaction.getType().equals(Transaction.INCOME_TYPE))
                continue;

            sum += transaction.getAmount();

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

        categories.forEach((key, value) -> {
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

        binding.totalAmountTv.setText(wallet.getCurrency() + " " + new DecimalFormat("0.00").format(sum));
    }

    private void getInitialTransactionData() {
        sortTransactions(transactionViewModel.getStoredTransactionList());
        PieChartUtils.updatePieChartData(binding.pieChart,
                wallet.getCurrency(),
                statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                statisticsViewModel.getStoredStatisticsDoc().getTotalAmountSpent(),
                false);
        BarDetailsUtils.updateBarchartData(binding.barChart,
                new ArrayList<>(statisticsViewModel.getStoredStatisticsDoc().getTransactions().values()),
                new TextView(requireContext()).getCurrentTextColor(),
                false);
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

    private void initDateChip() {
        binding.dateChip.setText(monthYearFormat.format(new Date()));
    }

    private void buildDatePickerRv() {

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

                LocalDate innerLocalDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
                if (innerLocalDate.isAfter(localDate)) {
                    break;
                }

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_month_picker, binding.monthChipGroup, false);
                chip.setText(monthYearFormat.format(calendar.getTime()));
                chip.setTag(monthYearFormat.format(calendar.getTime()));
                if (monthYearFormat.format(calendar.getTime()).equals(monthYearFormat.format(new Date()))) {
                    chip.setChecked(true);
                }
                binding.monthChipGroup.addView(chip);
            }

            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

        } while (calendar.get(Calendar.YEAR) <= yearNow);
    }

    private void loadMonthTransactions(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH),
                23, 59);
        Date end = calendar.getTime();

        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH),
                0, 0);
        Date start = calendar.getTime();

        binding.dateChip.setText(monthYearFormat.format(start));
        if (monthYearFormat.format(start).equals(monthYearFormat.format(new Date()))) {
            binding.periodCard.setVisibility(VISIBLE);
            binding.allMonthChip.setChecked(true);
        } else {
            binding.periodCard.setVisibility(GONE);
        }

        MaterialSharedAxis materialContainerTransform = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        materialContainerTransform.setPathMotion(new MaterialArcMotion());
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

        binding.monthCard.setVisibility(GONE);
        binding.dateChipCard.setVisibility(VISIBLE);

        loadTransactions(start, end, true, false);
    }

    private void loadTransactions(Date start, Date end, Boolean updateBar, Boolean last7Days) {
        transactionViewModel
                .loadTransactionsDateInterval(wallet.getId(), start, end)
                .observe(getViewLifecycleOwner(), values -> {
                    if (values == null || values.isEmpty()) {
                        Log.e(TAG, "loadTransactions: empty");
                        BarDetailsUtils.updateBarchartData(binding.barChart,
                                new ArrayList<>(),
                                new TextView(requireContext()).getCurrentTextColor(), last7Days);
                        binding.totalAmountTv.setText(R.string.no_data_provided);
                    } else {
                        sortTransactions(values);
                        if (updateBar) {
                            BarDetailsUtils.updateBarchartData(binding.barChart,
                                    values,
                                    new TextView(requireContext()).getCurrentTextColor(), last7Days);
//                            transEntryList = BarDetailsUtils.getTransEntryArrayList(values, last7Days);
                        }
//                        if (firstLoad) {
//                            startPostponedEnterTransition();
//                            firstLoad = false;
//                        }
                        binding.categoriesRv.scheduleLayoutAnimation();
                    }
                });
    }


    private void getArgsPassed() {
        AnalyticsFragmentArgs args = AnalyticsFragmentArgs.fromBundle(requireArguments());
        wallet = args.getWallet();

        switch (args.getType()) {
            case "bar":
                showBarChartDefault();
                break;
            case "pie":
                showPieChartDefault();
                break;
            default:
                break;
        }
    }

    private void showPieChartDefault() {
        binding.analyticsRoot.setTransitionName("pieChartCard");
        binding.pieChart.setVisibility(View.VISIBLE);
        binding.barChart.setVisibility(View.GONE);
        binding.analyticsTypeImage
                .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_bar_chart_24));
    }

    private void showBarChartDefault() {
        binding.analyticsRoot.setTransitionName("barChartCard");
        binding.pieChart.setVisibility(View.GONE);
        binding.barChart.setVisibility(View.VISIBLE);
        binding.analyticsTypeImage
                .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_pie_chart_24));
    }
}