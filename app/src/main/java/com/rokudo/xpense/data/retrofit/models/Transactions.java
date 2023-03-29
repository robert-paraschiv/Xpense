package com.rokudo.xpense.data.retrofit.models;

import java.util.Arrays;

public class Transactions {
    private BankTransaction[] booked;
    private BankTransaction[] pending;

    public Transactions(BankTransaction[] booked, BankTransaction[] pending) {
        this.booked = booked;
        this.pending = pending;
    }

    public Transactions() {
    }

    public  BankTransaction[] getBooked() {
        return booked;
    }

    public void setBooked( BankTransaction[] booked) {
        this.booked = booked;
    }

    public BankTransaction[] getPending() {
        return pending;
    }

    public void setPending( BankTransaction[] pending) {
        this.pending = pending;
    }

    @Override
    public String toString() {
        return "Transactions{" +
                "booked=" + Arrays.toString(booked) +
                ", pending=" + Arrays.toString(pending) +
                '}';
    }
}
