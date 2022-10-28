package com.rokudo.xpense.models;

import java.util.Date;
import java.util.Objects;

public class Invitation {
    private String id;
    private String creator_id;
    private String creator_name;
    private String wallet_title;
    private String invited_person_phone_number;
    private String creator_pic_url;
    private Date date;
    private String status;

    public Invitation() {
    }

    public Invitation(String creator_name, String wallet_title) {
        this.creator_name = creator_name;
        this.wallet_title = wallet_title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getCreator_name() {
        return creator_name;
    }

    public void setCreator_name(String creator_name) {
        this.creator_name = creator_name;
    }

    public String getWallet_title() {
        return wallet_title;
    }

    public void setWallet_title(String wallet_title) {
        this.wallet_title = wallet_title;
    }

    public String getInvited_person_phone_number() {
        return invited_person_phone_number;
    }

    public void setInvited_person_phone_number(String invited_person_phone_number) {
        this.invited_person_phone_number = invited_person_phone_number;
    }

    public String getCreator_pic_url() {
        return creator_pic_url;
    }

    public void setCreator_pic_url(String creator_pic_url) {
        this.creator_pic_url = creator_pic_url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invitation that = (Invitation) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "id='" + id + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", creator_name='" + creator_name + '\'' +
                ", wallet_title='" + wallet_title + '\'' +
                ", invited_person_phone_number='" + invited_person_phone_number + '\'' +
                ", creator_pic_url='" + creator_pic_url + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                '}';
    }
}
