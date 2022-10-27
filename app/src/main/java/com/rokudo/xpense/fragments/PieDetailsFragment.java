package com.rokudo.xpense.fragments;

import static androidx.recyclerview.widget.RecyclerView.VERTICAL;
import static com.rokudo.xpense.utils.PieChartUtils.setupPieChart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.rokudo.xpense.databinding.FragmentPieDetailsBinding;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.MapUtil;
import com.rokudo.xpense.utils.PieChartUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PieDetailsFragment extends Fragment {
    private FragmentPieDetailsBinding binding;
    private ExpenseCategoryAdapter adapter;
    private final List<ExpenseCategory> categoryList = new ArrayList<>();
    private Wallet mWallet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPieDetailsBinding.inflate(inflater, container, false);

        binding.backBtn.setOnClickListener(view -> Navigation.findNavController(binding.backBtn).popBackStack());

        setupPieChart(binding.pieChart, new TextView(requireContext()).getCurrentTextColor(), false);
        setUpExpenseCategoryRv();
        initDateChip();

        loadTransactions();

        return binding.getRoot();
    }

    @SuppressLint("SimpleDateFormat")
    private void initDateChip() {
        binding.dateChip.setText(new SimpleDateFormat("MMMM yyyy").format(new Date()));
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

    private void setUpExpenseCategoryRv() {
        PieDetailsFragmentArgs args = PieDetailsFragmentArgs.fromBundle(requireArguments());
        mWallet = args.getWallet();
        adapter = new ExpenseCategoryAdapter(categoryList, mWallet.getCurrency());
        binding.categoriesRv.setLayoutManager(new LinearLayoutManager(requireContext(), VERTICAL, false));
        binding.categoriesRv.setAdapter(adapter);
    }

    private void loadTransactions() {
        TransactionViewModel transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
        transactionViewModel.loadTransactions().observe(getViewLifecycleOwner(), values -> {
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
                if (CategoriesUtil.expenseCategoryList.contains(expenseCategory)) {
                    expenseCategory.setResourceId(CategoriesUtil.expenseCategoryList.get(CategoriesUtil.expenseCategoryList.indexOf(expenseCategory)).getResourceId());
                    categoryList.add(expenseCategory);
                    adapter.notifyItemInserted(categoryList.size() - 1);
                }
            });


            PieChartUtils.updatePieChartData(binding.pieChart, mWallet.getCurrency(),
                    values, false);
        });
    }

}
