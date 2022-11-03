package com.rokudo.xpense.data.retrofit.models;

import com.google.gson.annotations.SerializedName;

public class Institution {
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("bic")
    private String bic;
    @SerializedName("transaction_total_days")
    private Integer transaction_total_days;
    @SerializedName("countries")
    private String[] countries;
    @SerializedName("logo")
    private String logo;
    @SerializedName("payments")
    private Boolean payments;

    public Institution(String id, String name, String bic, Integer transaction_total_days,
                       String[] countries, String logo, Boolean payments) {
        this.id = id;
        this.name = name;
        this.bic = bic;
        this.transaction_total_days = transaction_total_days;
        this.countries = countries;
        this.logo = logo;
        this.payments = payments;
    }

    public Institution() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public Integer getTransaction_total_days() {
        return transaction_total_days;
    }

    public void setTransaction_total_days(Integer transaction_total_days) {
        this.transaction_total_days = transaction_total_days;
    }

    public String[] getCountries() {
        return countries;
    }

    public void setCountries(String[] countries) {
        this.countries = countries;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Boolean getPayments() {
        return payments;
    }

    public void setPayments(Boolean payments) {
        this.payments = payments;
    }
}
