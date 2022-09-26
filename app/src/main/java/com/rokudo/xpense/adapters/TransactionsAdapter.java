package com.rokudo.xpense.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    private static final String TAG = "TransactionsAdapter";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM d, HH:mm");
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        if (transaction != null) {
            holder.transactionAmount.setText(transaction.getAmount().toString());
            holder.transactionPersonName.setText(transaction.getUserName());
            if (transaction.getType().equals("Income")) {
                holder.transactionAmount.setTextColor(holder.transactionAmount.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.transactionAmount.setTextColor(holder.transactionAmount.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }
            holder.transactionDate.setText(simpleDateFormat.format(transaction.getDate()));
            holder.transactionCategory.setText(transaction.getCategory());

            Glide.with(holder.transactionImage)
                    .load(transaction.getPicUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(DialogUtils.getCircularProgressDrawable(holder.transactionImage.getContext()))
                    .fallback(R.drawable.ic_baseline_person_24)
                    .error(R.drawable.ic_baseline_person_24)
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
        final TextView transactionPersonName;
        final TextView transactionAmount;
        final TextView transactionDate;
        final TextView transactionCategory;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionImage = itemView.findViewById(R.id.transactionImage);
            transactionPersonName = itemView.findViewById(R.id.transactionPerson);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
        }
    }
}
