package com.rokudo.xpense.data.retrofit.models;

import com.google.gson.annotations.SerializedName;

public class Token {
    @SerializedName("access")
    private String access;
    @SerializedName("access_expires")
    private Integer access_expires;
    @SerializedName("refresh")
    private String refresh;
    @SerializedName("refresh_expires")
    private Integer refresh_expires;

    public Token(String access, Integer access_expires, String refresh, Integer refresh_expires) {
        this.access = access;
        this.access_expires = access_expires;
        this.refresh = refresh;
        this.refresh_expires = refresh_expires;
    }

    public Token() {
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public Integer getAccess_expires() {
        return access_expires;
    }

    public void setAccess_expires(Integer access_expires) {
        this.access_expires = access_expires;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public Integer getRefresh_expires() {
        return refresh_expires;
    }

    public void setRefresh_expires(Integer refresh_expires) {
        this.refresh_expires = refresh_expires;
    }
}
