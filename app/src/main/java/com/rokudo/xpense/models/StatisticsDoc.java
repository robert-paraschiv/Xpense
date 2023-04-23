package com.rokudo.xpense.models;

import java.util.Map;

public class StatisticsDoc {
    Double totalAmountSpent;
    Map<String, Double> amountByCategory;
    Map<String, Map<String, Transaction>> categories;
    Map<String, Transaction> transactions;
    Map<String, Map<String, Transaction>> transactionsByDay;

    public Double getTotalAmountSpent() {
        return totalAmountSpent;
    }

    public void setTotalAmountSpent(Double totalAmountSpent) {
        this.totalAmountSpent = totalAmountSpent;
    }

    public Map<String, Double> getAmountByCategory() {
        return amountByCategory;
    }

    public void setAmountByCategory(Map<String, Double> amountByCategory) {
        this.amountByCategory = amountByCategory;
    }

    public Map<String, Map<String, Transaction>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Map<String, Transaction>> categories) {
        this.categories = categories;
    }

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, Transaction> transactions) {
        this.transactions = transactions;
    }

    public Map<String, Map<String, Transaction>> getTransactionsByDay() {
        return transactionsByDay;
    }

    public void setTransactionsByDay(Map<String, Map<String, Transaction>> transactionsByDay) {
        this.transactionsByDay = transactionsByDay;
    }
}
