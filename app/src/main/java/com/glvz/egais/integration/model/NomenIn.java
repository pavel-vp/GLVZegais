package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created by pasha on 07.06.18.
 */
public class NomenIn {
    @JsonProperty(value="ID")
    private String id;
    @JsonProperty(value="Name")
    private String name;
    @JsonProperty(value="Capacity")
    private Double capacity;
    @JsonProperty(value="AlcVolume")
    private Double alcVolume;
    @JsonProperty(value="Barcode")
    private String[] barcode;

    public NomenIn() {
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
        alcVolume = alcVolume;
    }

    public String[] getBarcode() {
        return barcode;
    }

    public void setBarcode(String[] barcode) {
        this.barcode = barcode;
    }

    @Override
    public String toString() {
        return "NomenIn{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", alcVolume=" + alcVolume +
                ", barcode=" + Arrays.toString(barcode) +
                '}';
    }
}
