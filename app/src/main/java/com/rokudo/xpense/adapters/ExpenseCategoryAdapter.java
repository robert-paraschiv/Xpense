package com.rokudo.xpense.adapters;

import android.annotation.SuppressLint;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.utils.PieChartUtils;
import com.rokudo.xpense.utils.TransactionUtils;

import java.text.DecimalFormat;
import java.util.List;

public class ExpenseCategoryAdapter extends RecyclerView.Adapter<ExpenseCategoryAdapter.ViewHolder> {
    private final List<ExpenseCategory> categoryList;
    private final String currency;

    public ExpenseCategoryAdapter(List<ExpenseCategory> categoryList, String currency) {
        this.categoryList = categoryList;
        this.currency = currency;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView categoryPic;
        private final TextView categoryName;
        private final TextView categoryAmount;
        private final RecyclerView transactionsRv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.categoryAmount = itemView.findViewById(R.id.categoryAmount);
            this.categoryName = itemView.findViewById(R.id.categoryName);
            this.categoryPic = itemView.findViewById(R.id.categoryImage);
            this.transactionsRv = itemView.findViewById(R.id.transactionsRv);

            this.itemView.setOnClickListener(v -> {
                if (transactionsRv.getVisibility() == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(itemView.findViewById(R.id.mainCard), new ChangeBounds().setDuration(50));
                    transactionsRv.setVisibility(View.GONE);
                } else {
                    TransitionManager.beginDelayedTransition(itemView.findViewById(R.id.mainCard), new ChangeBounds().setDuration(500));
                    transactionsRv.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_category, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseCategory expenseCategory = categoryList.get(position);
        if (expenseCategory != null) {
            holder.categoryName.setText(expenseCategory.getName());
            holder.categoryAmount.setText(new DecimalFormat("0.00").format(expenseCategory.getAmount()) + " " + currency);
            holder.categoryPic.setImageDrawable(
                    AppCompatResources.getDrawable(holder.categoryPic.getContext(),
                            expenseCategory.getResourceId()));
            holder.categoryPic.setColorFilter(PieChartUtils.PIE_COLORS.get(position));
            if (expenseCategory.getTransactionList().isEmpty()) {
                holder.itemView.findViewById(R.id.mainCard).setClickable(false);
            } else {
                holder.itemView.findViewById(R.id.mainCard).setClickable(true);
                holder.transactionsRv.setLayoutManager(new LinearLayoutManager(holder.transactionsRv.getContext()));
                holder.transactionsRv.setAdapter(new TransactionsAdapter(expenseCategory.getTransactionList(), true));
            }
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
