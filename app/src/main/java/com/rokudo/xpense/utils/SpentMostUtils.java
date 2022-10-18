package com.rokudo.xpense.utils;

import android.annotation.SuppressLint;

import com.rokudo.xpense.adapters.SpentMostAdapter;
import com.rokudo.xpense.models.SpentMostItem;
import com.rokudo.xpense.models.Transaction;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpentMostUtils {
    @SuppressLint("NotifyDataSetChanged")
    public static void updateSpentMostOn(List<Transaction> values, String currency, SpentMostAdapter adapter) {
        if (values == null || values.isEmpty()) return;

        Map<String, Double> categories = new HashMap<>();
        Map<LocalDate, Double> days = new HashMap<>();
        Transaction expensiveTransaction = null;
        for (Transaction transaction : values) {

            if (transaction.getType().equals(Transaction.INCOME_TYPE)) {
                continue;
            }

            if (expensiveTransaction == null) {
                expensiveTransaction = transaction;
            } else if (expensiveTransaction.getAmount() < transaction.getAmount()) {
                expensiveTransaction = transaction;
            }

            LocalDate transactionDate = transaction.getDate().toInstant().atZone(ZoneOffset.UTC).toLocalDate();

            if (days.containsKey(transactionDate)) {
                Double amount = days.getOrDefault(transactionDate, 0.0);
                days.put(transactionDate, amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                days.put(transactionDate, transaction.getAmount());
            }

            if (categories.containsKey(transaction.getCategory())) {
                Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                categories.put(transaction.getCategory(), transaction.getAmount());
            }
        }

        SpentMostItem mostExpensiveTransaction = new SpentMostItem(expensiveTransaction != null ? expensiveTransaction.getTitle() : "", expensiveTransaction != null ? expensiveTransaction.getCategory() : "", "- " + (expensiveTransaction != null ? new DecimalFormat("0.00").format(expensiveTransaction.getAmount()) : "") + " " + currency, expensiveTransaction != null ? TransactionUtils.getTransactionDateString(expensiveTransaction) : "");

        SpentMostItem mostExpensiveCategory = new SpentMostItem();
        mostExpensiveCategory.setTitle("Most Expensive category ");
        categories.forEach((key, value) -> {
            if (mostExpensiveCategory.getAmount() == null) {
                mostExpensiveCategory.setAmount(String.valueOf(value));
            } else if (Double.parseDouble(mostExpensiveCategory.getAmount()) < value) {
                mostExpensiveCategory.setCategory(key);
                mostExpensiveCategory.setAmount(value + "");
            }
        });
        mostExpensiveCategory.setAmount(new DecimalFormat("0.00").format(Double.parseDouble(mostExpensiveCategory.getAmount())) + " " + currency);

        SpentMostItem mostExpensiveDay = new SpentMostItem();
        mostExpensiveDay.setTitle("Most expensive day");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("E, MMM d");
        days.forEach((key, value) -> {
            if (mostExpensiveDay.getAmount() == null) {
                mostExpensiveDay.setAmount(String.valueOf(value));
                mostExpensiveDay.setDate(dateTimeFormatter.format(key));
            } else if (Double.parseDouble(mostExpensiveDay.getAmount()) < value) {
                mostExpensiveDay.setDate(dateTimeFormatter.format(key));
                mostExpensiveDay.setAmount(value + "");
            }
        });
        mostExpensiveDay.setAmount(new DecimalFormat("0.00").format(Double.parseDouble(mostExpensiveDay.getAmount())) + " " + currency);


        List<SpentMostItem> spentMostItemList = new ArrayList<>();
        spentMostItemList.add(mostExpensiveTransaction);
        spentMostItemList.add(mostExpensiveCategory);
        spentMostItemList.add(mostExpensiveDay);
        adapter.setItems(spentMostItemList);
        adapter.notifyDataSetChanged();
    }

}
