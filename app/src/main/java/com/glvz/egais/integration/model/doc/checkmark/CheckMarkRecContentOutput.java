package com.glvz.egais.integration.model.doc.checkmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.BaseRecContentOutput;

import java.util.Arrays;

public class CheckMarkRecContentOutput extends BaseRecContentOutput {
    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="QTYFact")
    private Double qtyFact;
    @JsonProperty(value="QTYNewFact")
    private Double qtyNewFact;
    @JsonProperty(value="NomenName")
    private String nomenName;
    @JsonProperty(value="Capacity")
    private Double capacity;
    @JsonProperty(value="AlcVolume")
    private Double alcVolume;
    @JsonProperty(value="Marks")
    private CheckMarkMark[] marks;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNomenId() {
        return nomenId;
    }

    public void setNomenId(String nomenId) {
        this.nomenId = nomenId;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public String getNomenName() {
        return nomenName;
    }

    public void setNomenName(String nomenName) {
        this.nomenName = nomenName;
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

    public CheckMarkMark[] getMarks() {
        return marks;
    }

    public void setMarks(CheckMarkMark[] marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "CheckMarkRecContentOutput{" +
                "position='" + position + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", qty=" + qty +
                ", nomenName='" + nomenName + '\'' +
                ", capacity=" + capacity +
                ", alcVolume=" + alcVolume +
                ", marks=" + Arrays.toString(marks) +
                '}';
    }

    public Double getQtyFact() {
        return qtyFact;
    }

    public void setQtyFact(Double qtyFact) {
        this.qtyFact = qtyFact;
    }

    public Double getQtyNewFact() {
        return qtyNewFact;
    }

    public void setQtyNewFact(Double qtyNewFact) {
        this.qtyNewFact = qtyNewFact;
    }
}
