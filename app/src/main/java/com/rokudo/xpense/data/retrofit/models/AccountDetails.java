package com.rokudo.xpense.data.retrofit.models;

public class AccountDetails {
    private Account account;

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
