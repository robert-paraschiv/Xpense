package com.rokudo.xpense.models;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Wallet implements Serializable {
    private String id;
    private String title;
    private String creator_id;
    private Double amount;
    private String currency;
    private Date creation_date;
    private List<String> users;
    private List<WalletUser> walletUsers;
    private BAccount bAccount;

    public Wallet() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public Double getAmount() {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return Double.parseDouble(decimalFormat.format(amount));
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<WalletUser> getWalletUsers() {
        return walletUsers;
    }

    public void setWalletUsers(List<WalletUser> walletUsers) {
        this.walletUsers = walletUsers;
    }

    public BAccount getbAccount() {
        return bAccount;
    }

    public void setbAccount(BAccount bAccount) {
        this.bAccount = bAccount;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", creation_date=" + creation_date +
                ", users=" + users +
                ", walletUsers=" + walletUsers +
                ", bAccount=" + bAccount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return id.equals(wallet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
