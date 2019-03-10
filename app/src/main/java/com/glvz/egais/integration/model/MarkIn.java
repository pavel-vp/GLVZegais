package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarkIn {
    @JsonProperty(value="Mark")
    private String mark;
    @JsonProperty(value="AlcCode")
    private String alcCode;
    @JsonProperty(value="NomenID")
    private String nomenId;

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getAlcCode() {
        return alcCode;
    }

    public void setAlcCode(String alcCode) {
        this.alcCode = alcCode;
    }

    public String getNomenId() {
        return nomenId;
    }

    public void setNomenId(String nomenId) {
        this.nomenId = nomenId;
    }

    @Override
    public String toString() {
        return "MarkIn{" +
                "mark='" + mark + '\'' +
                ", alcCode='" + alcCode + '\'' +
                ", nomenId='" + nomenId + '\'' +
                '}';
    }
}
