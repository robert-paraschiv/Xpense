package com.rokudo.xpense.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.utils.PieChartUtils;

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.categoryAmount = itemView.findViewById(R.id.categoryAmount);
            this.categoryName = itemView.findViewById(R.id.categoryName);
            this.categoryPic = itemView.findViewById(R.id.categoryImage);
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
            holder.categoryAmount.setText(expenseCategory.getAmount().toString() + " " + currency);
            holder.categoryPic.setImageDrawable(
                    AppCompatResources.getDrawable(holder.categoryPic.getContext(),
                            expenseCategory.getResourceId()));
            holder.categoryPic.setColorFilter(PieChartUtils.PIE_COLORS.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
}
