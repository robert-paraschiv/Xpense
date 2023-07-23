package com.rokudo.xpense.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.AnalyticsBarUtils.getTransEntryArrayList;
import static com.rokudo.xpense.utils.AnalyticsBarUtils.setupBarChart;
import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;
import static com.rokudo.xpense.utils.DateUtils.yearFormat;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialArcMotion;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialSharedAxis;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ExpenseCategoryAdapter;
import com.rokudo.xpense.adapters.OnTransClickListener;
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.StatisticsViewModel;
import com.rokudo.xpense.databinding.FragmentAnalyticsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.TransEntry;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.AnalyticsBarUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.OnSwipeTouchListener;
import com.rokudo.xpense.utils.PieChartUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AnalyticsFragment extends Fragment implements OnTransClickListener {
    private static final String TAG = "AnalyticsFragment";

    private FragmentAnalyticsBinding binding;
    private StatisticsViewModel statisticsViewModel;
    private Date selectedDate = new Date();
    private Wallet wallet;
    private ExpenseCategoryAdapter expenseCategoryAdapter;
    private TransactionsAdapter transactionsAdapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private boolean loadedAll = false;

    private boolean isYearMode = false;
    private List<ExpenseCategory> categoryList = new ArrayList<>();

    private List<TransEntry> transEntryArrayList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater);

        postponeEnterTransition();

        statisticsViewModel = new ViewModelProvider(requireActivity()).get(StatisticsViewModel.class);

        initOnClicks();
        buildDatePickerRv();
        getArgsPassed();
        setUpExpenseCategoryRv();
        setUpTransactionsRv();

        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), false);
        setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());
        getInitialTransactionData();


        startPostponedEnterTransition();

        return binding.getRoot();
    }

    private void setUpTransactionsRv() {
        transactionsAdapter = new TransactionsAdapter(new ArrayList<>(), true, this);

        binding.transactionsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.transactionsRv.setAdapter(transactionsAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
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
            binding.barChart.highlightValue(null);
            binding.pieChart.highlightValue(null);
            if (binding.categoriesRv.getVisibility() == GONE) {
                binding.categoriesRv.setVisibility(VISIBLE);
                binding.transactionsRv.setVisibility(GONE);
            }

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
            binding.barChart.highlightValue(null);
            binding.pieChart.highlightValue(null);
            if (binding.categoriesRv.getVisibility() == GONE) {
                binding.categoriesRv.setVisibility(VISIBLE);
                binding.transactionsRv.setVisibility(GONE);
            }
            isYearMode = true;
            buildDatePickerRv();
            if (binding.dateChipCard.getVisibility() == GONE) {
                hideChipGroup();
            }
            resetCategoriesRv();
            loadTransactions(selectedDate, isYearMode);
        });

        binding.barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight h) {
                TransEntry transEntry = transEntryArrayList.get((int) entry.getX());
                Log.d(TAG, "onValueSelected: ");


                SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
                SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
                String transEntryDay = isYearMode ? monthFormat.format(transEntry.getDate())
                        : dayFormat.format(transEntry.getDate());


                HandlerThread handlerThread = new HandlerThread(transEntryDay);
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper());
                handler.post(() -> {
                    Map<String, Double> categoriesByAmount = new HashMap<>();
                    Map<String, Map<String, Transaction>> transactionsByCategory = new HashMap<>();

                    if (isYearMode) {
                        statisticsViewModel.getAnalyticsStoredStatisticsDoc()
                                .getTransactions()
                                .values()
                                .forEach(transaction -> {
                                    if (!monthFormat.format(transaction.getDate()).equals(transEntryDay)) {
                                        return;
                                    }

                                    if (categoriesByAmount.containsKey(transaction.getCategory())) {
                                        Double oldVal = categoriesByAmount.get(transaction.getCategory());
                                        if (oldVal != null) {
                                            categoriesByAmount.replace(transaction.getCategory(), oldVal + transaction.getAmount());
                                        }
                                    } else {
                                        categoriesByAmount.put(transaction.getCategory(), transaction.getAmount());
                                    }

                                    if (transactionsByCategory.containsKey(transaction.getCategory())) {
                                        if (transactionsByCategory.get(transaction.getCategory()) == null) {
                                            return;
                                        }
                                        Objects.requireNonNull(transactionsByCategory.get(transaction.getCategory()))
                                                .put(transaction.getId(), transaction);
                                    } else {
                                        Map<String, Transaction> transactionMap = new HashMap<>();
                                        transactionMap.put(transaction.getId(), transaction);
                                        transactionsByCategory.put(transaction.getCategory(), transactionMap);
                                    }
                                });

                    } else {
                        if (statisticsViewModel.getAnalyticsStoredStatisticsDoc()
                                .getTransactionsByDay() == null
                                || statisticsViewModel.getAnalyticsStoredStatisticsDoc()
                                .getTransactionsByDay().get(transEntryDay)
                                == null) {
                            return;
                        }
                        Objects.requireNonNull(statisticsViewModel.getAnalyticsStoredStatisticsDoc()
                                        .getTransactionsByDay()
                                        .get(transEntryDay))
                                .values().forEach(transaction -> {

                                    if (categoriesByAmount.containsKey(transaction.getCategory())) {
                                        Double oldVal = categoriesByAmount.get(transaction.getCategory());
                                        if (oldVal != null) {
                                            categoriesByAmount.replace(transaction.getCategory(), oldVal + transaction.getAmount());
                                        }
                                    } else {
                                        categoriesByAmount.put(transaction.getCategory(), transaction.getAmount());
                                    }
                                    if (transactionsByCategory.containsKey(transaction.getCategory())) {
                                        if (transactionsByCategory.get(transaction.getCategory()) == null) {
                                            return;
                                        }
                                        Objects.requireNonNull(transactionsByCategory.get(transaction.getCategory()))
                                                .put(transaction.getId(), transaction);
                                    } else {
                                        Map<String, Transaction> transactionMap = new HashMap<>();
                                        transactionMap.put(transaction.getId(), transaction);
                                        transactionsByCategory.put(transaction.getCategory(), transactionMap);
                                    }

                                });
                    }

                    requireActivity().runOnUiThread(() -> {
                        resetCategoriesRv();
                        populateCategoriesRv(categoriesByAmount, transactionsByCategory);
                        binding.categoriesRv.scheduleLayoutAnimation();
                    });
                });

            }

            @Override
            public void onNothingSelected() {
                resetCategoriesRv();
                populateCategoriesRv(statisticsViewModel.getAnalyticsStoredStatisticsDoc().getAmountByCategory(),
                        statisticsViewModel.getAnalyticsStoredStatisticsDoc().getCategories());

                binding.categoriesRv.scheduleLayoutAnimation();
            }
        });

        binding.pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Log.d(TAG, "onValueSelected: ");
                HandlerThread handlerThread = new HandlerThread(((PieEntry) e).getLabel());
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper());
                handler.post(() -> {
                    List<Transaction> transactions = new ArrayList<>(Objects.requireNonNull(
                                    statisticsViewModel.getAnalyticsStoredStatisticsDoc()
                                            .getCategories().get(((PieEntry) e).getLabel()))
                            .values());
                    transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());
                    transactionList = transactions;

                    requireActivity().runOnUiThread(() -> {
                        binding.categoriesRv.setVisibility(GONE);
                        binding.transactionsRv.setVisibility(VISIBLE);

                        transactionsAdapter.setTransactionList(transactions);
//                        if (transactions.size() < 10) {
//                            transactionsAdapter.setTransactionList(transactions);
//                            loadedAll = true;
//                        } else {
//                            transactionsAdapter.setTransactionList(transactions.subList(0, 10));
//                            loadedAll = false;
//                        }
                        binding.transactionsRv.scheduleLayoutAnimation();
                        Log.d(TAG, "onValueSelected: thread done");
                    });
                });
                Log.d(TAG, "onValueSelected: finished");
            }

            @Override
            public void onNothingSelected() {
                binding.categoriesRv.setVisibility(VISIBLE);
                binding.transactionsRv.setVisibility(GONE);
                binding.categoriesRv.scheduleLayoutAnimation();
            }
        });
        binding.dateChip.setOnTouchListener(new OnSwipeTouchListener(requireContext()) {

            @Override
            public void onSwipeLeft() {
                if (monthYearFormat.format(selectedDate).equals(monthYearFormat.format(new Date()))) {
                    return;
                }
                handleSwipeOnDate(true);

                super.onSwipeLeft();
            }

            @Override
            public void onSwipeRight() {
                handleSwipeOnDate(false);
                super.onSwipeRight();
            }

        });
