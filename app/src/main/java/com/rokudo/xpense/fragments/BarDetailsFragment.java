package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;

import android.os.Bundle;
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

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.transition.MaterialContainerTransform;
import com.rokudo.xpense.R;
import com.rokudo.xpense.adapters.ExpenseCategoryAdapter;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentBarDetailsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.BarChartUtils;
import com.rokudo.xpense.utils.BarDetailsUtils;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BarDetailsFragment extends Fragment {

    private FragmentBarDetailsBinding binding;
    private TransactionViewModel transactionViewModel;
    private ExpenseCategoryAdapter adapter;
    private Wallet mWallet;
    private final List<ExpenseCategory> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBarDetailsBinding.inflate(inflater, container, false);

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());

        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        BarDetailsUtils.setupBarChart(binding.barChart, new TextView(requireContext()).getCurrentTextColor());
        setUpExpenseCategoryRv();

        loadTransactions();

        return binding.getRoot();
    }

    private void setUpExpenseCategoryRv() {
        BarDetailsFragmentArgs args = BarDetailsFragmentArgs.fromBundle(requireArguments());
        mWallet = args.getWallet();
        adapter = new ExpenseCategoryAdapter(categoryList, mWallet.getCurrency());
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    private void loadTransactions() {
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

        transactionViewModel.loadTransactionsDateInterval(mWallet.getId(), start, end).observe(getViewLifecycleOwner(), values -> {
            Map<String, Double> categories = new HashMap<>();
            Map<String, List<Transaction>> transactionsByCategory = new HashMap<>();
            for (Transaction transaction : values) {
                if (transaction.getType().equals(Transaction.INCOME_TYPE))
                    continue;

                if (transactionsByCategory.containsKey(transaction.getCategory())) {
                    Objects.requireNonNull(transactionsByCategory.get(transaction.getCategory())).add(transaction);
                } else {
                    transactionsByCategory.put(transaction.getCategory(), new ArrayList<>(Collections.singleton(transaction)));
                }

                if (categories.containsKey(transaction.getCategory())) {
                    Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                    categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
                } else {
                    categories.put(transaction.getCategory(), transaction.getAmount());
                }
            }
            categories = MapUtil.sortByValue(categories);
            categories.forEach((key, value) -> {
                ExpenseCategory expenseCategory = new ExpenseCategory(key, transactionsByCategory.get(key), null, value);
                if (CategoriesUtil.categoryList.contains(expenseCategory)) {
                    expenseCategory.setResourceId(CategoriesUtil.categoryList.get(CategoriesUtil.categoryList.indexOf(expenseCategory)).getResourceId());
                    if (!categoryList.contains(expenseCategory)) {
                        categoryList.add(expenseCategory);
                        adapter.notifyItemInserted(categoryList.size() - 1);
                    }
                }
            });
            binding.barChart.setMaxVisibleValueCount(31);
            BarDetailsUtils.updateBarchartData(binding.barChart, values, new TextView(requireContext()).getCurrentTextColor());
        });
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