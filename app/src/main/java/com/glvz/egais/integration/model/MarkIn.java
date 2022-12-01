package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkIn {
    @JsonProperty(value="Mark")
    private String mark;
    @JsonProperty(value="AlcCode")
    private String alcCode;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="Box")
    private String box;
    @JsonProperty(value="MRC")
    private Double mrc;

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

    public Double getMrc() {
        return mrc;
    }

    public void setMrc(Double mrc) {
        this.mrc = mrc;
    }

    @Override
    public String toString() {
        return "MarkIn{" +
                "mark='" + mark + '\'' +
                ", alcCode='" + alcCode + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", box='" + box + '\'' +
                ", mrc=" + mrc  +
                '}';
    }
}
