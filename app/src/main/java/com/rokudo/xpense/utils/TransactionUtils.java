package com.rokudo.xpense.utils;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.rokudo.xpense.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("SimpleDateFormat")
public class TransactionUtils {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM d");
    static SimpleDateFormat checkDateFormat = new SimpleDateFormat("dd MMMM yyyy");

    @NonNull
    public static String getTransactionDateString(Transaction transaction) {
        return isTransactionDoneToday(transaction) ?
                "Today" : simpleDateFormat.format(transaction.getDate());
    }

    private static boolean isTransactionDoneToday(Transaction transaction) {
        return checkDateFormat.format(new Date()).equals(checkDateFormat.format(transaction.getDate()));
    }

    public static boolean isTransactionDifferent(Transaction oldTransaction, Transaction newTransaction) {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        if (oldTransaction.getTitle() == null)
            return true;

        if (!oldTransaction.getTitle().equals(newTransaction.getTitle()))
            return true;

        if (!oldTransaction.getAmount().equals(newTransaction.getAmount()))
            return true;

        if (!oldTransaction.getType().equals(newTransaction.getType()))
            return true;

        if (!oldTransaction.getCategory().equals(newTransaction.getCategory()))
            return true;

        if (!simpleDateFormat1.format(oldTransaction.getDate()).equals(simpleDateFormat1.format(newTransaction.getDate())))
            return true;

        return false;
    }

    public static boolean isBankTransactionDifferent(Transaction oldTransaction, Transaction newTransaction) {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

        if (oldTransaction.getTitle() == null)
            return true;

        if (!oldTransaction.getTitle().equals(newTransaction.getTitle()))
            return true;

        if (!oldTransaction.getAmount().equals(newTransaction.getAmount()))
            return true;

        if (!simpleDateFormat1.format(oldTransaction.getDate()).equals(simpleDateFormat1.format(newTransaction.getDate())))
            return true;

        return false;
    }


}
