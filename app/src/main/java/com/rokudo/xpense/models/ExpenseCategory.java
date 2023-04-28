package com.rokudo.xpense.models;

import java.util.List;
import java.util.Objects;

public class ExpenseCategory {
    String name;
    Integer resourceId;
    Double amount;
    List<Transaction> transactionList;
    Integer color;

    public ExpenseCategory(String name) {
        this.name = name;
    }

    public ExpenseCategory(String name, Integer resourceId, Integer color) {
        this.name = name;
        this.resourceId = resourceId;
        this.amount = 0.0;
        this.color = color;
    }

    public ExpenseCategory(String name, List<Transaction> transactionList, Integer resourceId, Double amount) {
        this.name = name;
        this.transactionList = transactionList;
        this.resourceId = resourceId;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseCategory that = (ExpenseCategory) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
