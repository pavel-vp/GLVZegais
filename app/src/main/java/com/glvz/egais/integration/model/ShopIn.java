package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pasha on 07.06.18.
 */
public class ShopIn {

    public static final String CHECKMARK_DM = "DataMatrix";
    public static final String CHECKMARK_DMPDF = "DataMatrixPDF417";

    @JsonProperty(value="ID")
    private String id;
    @JsonProperty(value="Name")
    private String name;
    @JsonProperty(value="CheckMark")
    private String checkMark;

    public ShopIn() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(String checkMark) {
        this.checkMark = checkMark;
    }

    @Override
    public String toString() {
        return "ShopIn{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", checkMark='" + checkMark + '\'' +
                '}';
    }
}
