package com.rokudo.xpense.data.retrofit.models;

public class TransactionsResponse {
    private Transactions transactions;

    public TransactionsResponse(Transactions transactions) {
        this.transactions = transactions;
    }

    public TransactionsResponse() {
    }

    public Transactions getTransactions() {
        return transactions;
    }

    public void setTransactions(Transactions transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "TransactionsResponse{" +
                "transactions=" + transactions +
                '}';
    }
}
