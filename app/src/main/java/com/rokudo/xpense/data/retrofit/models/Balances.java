package com.rokudo.xpense.data.retrofit.models;

import java.util.Arrays;

public class Balances {
    Balance[] balances;

    public Balance[] getBalances() {
        return balances;
    }

    public void setBalances(Balance[] balances) {
        this.balances = balances;
    }

    @Override
    public String toString() {
        return "Balances{" +
                "balances=" + Arrays.toString(balances) +
                '}';
    }
}
