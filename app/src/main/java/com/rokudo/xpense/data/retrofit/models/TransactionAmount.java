package com.rokudo.xpense.data.retrofit.models;

public class TransactionAmount {
    private String currency;
    private Float amount;

    public TransactionAmount(String currency, Float amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "TransactionAmount{" +
                "currency='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }
}
