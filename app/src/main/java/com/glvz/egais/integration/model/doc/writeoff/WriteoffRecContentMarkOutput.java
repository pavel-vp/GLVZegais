package com.glvz.egais.integration.model.doc.writeoff;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class WriteoffRecContentMarkOutput implements Serializable {
    @JsonProperty(value="Box")
    private String box;
    @JsonProperty(value="Mark")
    private String mark;

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
}
