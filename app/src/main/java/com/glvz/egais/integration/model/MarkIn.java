package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkIn {
    @JsonProperty(value="Mark")
    private String mark;
    @JsonProperty(value="AlcCode")
    private String alcCode;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="Box")
    private String box;

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


    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    @Override
    public String toString() {
        return "MarkIn{" +
                "mark='" + mark + '\'' +
                ", alcCode='" + alcCode + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", box='" + box + '\'' +
                '}';
    }
}
