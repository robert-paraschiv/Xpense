package com.rokudo.xpense.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BAccount implements Serializable {
    private String id;
    private String owner_id;
    private String EUA_id;
    private String requisition_id;
    private String institutionId;
    private String bankName;
    private String bankPic;
    private List<String> accounts;
    private List<String> walletIds;
    private String linked_acc_id;
    private String linked_acc_iban;
    private String linked_acc_currency;
    private Date EUA_EndDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getEUA_id() {
        return EUA_id;
    }

    public void setEUA_id(String EUA_id) {
        this.EUA_id = EUA_id;
    }

    public String getRequisition_id() {
        return requisition_id;
    }

    public void setRequisition_id(String requisition_id) {
        this.requisition_id = requisition_id;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public List<String> getWalletIds() {
        return walletIds;
    }

    public void setWalletIds(List<String> walletIds) {
        this.walletIds = walletIds;
    }

    public String getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(String institutionId) {
        this.institutionId = institutionId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankPic() {
        return bankPic;
    }

    public void setBankPic(String bankPic) {
        this.bankPic = bankPic;
    }

    public String getLinked_acc_id() {
        return linked_acc_id;
    }

    public void setLinked_acc_id(String linked_acc_id) {
        this.linked_acc_id = linked_acc_id;
    }

    public String getLinked_acc_iban() {
        return linked_acc_iban;
    }

    public void setLinked_acc_iban(String linked_acc_iban) {
        this.linked_acc_iban = linked_acc_iban;
    }

    public String getLinked_acc_currency() {
        return linked_acc_currency;
    }

    public void setLinked_acc_currency(String linked_acc_currency) {
        this.linked_acc_currency = linked_acc_currency;
    }

    public Date getEUA_EndDate() {
        return EUA_EndDate;
    }

    public void setEUA_EndDate(Date EUA_EndDate) {
        this.EUA_EndDate = EUA_EndDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "BAccount{" +
                "id='" + id + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", EUA_id='" + EUA_id + '\'' +
                ", requisition_id='" + requisition_id + '\'' +
                ", institutionId='" + institutionId + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankPic='" + bankPic + '\'' +
                ", accounts=" + accounts +
                ", walletIds=" + walletIds +
                ", linked_acc_id='" + linked_acc_id + '\'' +
                ", linked_acc_iban='" + linked_acc_iban + '\'' +
                ", linked_acc_currency='" + linked_acc_currency + '\'' +
                ", EUA_EndDate=" + EUA_EndDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BAccount that = (BAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
