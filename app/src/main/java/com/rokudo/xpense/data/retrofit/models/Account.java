package com.rokudo.xpense.data.retrofit.models;

public class Account {
    private String resourceId;
    private String iban;
    private String currency;
    private String ownerName;
    private String name;
    private String product;
    private String cashAccountType;

    public Account(String resourceId, String iban, String currency,
                   String ownerName, String name, String product, String cashAccountType) {
        this.resourceId = resourceId;
        this.iban = iban;
        this.currency = currency;
        this.ownerName = ownerName;
        this.name = name;
        this.product = product;
        this.cashAccountType = cashAccountType;
    }

    public Account() {
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCashAccountType() {
        return cashAccountType;
    }

    public void setCashAccountType(String cashAccountType) {
        this.cashAccountType = cashAccountType;
    }
}
