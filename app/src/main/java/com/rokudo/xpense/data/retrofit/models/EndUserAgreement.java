package com.rokudo.xpense.data.retrofit.models;

import com.google.gson.annotations.SerializedName;

public class EndUserAgreement {
    @SerializedName("id")
    private String id;
    @SerializedName("created")
    private String created;
    @SerializedName("max_historical_days")
    private Integer max_historical_days;
    @SerializedName("access_valid_for_days")
    private Integer access_valid_for_days;
    @SerializedName("access_scope")
    private String[] access_scope;
    @SerializedName("accepted")
    private String accepted;
    @SerializedName("institution_id")
    private String institution_id;

    public EndUserAgreement(String id, String created, Integer max_historical_days,
                            Integer access_valid_for_days, String[] access_scope, String accepted, String institution_id) {
        this.id = id;
        this.created = created;
        this.max_historical_days = max_historical_days;
        this.access_valid_for_days = access_valid_for_days;
        this.access_scope = access_scope;
        this.accepted = accepted;
        this.institution_id = institution_id;
    }

    public EndUserAgreement() {
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

    public Integer getMax_historical_days() {
        return max_historical_days;
    }

    public void setMax_historical_days(Integer max_historical_days) {
        this.max_historical_days = max_historical_days;
    }

    public Integer getAccess_valid_for_days() {
        return access_valid_for_days;
    }

    public void setAccess_valid_for_days(Integer access_valid_for_days) {
        this.access_valid_for_days = access_valid_for_days;
    }

    public String[] getAccess_scope() {
        return access_scope;
    }

    public void setAccess_scope(String[] access_scope) {
        this.access_scope = access_scope;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public String getInstitution_id() {
        return institution_id;
    }

    public void setInstitution_id(String institution_id) {
        this.institution_id = institution_id;
    }
}
