package com.rokudo.xpense.models;

import java.util.Objects;

public class ExpenseCategory {
    String name;
    Integer resourceId;
    Double amount;

    public ExpenseCategory(String name, Integer resourceId) {
        this.name = name;
        this.resourceId = resourceId;
        this.amount = 0.0;
    }

    public ExpenseCategory(String name, Integer resourceId, Double amount) {
        this.name = name;
        this.resourceId = resourceId;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
