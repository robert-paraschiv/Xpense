package com.rokudo.xpense.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BAccount {
    String id;
    String owner_id;
    String EUA_id;
    String requisition_id;
    String institutionId;
    String bankName;
    String bankPic;
    List<String> accounts;
    List<String> walletIds;

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

    public  List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts( List<String> accounts) {
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
