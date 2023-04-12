package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.BarChartUtils.setupBarChart;
import static com.rokudo.xpense.utils.BarDetailsUtils.setupBarChart;
import static com.rokudo.xpense.utils.DateUtils.monthYearFormat;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ExpenseCategoryAdapter;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentAnalyticsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.BarChartUtils;
import com.rokudo.xpense.utils.BarDetailsUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.PieChartUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnalyticsFragment extends Fragment {

    FragmentAnalyticsBinding binding;
    TransactionViewModel transactionViewModel;
    private Double sum = 0.0;

    Wallet wallet;

    private ExpenseCategoryAdapter adapter;
    private List<ExpenseCategory> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalyticsBinding.inflate(inflater);


        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        initDateChip();
        getArgsPassed();
        setUpExpenseCategoryRv();
        initOnClicks();

        getInitialTransactionData();
        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), false);
        setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());


        return binding.getRoot();
    }

    private void initOnClicks() {
        binding.analyticsTypeImage.setOnClickListener(v -> {
            toggleChartsVisibility();
        });
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

        binding.totalAmountTv.setText(wallet.getCurrency() + " " + new DecimalFormat("0.00").format(sum));
    }

    private void getInitialTransactionData() {
        sortTransactions(transactionViewModel.getStoredTransactionList());
        PieChartUtils.updatePieChartData(binding.pieChart,
                wallet.getCurrency(),
                transactionViewModel.getStoredTransactionList(),
                false);
        BarDetailsUtils.updateBarchartData(binding.barChart,
                transactionViewModel.getStoredTransactionList(),
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