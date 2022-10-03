package com.rokudo.xpense.models;

import java.util.Date;
import java.util.Objects;

public class TransEntry {
    String day;
    Date date;
    Float amount;

    public TransEntry(String day, Date date, Float amount) {
        this.day = day;
        this.date = date;
        this.amount = amount;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransEntry TransEntry = (TransEntry) o;
        return day.equals(TransEntry.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day);
    }
}

