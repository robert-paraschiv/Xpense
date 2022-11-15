package com.rokudo.xpense.data.retrofit.models;

public class RequisitionsResult {
    private Integer count;
    private String next;
    private String previous;
    private Requisition[] results;

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

    public Requisition[] getResults() {
        return results;
    }

    public void setResults(Requisition[] results) {
        this.results = results;
    }
}
