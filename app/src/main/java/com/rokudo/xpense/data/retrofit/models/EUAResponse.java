package com.rokudo.xpense.data.retrofit.models;

import java.util.List;

public class EUAResponse {
    private Integer count;
    private String next;
    private String previous;
    private List<EndUserAgreement> results;

    public EUAResponse(Integer count, String next, String previous,
                       List<EndUserAgreement> results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    public EUAResponse() {
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<EndUserAgreement> getResults() {
        return results;
    }

    public void setResults(List<EndUserAgreement> results) {
        this.results = results;
    }
}
