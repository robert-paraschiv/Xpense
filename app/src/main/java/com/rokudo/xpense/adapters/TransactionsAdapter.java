package com.rokudo.xpense.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.TransactionUtils.getTransactionDateString;

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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rokudo.xpense.R;
import com.rokudo.xpense.models.ExpenseCategory;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.utils.CategoriesUtil;
import com.rokudo.xpense.utils.dialogs.DialogUtils;

import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    private final List<Transaction> transactionList;
    private final OnTransClickListener onTransactionClickListener;
    private final Boolean smallLayout;

    public TransactionsAdapter(List<Transaction> transactionList, Boolean smallLayout,
                               OnTransClickListener onTransactionClickListener) {
        this.transactionList = transactionList;
        this.smallLayout = smallLayout;
        this.onTransactionClickListener = onTransactionClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView transactionImage;
        final ImageView transactionCategoryImage;
        final TextView transactionPersonName;
        final TextView transactionAmount;
        final TextView transactionDate;
        final TextView transactionCategory;
        final TextView transactionTitle;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionImage = itemView.findViewById(R.id.transactionImage);
            transactionPersonName = itemView.findViewById(R.id.transactionPerson);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
            transactionCategoryImage = itemView.findViewById(R.id.transactionCategoryImage);
            transactionTitle = itemView.findViewById(R.id.transactionTitle);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (onTransactionClickListener != null) {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionClickListener.onClick(transactionList.get(position));
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(smallLayout ? R.layout.item_transaction_small : R.layout.item_transaction,
                        parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        if (transaction != null) {
            holder.transactionPersonName.setText(transaction.getUserName());
            String transAmountPrefix = "";
            if (transaction.getType() != null) {

                if (transaction.getType().equals("Income")) {
                    transAmountPrefix = "+ ";
                    holder.transactionAmount.setTextColor(holder.transactionAmount.getContext().getResources().getColor(android.R.color.holo_green_dark
                            , holder.transactionAmount.getContext().getTheme()));
                } else {
                    transAmountPrefix = "- ";
                    holder.transactionAmount.setTextColor(holder.transactionAmount.getContext().getResources().getColor(android.R.color.holo_red_dark
                            , holder.transactionAmount.getContext().getTheme()));
                }
            } else {
                if (transaction.getAmount() != null) {
                    if (transaction.getAmount() < 0) {
                        holder.transactionAmount
                                .setTextColor(holder.transactionAmount.getContext().getResources()
                                        .getColor(android.R.color.holo_red_dark
                                                , holder.transactionAmount.getContext().getTheme()));
                    } else {
                        transAmountPrefix = "+ ";
                        holder.transactionAmount.setTextColor(holder.transactionAmount.getContext()
                                .getResources().getColor(android.R.color.holo_green_dark
                                        , holder.transactionAmount.getContext().getTheme()));
                    }
                }
            }
            if (transaction.getAmount() != null)
                holder.transactionAmount.setText(transAmountPrefix + transaction.getAmount().toString()
                        + " " + transaction.getCurrency());
            if (transaction.getTitle() != null)
                holder.transactionTitle.setText(transaction.getTitle());
            if (transaction.getDate() != null)
                holder.transactionDate.setText(getTransactionDateString(transaction));
            if (transaction.getCategory() != null) {
                holder.transactionCategory.setText(transaction.getCategory());
                ExpenseCategory expenseCategory = new ExpenseCategory(transaction.getCategory(), 0, 0);
                int i = CategoriesUtil.expenseCategoryList.indexOf(expenseCategory);
                if (i >= 0) {
                    holder.transactionCategoryImage.setImageDrawable(
                            AppCompatResources.getDrawable(holder.transactionCategoryImage.getContext(),
                                    CategoriesUtil.expenseCategoryList.get(i).getResourceId()));
                    Integer color = CategoriesUtil.expenseCategoryList.get(i).getColor();
                    holder.transactionCategoryImage.setColorFilter(color);
                }
            }

            holder.transactionImage.setVisibility(transaction.getPicUrl() == null ? View.GONE : View.VISIBLE);

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
}
