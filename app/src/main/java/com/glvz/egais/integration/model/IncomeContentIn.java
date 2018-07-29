package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class IncomeContentIn {

    @JsonProperty(value="position")
    private Integer position;
    @JsonProperty(value="alccode")
    private String alccode;
    @JsonProperty(value="name")
    private String name;
    @JsonProperty(value="Capacity")
    private Double capacity;
    @JsonProperty(value="AlcVolume")
    private String alcVolume;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="Price")
    private Double price;
    @JsonProperty(value="BottlingDate")
    private String bottlingDate;
    @JsonProperty(value="Marked")
    private Integer marked;
    @JsonProperty(value="MarkInfo")
    private IncomeContentMarkIn[] markInfo;
    @JsonProperty(value="BoxTree")
    private IncomeContentBoxTreeIn[] boxTree;

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
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

    public String getAlcVolume() {
        return alcVolume;
    }

    public void setAlcVolume(String alcVolume) {
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

    @Override
    public String toString() {
        return "IncomeContentIn{" +
                "position=" + position +
                ", alccode='" + alccode + '\'' +
                ", name='" + name + '\'' +
                ", capacity='" + capacity + '\'' +
                ", alcVolume='" + alcVolume + '\'' +
                ", qty=" + qty +
                ", price=" + price +
                ", bottlingDate='" + bottlingDate + '\'' +
                ", marked=" + marked +
                ", markInfo=" + Arrays.toString(markInfo) +
                ", boxTree=" + Arrays.toString(boxTree) +
                '}';
    }
}
