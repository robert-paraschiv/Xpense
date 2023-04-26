package com.rokudo.xpense.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.AnalyticsBarUtils.setupBarChart;
import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;
import static com.rokudo.xpense.utils.DateUtils.yearFormat;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rokudo.xpense.adapters.OnTransClickListener;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.databinding.FragmentAnalyticsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.AnalyticsBarUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.PieChartUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnalyticsFragment extends Fragment implements OnTransClickListener {
    private static final String TAG = "AnalyticsFragment";

    private FragmentAnalyticsBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private Date selectedDate = new Date();
    private Wallet wallet;
    private ExpenseCategoryAdapter adapter;

    private boolean isYearMode = false;
    private List<ExpenseCategory> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater);

        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        initOnClicks();
        buildDatePickerRv();
        getArgsPassed();
        setUpExpenseCategoryRv();

        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), false);
        setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());
        getInitialTransactionData();


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
                    selectedDate = isYearMode ? yearFormat.parse(chip.getText().toString())
                            : monthYearFormat.parse(chip.getText().toString());
                    AnalyticsBarUtils.setBarLabelRotation(binding.barChart, true);
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

            AnalyticsBarUtils.setBarLabelRotation(binding.barChart, true);
            TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

            binding.dateChipCard.setVisibility(GONE);
            binding.monthCard.setVisibility(VISIBLE);

            binding.monthHorizontalScroll.postDelayed(() -> {
                Chip chip = binding.monthChipGroup.findViewWithTag(isYearMode ? yearFormat.format(selectedDate.getTime())
                        : monthYearFormat.format(selectedDate.getTime()));
                binding.monthHorizontalScroll.smoothScrollTo(chip.getLeft() - chip.getPaddingLeft(), chip.getTop());
            }, 30);
        });
        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());

        binding.thisMonthChip.setOnClickListener(v -> {
            AnalyticsBarUtils.setBarLabelRotation(binding.barChart, true);
            isYearMode = false;
            buildDatePickerRv();
            if (binding.dateChipCard.getVisibility() == GONE) {
                hideChipGroup();
            }
            resetCategoriesRv();
            loadTransactions(selectedDate, isYearMode);
        });
        binding.thisYearChip.setOnClickListener(v -> {
            isYearMode = true;
            buildDatePickerRv();
            if (binding.dateChipCard.getVisibility() == GONE) {
                hideChipGroup();
            }
            resetCategoriesRv();
            loadTransactions(selectedDate, isYearMode);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetCategoriesRv() {
        categoryList = new ArrayList<>();
        adapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency(), this);
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
        adapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency(), this);
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    private void populateCategoriesRv(Map<String, Double> categories,
                                      Map<String, Map<String, Transaction>> transactionsByCategory) {

        categories = MapUtil.sortByValue(categories);

        categories.forEach((key, value) -> {
            if (key.equals("Income") || value == 0d) {
                return;
            }
            ExpenseCategory expenseCategory = new ExpenseCategory(key,
                    new ArrayList<>(Objects.requireNonNull(transactionsByCategory.get(key)).values()),
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

    }

    private void getInitialTransactionData() {
        populateCategoriesRv(statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                statisticsViewModel.getStoredStatisticsDoc().getCategories());
        PieChartUtils.updatePieChartData(binding.pieChart,
                wallet.getCurrency(),
                statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                statisticsViewModel.getStoredStatisticsDoc().getTotalAmountSpent(),
                false);
        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                new ArrayList<>(statisticsViewModel.getStoredStatisticsDoc().getTransactions().values()),
                new TextView(requireContext()).getCurrentTextColor(),
                false);

        binding.totalAmountTv.setText(String.format("%s %s",
                wallet.getCurrency(),
                new DecimalFormat("0.00").format(statisticsViewModel.getStoredStatisticsDoc().getTotalAmountSpent())));
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
            if (isYearMode) {
                LocalDate innerLocalDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, 1);
                if (innerLocalDate.isAfter(localDate)) {
                    break;
                }
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_month_picker, binding.monthChipGroup, false);
                chip.setText(yearFormat.format(calendar.getTime()));
                chip.setTag(yearFormat.format(calendar.getTime()));
                if (yearFormat.format(calendar.getTime()).equals(yearFormat.format(new Date()))) {
                    chip.setChecked(true);
                }
                binding.monthChipGroup.addView(chip);

            } else {
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
                        selectedDate = new Date();
                    }
                    binding.monthChipGroup.addView(chip);
                }
            }

            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

        } while (calendar.get(Calendar.YEAR) <= yearNow);

        binding.dateChip.setText(isYearMode ? yearFormat.format(new Date()) : monthYearFormat.format(new Date()));
    }

    private void loadMonthTransactions(Date selectedMonthDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedMonthDate);
        calendar.set(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.getActualMinimum(Calendar.DAY_OF_MONTH),
                0, 0);
        Date date = calendar.getTime();

        binding.dateChip.setText(isYearMode ? yearFormat.format(date) : monthYearFormat.format(date));

        hideChipGroup();

        loadTransactions(date, isYearMode);
    }

    private void hideChipGroup() {
        MaterialSharedAxis materialContainerTransform = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        materialContainerTransform.setPathMotion(new MaterialArcMotion());
        materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        TransitionManager.beginDelayedTransition(binding.monthLayout, materialContainerTransform);

        binding.monthCard.setVisibility(GONE);
        binding.dateChipCard.setVisibility(VISIBLE);
    }

    private void loadTransactions(Date start, boolean isYearSelected) {
        statisticsViewModel.loadStatisticsDoc(wallet.getId(), start, isYearSelected)
                .observe(getViewLifecycleOwner(), statisticsDoc -> {
                    if (statisticsDoc == null || statisticsDoc.getTransactions().isEmpty()) {
                        Log.e(TAG, "loadTransactions: empty");
                        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                                new ArrayList<>(),
                                new TextView(requireContext()).getCurrentTextColor(), isYearSelected);

                        PieChartUtils.updatePieChartData(binding.pieChart,
                                wallet.getCurrency(),
                                new HashMap<>(),
                                0d,
                                false);
                        binding.totalAmountTv.setText(R.string.no_data_provided);
                    } else {
                        List<Transaction> transactions = new ArrayList<>(statisticsDoc.getTransactions().values());
                        populateCategoriesRv(statisticsDoc.getAmountByCategory(), statisticsDoc.getCategories());
                        transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());

                        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                                transactions,
                                new TextView(requireContext()).getCurrentTextColor(), isYearSelected);

                        PieChartUtils.updatePieChartData(binding.pieChart,
                                wallet.getCurrency(),
                                statisticsDoc.getAmountByCategory(),
                                statisticsDoc.getTotalAmountSpent(),
                                false);


                        binding.totalAmountTv.setText(String.format("%s %s",
                                wallet.getCurrency(),
                                new DecimalFormat("0.00").format(statisticsDoc.getTotalAmountSpent())));

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

    @Override
    public void onClick(Transaction transaction) {
        MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
        MaterialSharedAxis reenter = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        reenter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));

        setExitTransition(exit);
        setReenterTransition(reenter);


        Navigation.findNavController(binding.getRoot())
                .navigate(AnalyticsFragmentDirections
                        .actionAnalyticsFragmentToAddTransactionFragment(wallet.getId(),
                                wallet.getCurrency(),
                                transaction));
    }
}