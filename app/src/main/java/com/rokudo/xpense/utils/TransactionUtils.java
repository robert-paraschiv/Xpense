package com.rokudo.xpense.utils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.rokudo.xpense.utils.dialogs.DialogUtils.getCircularProgressDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.rokudo.xpense.R;
import com.rokudo.xpense.databinding.FragmentHomeBinding;
import com.rokudo.xpense.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class TransactionUtils {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM d, HH:mm");
    static SimpleDateFormat todayDateFormat = new SimpleDateFormat("HH:mm");
    static SimpleDateFormat checkDateFormat = new SimpleDateFormat("dd MMMM yyyy");

    @NonNull
    public static String getTransactionDateString(Transaction transaction) {
        return isTransactionDoneToday(transaction) ?
                "Today, " + todayDateFormat.format(transaction.getDate())
                : simpleDateFormat.format(transaction.getDate());
    }

    private static boolean isTransactionDoneToday(Transaction transaction) {
        return checkDateFormat.format(new Date()).equals(checkDateFormat.format(transaction.getDate()));
    }

    @SuppressLint("SetTextI18n")
    public static void updateLatestTransactionUI(Transaction transaction, FragmentHomeBinding binding, Context context) {
        if (transaction.getId() == null) {
            binding.lastTransactionLayout.setVisibility(View.GONE);
        } else {
            binding.lastTransactionLayout.setVisibility(View.VISIBLE);
            String transAmountPrefix;
            if (transaction.getType().equals("Income")) {
                binding.latestTransactionItem.transactionAmount
                        .setTextColor(context.getResources().getColor(android.R.color.holo_green_dark, context.getTheme()));
                transAmountPrefix = "+ ";
            } else {
                transAmountPrefix = "- ";
                binding.latestTransactionItem.transactionAmount
                        .setTextColor(context.getResources().getColor(android.R.color.holo_red_dark, context.getTheme()));
            }
            binding.latestTransactionItem.transactionAmount.setText(transAmountPrefix + transaction.getAmount().toString());
            binding.latestTransactionItem.transactionCategory.setText(transaction.getCategory());
            binding.latestTransactionItem.transactionDate.setText(TransactionUtils.getTransactionDateString(transaction));
            binding.latestTransactionItem.transactionPerson.setText(transaction.getUserName());
            CircularProgressDrawable circularProgressDrawable = getCircularProgressDrawable(context);
            Glide.with(context)
                    .load(transaction.getPicUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .fallback(R.drawable.ic_baseline_person_24)
                    .transition(withCrossFade())
                    .into(binding.latestTransactionItem.transactionImage);
        }
    }

}
