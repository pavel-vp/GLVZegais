package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class IncomeRecContentOutput implements Serializable {
    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="alccode")
    private String alccode;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="QTYFact")
    private Double qtyFact;
    @JsonProperty(value="BarCode")
    private String barCode;
    @JsonProperty(value="QTYDirectInput")
    private Integer qtyDirectInput;
    @JsonProperty(value="Marks")
    private IncomeContentMarkIn[] marks;
    @JsonProperty(value="BoxTree")
    private IncomeContentBoxTreeIn[] boxTree;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getAlccode() {
        return alccode;
    }

    public void setAlccode(String alccode) {
        this.alccode = alccode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getQtyFact() {
        return qtyFact;
    }

    public void setQtyFact(Double qtyFact) {
        this.qtyFact = qtyFact;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public IncomeContentMarkIn[] getMarks() {
        return marks;
    }

    public void setMarks(IncomeContentMarkIn[] marks) {
        this.marks = marks;
    }

    public IncomeContentBoxTreeIn[] getBoxTree() {
        return boxTree;
    }

    public void setBoxTree(IncomeContentBoxTreeIn[] boxTree) {
        this.boxTree = boxTree;
    }

    public Integer getQtyDirectInput() {
        return qtyDirectInput;
    }

    public void setQtyDirectInput(Integer qtyDirectInput) {
        this.qtyDirectInput = qtyDirectInput;
    }

}
