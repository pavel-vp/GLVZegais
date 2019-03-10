package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AlcCodeIn {

    @JsonProperty(value="AlcCode")
    private String alcCode;
    @JsonProperty(value="Name")
    private String name;
    @JsonProperty(value="NomenID")
    private String nomenId;


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AlcCodeIn{" +
                "alcCode='" + alcCode + '\'' +
                ", name='" + name + '\'' +
                ", nomenId='" + nomenId + '\'' +
                '}';
    }
}
