package com.rokudo.xpense.data.retrofit.models;

import java.util.Objects;

public class BankTransaction {
    private String transactionId;
    private String endToEndId;
    private String bookingDate;
    private String valueDate;
    private TransactionAmount transactionAmount;
    private String remittanceInformationUnstructured;
    private String proprietaryBankTransactionCode;
    private String internalTransactionId;

    public BankTransaction() {
    }

    public BankTransaction(String transactionId, String endToEndId, String bookingDate,
                           String valueDate, TransactionAmount transactionAmount,
                           String remittanceInformationUnstructured,
                           String proprietaryBankTransactionCode, String internalTransactionId) {
        this.transactionId = transactionId;
        this.endToEndId = endToEndId;
        this.bookingDate = bookingDate;
        this.valueDate = valueDate;
        this.transactionAmount = transactionAmount;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
        this.internalTransactionId = internalTransactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(TransactionAmount transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public String getProprietaryBankTransactionCode() {
        return proprietaryBankTransactionCode;
    }

    public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
        this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    }

    public String getInternalTransactionId() {
        return internalTransactionId;
    }

    public void setInternalTransactionId(String internalTransactionId) {
        this.internalTransactionId = internalTransactionId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    @Override
    public String toString() {
        return "BankTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", endToEndId='" + endToEndId + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", valueDate='" + valueDate + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", remittanceInformationUnstructured='" + remittanceInformationUnstructured + '\'' +
                ", proprietaryBankTransactionCode='" + proprietaryBankTransactionCode + '\'' +
                ", internalTransactionId='" + internalTransactionId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankTransaction that = (BankTransaction) o;
        return Objects.equals(transactionId, that.transactionId) && Objects.equals(internalTransactionId, that.internalTransactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, internalTransactionId);
    }
}


