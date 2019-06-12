package com.glvz.egais.integration.model.doc.checkmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckMarkMark {

    @JsonProperty(value="Mark")
    private String mark;
    @JsonProperty(value="State")
    private Integer state;

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
