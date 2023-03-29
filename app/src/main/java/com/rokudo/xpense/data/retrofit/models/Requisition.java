package com.rokudo.xpense.data.retrofit.models;

public class Requisition {
    private String id;
    private String created;
    private String redirect;
    private String status;
    private String institution_id;
    private String agreement;
    private String reference;
    private String[] accounts;
    private String user_language;
    private String link;
    private String ssn;
    private Boolean account_selection;
    private Boolean redirect_immediate;

    public Requisition(String id, String created, String redirect, String status,
                       String institution_id, String agreement, String reference,
                       String[] accounts, String user_language, String link, String ssn,
                       Boolean account_selection, Boolean redirect_immediate) {
        this.id = id;
        this.created = created;
        this.redirect = redirect;
        this.status = status;
        this.institution_id = institution_id;
        this.agreement = agreement;
        this.reference = reference;
        this.accounts = accounts;
        this.user_language = user_language;
        this.link = link;
        this.ssn = ssn;
        this.account_selection = account_selection;
        this.redirect_immediate = redirect_immediate;
    }

    public Requisition() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstitution_id() {
        return institution_id;
    }

    public void setInstitution_id(String institution_id) {
        this.institution_id = institution_id;
    }

    public String getAgreement() {
        return agreement;
    }

    public void setAgreement(String agreement) {
        this.agreement = agreement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String[] getAccounts() {
        return accounts;
    }

    public void setAccounts(String[] accounts) {
        this.accounts = accounts;
    }

    public String getUser_language() {
        return user_language;
    }

    public void setUser_language(String user_language) {
        this.user_language = user_language;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public Boolean getAccount_selection() {
        return account_selection;
    }

    public void setAccount_selection(Boolean account_selection) {
        this.account_selection = account_selection;
    }

    public Boolean getRedirect_immediate() {
        return redirect_immediate;
    }

    public void setRedirect_immediate(Boolean redirect_immediate) {
        this.redirect_immediate = redirect_immediate;
    }
}
