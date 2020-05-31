package com.glvz.egais.integration.model.doc.income;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomeContentMarkIn {
    @JsonProperty(value="Box")
    private String box;
    @JsonProperty(value="Mark")
    private String mark;
    @JsonProperty(value="Multiplier")
    private String multiplier;


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


    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String toString() {
        return "IncomeContentMarkIn{" +
                "box='" + box + '\'' +
                ", mark='" + mark + '\'' +
                ", multiplier='" + multiplier + '\'' +
                '}';
    }
}
