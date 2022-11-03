package com.rokudo.xpense.data.retrofit.models;

public class BankTransaction {
    private String transactionId;
    private String endToEndId;
    private String bookingDate;
    private TransactionAmount transactionAmount;
    private String remittanceInformationUnstructured;
    private String proprietaryBankTransactionCode;
    private String internalTransactionId;

    public BankTransaction() {
    }

    public BankTransaction(String transactionId, String endToEndId, String bookingDate,
                           TransactionAmount transactionAmount, String remittanceInformationUnstructured,
                           String proprietaryBankTransactionCode, String internalTransactionId) {
        this.transactionId = transactionId;
        this.endToEndId = endToEndId;
        this.bookingDate = bookingDate;
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

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", endToEndId='" + endToEndId + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", remittanceInformationUnstructured='" + remittanceInformationUnstructured + '\'' +
                ", proprietaryBankTransactionCode='" + proprietaryBankTransactionCode + '\'' +
                ", internalTransactionId='" + internalTransactionId + '\'' +
                '}';
    }
}