//        binding.transactionsRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (!isRecyclerScrollable(recyclerView)) {
//                    if (!loadedAll) {
//                        transactionsAdapter.setTransactionList(transactionList);
//                        loadedAll = true;
//                    }
//                }
//            }
//        });
    }

    private void handleSwipeOnDate(boolean increaseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        if (isYearMode) {
            calendar.set(Calendar.YEAR, increaseDate ? calendar.get(Calendar.YEAR) + 1 : calendar.get(Calendar.YEAR) - 1);
        } else {
            calendar.set(Calendar.MONTH, increaseDate ? calendar.get(Calendar.MONTH) + 1 : calendar.get(Calendar.MONTH) - 1);
        }
        selectedDate = calendar.getTime();

        binding.dateChip.setText(isYearMode ? yearFormat.format(selectedDate) : monthYearFormat.format(selectedDate));
        AnalyticsBarUtils.setBarLabelRotation(binding.barChart, true);
        resetCategoriesRv();
        loadMonthTransactions(selectedDate);
    }

    public boolean isRecyclerScrollable(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        TransactionsAdapter adapter = (TransactionsAdapter) recyclerView.getAdapter();
        if (layoutManager == null || adapter == null) return false;

        return layoutManager.findLastCompletelyVisibleItemPosition() < adapter.getItemCount() - 1;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetCategoriesRv() {
        categoryList = new ArrayList<>();
        expenseCategoryAdapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency(), this);
        binding.categoriesRv.setAdapter(expenseCategoryAdapter);
        expenseCategoryAdapter.notifyDataSetChanged();
    }

    private void toggleChartsVisibility() {
        if (binding.barChart.getHighlighted() != null) {
            binding.barChart.highlightValue(null);
            resetCategoriesRv();
            populateCategoriesRv(statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                    statisticsViewModel.getStoredStatisticsDoc().getCategories());
            binding.categoriesRv.scheduleLayoutAnimation();
        }
        if (binding.pieChart.getHighlighted() != null) {
            binding.pieChart.highlightValue(null);
            if (binding.categoriesRv.getVisibility() == GONE) {
                binding.categoriesRv.setVisibility(VISIBLE);
                binding.transactionsRv.setVisibility(GONE);
            }
        }

        if (binding.barChart.getVisibility() == View.VISIBLE) {
            binding.barChart.setVisibility(View.GONE);
            binding.pieChart.setVisibility(View.VISIBLE);
            binding.analyticsTypeImage
                    .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_bar_chart_24));
            binding.pieChart.animateXY(requireContext().getResources().getInteger(R.integer.transition_duration_millis),
                    requireContext().getResources().getInteger(R.integer.transition_duration_millis));
        } else {
            binding.barChart.setVisibility(View.VISIBLE);
            binding.pieChart.setVisibility(View.GONE);
            binding.analyticsTypeImage
                    .setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_pie_chart_24));
            binding.barChart.animateXY(requireContext().getResources().getInteger(R.integer.transition_duration_millis),
                    requireContext().getResources().getInteger(R.integer.transition_duration_millis));
        }
    }

    private void setUpExpenseCategoryRv() {
        expenseCategoryAdapter = new ExpenseCategoryAdapter(categoryList, wallet.getCurrency(), this);
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(expenseCategoryAdapter);
    }

    private void populateCategoriesRv(Map<String, Double> categories,
                                      Map<String, Map<String, Transaction>> transactionsByCategory) {

        categories = MapUtil.sortByValue(categories);

        categories.forEach((key, value) -> {
            if (key.equals("Income") || value == 0d
                    || !transactionsByCategory.containsKey(key)) {
                return;
            }
            List<Transaction> transactionList = new ArrayList<>(Objects.requireNonNull(transactionsByCategory.get(key)).values());
            transactionList.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());
            ExpenseCategory expenseCategory = new ExpenseCategory(key,
                    transactionList,
                    null,
                    value);
            if (CategoriesUtil.expenseCategoryList.contains(expenseCategory)) {
                expenseCategory.setResourceId(
                        CategoriesUtil.expenseCategoryList.get(
                                        CategoriesUtil.expenseCategoryList.indexOf(expenseCategory))
                                .getResourceId());
                if (!categoryList.contains(expenseCategory)) {
                    categoryList.add(expenseCategory);
                    expenseCategoryAdapter.notifyItemInserted(categoryList.size() - 1);
                }
            }
        });

    }

    private void getInitialTransactionData() {
        if (statisticsViewModel.getStoredStatisticsDoc() == null) {
            return;
        }

        statisticsViewModel.setAnalyticsStoredStatisticsDoc(statisticsViewModel.getStoredStatisticsDoc());

        populateCategoriesRv(statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                statisticsViewModel.getStoredStatisticsDoc().getCategories());
        PieChartUtils.updatePieChartData(binding.pieChart,
                wallet.getCurrency(),
                statisticsViewModel.getStoredStatisticsDoc().getAmountByCategory(),
                statisticsViewModel.getStoredStatisticsDoc().getTotalAmountSpent(),
                false);

        List<Transaction> transactionList = new ArrayList<>(statisticsViewModel.getStoredStatisticsDoc().getTransactions().values());
        transactionList.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());
        transEntryArrayList = getTransEntryArrayList(transactionList, isYearMode);

        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                transEntryArrayList,
                new TextView(requireContext()).getCurrentTextColor()
        );

        binding.totalAmountTv.setText(String.format("%s %s",
                wallet.getCurrency(),
                new DecimalFormat("0.00").format(statisticsViewModel.getStoredStatisticsDoc().getTotalAmountSpent())));
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
                    statisticsViewModel.setAnalyticsStoredStatisticsDoc(statisticsDoc);
                    if (statisticsDoc == null || statisticsDoc.getTransactions().isEmpty()) {
                        Log.e(TAG, "loadTransactions: empty");
                        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                                new ArrayList<>(),
                                new TextView(requireContext()).getCurrentTextColor());

                        PieChartUtils.updatePieChartData(binding.pieChart,
                                wallet.getCurrency(),
                                new HashMap<>(),
                                0d,
                                false);
                        binding.totalAmountTv.setText(R.string.no_data_provided);
                    } else {
                        if (binding.categoriesRv.getVisibility() == GONE) {
                            binding.pieChart.highlightValue(null);
                            binding.categoriesRv.setVisibility(VISIBLE);
                            binding.transactionsRv.setVisibility(GONE);
                        }
                        populateCategoriesRv(statisticsDoc.getAmountByCategory(), statisticsDoc.getCategories());

                        List<Transaction> transactions = new ArrayList<>(statisticsDoc.getTransactions().values());
                        transactions.sort(Comparator.comparingLong(Transaction::getDateLong).reversed());

                        transEntryArrayList = getTransEntryArrayList(transactions, isYearMode);

                        AnalyticsBarUtils.updateBarchartData(binding.barChart,
                                transEntryArrayList,
                                new TextView(requireContext()).getCurrentTextColor());
                        binding.barChart.animateXY(requireContext().getResources().getInteger(R.integer.transition_duration_millis),
                                requireContext().getResources().getInteger(R.integer.transition_duration_millis));

                        PieChartUtils.updatePieChartData(binding.pieChart,
                                wallet.getCurrency(),
                                statisticsDoc.getAmountByCategory(),
                                statisticsDoc.getTotalAmountSpent(),
                                false);
                        binding.pieChart.animateXY(requireContext().getResources().getInteger(R.integer.transition_duration_millis),
                                requireContext().getResources().getInteger(R.integer.transition_duration_millis));

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
        if (args.getBottomNavAction()) {
            MaterialSharedAxis enter = new MaterialSharedAxis(MaterialSharedAxis.X, true);
            enter.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            MaterialSharedAxis exit = new MaterialSharedAxis(MaterialSharedAxis.X, false);
            exit.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            setEnterTransition(enter);
            setReturnTransition(exit);
        } else {

            MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
            materialContainerTransform.setDuration(getResources().getInteger(R.integer.transition_duration_millis));
            materialContainerTransform.setEndShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(18));
            materialContainerTransform.setStartShapeAppearanceModel(new ShapeAppearanceModel().withCornerSize(48));
            setSharedElementEnterTransition(materialContainerTransform);
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
                                transaction, true));
    }
}