package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

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
import com.rokudo.xpense.adapters.TransactionsAdapter;
import com.rokudo.xpense.data.viewmodels.TransactionViewModel;
import com.rokudo.xpense.databinding.FragmentPieDetailsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.PieChartUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieDetailsFragment extends Fragment {
    private FragmentPieDetailsBinding binding;
    private ExpenseCategoryAdapter adapter;
    private List<ExpenseCategory> categoryList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPieDetailsBinding.inflate(inflater, container, false);

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());

        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor());
        loadTransactions();
        setUpExpenseCategoryRv();
        return binding.getRoot();
    }

    private void setUpExpenseCategoryRv() {
        PieDetailsFragmentArgs args = PieDetailsFragmentArgs.fromBundle(requireArguments());
        adapter = new ExpenseCategoryAdapter(categoryList, args.getWallet().getCurrency());
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    private void loadTransactions() {
        TransactionViewModel transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        transactionViewModel.loadTransactions().observe(getViewLifecycleOwner(), values -> {
            Map<String, Double> categories = new HashMap<>();
            for (Transaction transaction : values) {
                if (transaction.getType().equals(Transaction.INCOME_TYPE))
                    continue;
                if (categories.containsKey(transaction.getCategory())) {
                    Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                    categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
                } else {
                    categories.put(transaction.getCategory(), transaction.getAmount());
                }
            }
            categories = MapUtil.sortByValue(categories);
            categories.forEach((key, value) -> {
                ExpenseCategory expenseCategory = new ExpenseCategory(key, null, value);
                if (CategoriesUtil.categoryList.contains(expenseCategory)) {
                    expenseCategory.setResourceId(CategoriesUtil.categoryList.get(CategoriesUtil.categoryList.indexOf(expenseCategory)).getResourceId());
                    categoryList.add(expenseCategory);
                    adapter.notifyItemInserted(categoryList.size() - 1);
                }
            });


            PieChartUtils.updatePieChartData(binding.pieChart, "", values, false);
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
