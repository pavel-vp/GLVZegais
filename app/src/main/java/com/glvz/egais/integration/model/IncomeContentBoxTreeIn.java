package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncomeContentBoxTreeIn {

    @JsonProperty(value="Box")
    private String box;
    @JsonProperty(value="ParentBox")
    private String parentBox;

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getParentBox() {
        return parentBox;
    }

    public void setParentBox(String parentBox) {
        this.parentBox = parentBox;
    }

    @Override
    public String toString() {
        return "IncomeContentBoxTreeIn{" +
                "box='" + box + '\'' +
                ", parentBox='" + parentBox + '\'' +
                '}';
    }
}
