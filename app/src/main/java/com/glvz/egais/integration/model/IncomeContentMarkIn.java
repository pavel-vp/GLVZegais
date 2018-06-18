package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncomeContentMarkIn {
    @JsonProperty(value="Box")
    private String box;
    @JsonProperty(value="Mark")
    private String mark;

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "IncomeContentMarkIn{" +
                "box='" + box + '\'' +
                ", mark='" + mark + '\'' +
                '}';
    }
}
