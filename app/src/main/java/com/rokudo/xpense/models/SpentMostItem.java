package com.rokudo.xpense.models;

public class SpentMostItem {
    private String title;
    private String category;
    private String amount;
    private String date;

    public SpentMostItem() {
    }

    public SpentMostItem(String title, String category, String amount, String date) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
