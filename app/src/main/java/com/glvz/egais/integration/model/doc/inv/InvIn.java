package com.glvz.egais.integration.model.doc.inv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocIn;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvIn implements DocIn {
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
    @JsonProperty(value="CheckMark")
    private String checkMark;
    @JsonProperty(value="Comment")
    private String comment;
    @JsonProperty(value="QTYDirectInput")
    private Integer qtyDirectInput;

    @JsonProperty(value="Content")
    private InvContentIn[] content;

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

    public String getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(String checkMark) {
        this.checkMark = checkMark;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public InvContentIn[] getContent() {
        return content;
    }

    public void setContent(InvContentIn[] content) {
        this.content = content;
    }


    public Integer getQtyDirectInput() {
        return qtyDirectInput;
    }

    public boolean ableDirectInput() {
        return qtyDirectInput != null && qtyDirectInput != 0;
    }

    public void setQtyDirectInput(Integer qtyDirectInput) {
        this.qtyDirectInput = qtyDirectInput;
    }

    @Override
    public String toString() {
        return "InvIn{" +
                "docId='" + docId + '\'' +
                ", number='" + number + '\'' +
                ", date='" + date + '\'' +
                ", skladID='" + skladID + '\'' +
                ", skladName='" + skladName + '\'' +
                ", checkMark='" + checkMark + '\'' +
                ", comment='" + comment + '\'' +
                ", qtyDirectInput=" + qtyDirectInput +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
