package com.glvz.egais.integration.model.doc.checkmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocIn;

import java.util.Arrays;

public class CheckMarkIn implements DocIn {

    @JsonProperty(value="DOCId")
    private String docId;
    @JsonProperty(value="NUMBER")
    private String number;
    @JsonProperty(value="Date")
    private String date;
    @JsonProperty(value="SkladID")
    private String skladID;
    @JsonProperty(value="Content")
    private CheckMarkContentIn[] content;


    @Override
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSkladID() {
        return skladID;
    }

    public void setSkladID(String skladID) {
        this.skladID = skladID;
    }

    public CheckMarkContentIn[] getContent() {
        return content;
    }

    public void setContent(CheckMarkContentIn[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "CheckMarkIn{" +
                "docId='" + docId + '\'' +
                ", number='" + number + '\'' +
                ", date='" + date + '\'' +
                ", skladID='" + skladID + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
