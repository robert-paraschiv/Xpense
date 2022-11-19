package com.rokudo.xpense.data.retrofit.models;

public class AccountDetails {
    private Account account;
    private String account_id;

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public AccountDetails(Account account) {
        this.account = account;
    }

    public AccountDetails() {
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}
