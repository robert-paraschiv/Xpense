package com.rokudo.xpense.utils;

import androidx.annotation.NonNull;

import com.rokudo.xpense.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;

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

}
