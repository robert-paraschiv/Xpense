package com.rokudo.xpense.models;

import java.util.Objects;

public class TransEntry {
    String date;
    Float amount;

    public TransEntry(String date, Float amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransEntry TransEntry = (TransEntry) o;
        return date.equals(TransEntry.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}

