package com.rokudo.xpense.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    private static final String TAG = "TransactionsAdapter";

    List<Transaction> transactionList = new ArrayList<>();

    public TransactionsAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        if (transaction != null) {
            holder.transactionAmount.setText(transaction.getAmount().toString());
            holder.transactionDate.setText(transaction.getDate().toString());
            holder.transactionCategory.setText(transaction.getCategory());

            Glide.with(holder.transactionImage)
                    .load(transaction.getPicUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(holder.transactionImage.getContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(holder.transactionImage);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView transactionImage;
        final TextView transactionAmount;
        final TextView transactionDate;
        final TextView transactionCategory;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionImage = itemView.findViewById(R.id.transactionImage);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
        }
    }
}
