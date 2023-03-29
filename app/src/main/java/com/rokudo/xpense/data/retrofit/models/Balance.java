package com.rokudo.xpense.data.retrofit.models;

import java.util.Map;

public class Balance {
    private Map<String, String> balanceAmount;
    private String balanceType;
    private String referenceDay;
    private String lastChangeDateTime;

    public Map<String, String> getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(Map<String, String> balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(String balanceType) {
        this.balanceType = balanceType;
    }

    public String getReferenceDay() {
        return referenceDay;
    }

    public void setReferenceDay(String referenceDay) {
        this.referenceDay = referenceDay;
    }

    public String getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public void setLastChangeDateTime(String lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
    }

    @Override
    public String toString() {
        return "Balance{" +
                "balanceAmount=" + balanceAmount +
                ", balanceType='" + balanceType + '\'' +
                ", referenceDay='" + referenceDay + '\'' +
                ", lastChangeDateTime='" + lastChangeDateTime + '\'' +
                '}';
    }
}
