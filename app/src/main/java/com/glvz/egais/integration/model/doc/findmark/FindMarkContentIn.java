package com.glvz.egais.integration.model.doc.findmark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocContentIn;

import java.util.Arrays;

public class FindMarkContentIn implements DocContentIn {

    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="NomenName")
    private String nomenName;
    @JsonProperty(value="Capacity")
    private Double capacity;
    @JsonProperty(value="AlcVolume")
    private Double alcVolume;
    @JsonProperty(value="Mark")
    private String[] mark;

    @Override
    public Double getQty() {
        return qty;
    }

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

    public String[] getMark() {
        return mark;
    }

    public void setMark(String[] mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "FindMarkContentIn{" +
                "position='" + position + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", qty=" + qty +
                ", nomenName='" + nomenName + '\'' +
                ", capacity=" + capacity +
                ", alcVolume=" + alcVolume +
                ", mark=" + Arrays.toString(mark) +
                '}';
    }
}
