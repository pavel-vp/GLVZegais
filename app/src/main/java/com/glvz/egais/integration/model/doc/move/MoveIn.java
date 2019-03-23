package com.glvz.egais.integration.model.doc.move;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocIn;

import java.util.Arrays;

public class MoveIn implements DocIn {

    @JsonProperty(value="DOCId")
    private String docId;
    @JsonProperty(value="NUMBER")
    private String number;
    @JsonProperty(value="Date")
    private String date;
    @JsonProperty(value="SkladID")
    private String skladID;
    @JsonProperty(value="SkladName")
    private String skladName;
    @JsonProperty(value="PoluchID")
    private String poluchID;
    @JsonProperty(value="PoluchName")
    private String poluchName;
    @JsonProperty(value="CheckMark")
    private String checkMark;

    @JsonProperty(value="Content")
    private MoveContentIn[] content;

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

    public String getSkladName() {
        return skladName;
    }

    public void setSkladName(String skladName) {
        this.skladName = skladName;
    }

    public String getPoluchID() {
        return poluchID;
    }

    public void setPoluchID(String poluchID) {
        this.poluchID = poluchID;
    }

    public String getPoluchName() {
        return poluchName;
    }

    public void setPoluchName(String poluchName) {
        this.poluchName = poluchName;
    }

    public String getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(String checkMark) {
        this.checkMark = checkMark;
    }

    public MoveContentIn[] getContent() {
        return content;
    }

    public void setContent(MoveContentIn[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MoveIn{" +
                "docId='" + docId + '\'' +
                ", number='" + number + '\'' +
                ", date='" + date + '\'' +
                ", skladID='" + skladID + '\'' +
                ", skladName='" + skladName + '\'' +
                ", poluchID='" + poluchID + '\'' +
                ", poluchName='" + poluchName + '\'' +
                ", checkMark='" + checkMark + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
