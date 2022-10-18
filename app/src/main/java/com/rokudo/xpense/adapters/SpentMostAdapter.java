package com.rokudo.xpense.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.SpentMostItem;

import java.util.List;

public class SpentMostAdapter extends RecyclerView.Adapter<SpentMostAdapter.ViewHolder> {
    private List<SpentMostItem> spentMostItems;

    public SpentMostAdapter(List<SpentMostItem> spentMostItems) {
        this.spentMostItems = spentMostItems;
    }

    public void setItems(List<SpentMostItem> spentMostItemList) {
        this.spentMostItems = spentMostItemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, amount, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.category = itemView.findViewById(R.id.category);
            this.amount = itemView.findViewById(R.id.amount);
            this.date = itemView.findViewById(R.id.date);
        }
    }

    @NonNull
    @Override
    public SpentMostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spent_most, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpentMostAdapter.ViewHolder holder, int position) {
        SpentMostItem item = spentMostItems.get(position);
        if (Strings.isNullOrEmpty(item.getTitle())) {
            holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(item.getTitle());
        }
        if (Strings.isNullOrEmpty(item.getAmount())) {
            holder.amount.setVisibility(View.GONE);
        } else {
            holder.amount.setVisibility(View.VISIBLE);
            holder.amount.setText(item.getAmount());
        }
        if (Strings.isNullOrEmpty(item.getDate())) {
            holder.date.setVisibility(View.GONE);
        } else {
            holder.date.setVisibility(View.VISIBLE);
            holder.date.setText(item.getDate());
        }
        if (Strings.isNullOrEmpty(item.getCategory())) {
            holder.category.setVisibility(View.GONE);
        } else {
            holder.category.setVisibility(View.VISIBLE);
            holder.category.setText(item.getCategory());
        }
    }

    @Override
    public int getItemCount() {
        return spentMostItems.size();
    }

}
