package com.glvz.egais.integration.model.doc.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocContentIn;

import java.util.Arrays;

public class IncomeContentIn implements DocContentIn {

    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="alccode")
    private String alccode;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="Capacity")
    private Double capacity;
    @JsonProperty(value="AlcVolume")
    private Double alcVolume;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="Price")
    private Double price;
    @JsonProperty(value="BottlingDate")
    private String bottlingDate;
    @JsonProperty(value="Marked")
    private Integer marked;
    @JsonProperty(value="QTYDirectInput")
    private Integer qtyDirectInput;
    @JsonProperty(value="MarkInfo")
    private IncomeContentMarkIn[] markInfo;
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

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getAlcVolume() {
        return alcVolume;
    }

    public void setAlcVolume(Double alcVolume) {
        this.alcVolume = alcVolume;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getBottlingDate() {
        return bottlingDate;
    }

    public void setBottlingDate(String bottlingDate) {
        this.bottlingDate = bottlingDate;
    }

    public Integer getMarked() {
        return marked;
    }

    public void setMarked(Integer marked) {
        this.marked = marked;
    }

    public IncomeContentMarkIn[] getMarkInfo() {
        return markInfo;
    }

    public void setMarkInfo(IncomeContentMarkIn[] markInfo) {
        this.markInfo = markInfo;
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

    @Override
    public String toString() {
        return "IncomeContentIn{" +
                "position='" + position + '\'' +
                ", alccode='" + alccode + '\'' +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", alcVolume=" + alcVolume +
                ", qty=" + qty +
                ", price=" + price +
                ", bottlingDate='" + bottlingDate + '\'' +
                ", marked=" + marked +
                ", qtyDirectInput=" + qtyDirectInput +
                ", markInfo=" + Arrays.toString(markInfo) +
                ", boxTree=" + Arrays.toString(boxTree) +
                '}';
    }
}
