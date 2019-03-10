package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * Created by pasha on 07.06.18.
 */
public class NomenIn {

    public static final int NOMENTYPE_ALCO_MARK = 1;
    public static final int NOMENTYPE_ALCO_NOMARK = 2;
    public static final int NOMENTYPE_ALCO_TOBACCO = 3;
    public static final int NOMENTYPE_ALCO_OTHER = 0;

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
    @JsonProperty(value="NomenType")
    private Integer nomenType;
    @JsonProperty(value="MRCArr")
    private String[] mcArr;

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
        this.alcVolume = alcVolume;
    }

    public String[] getBarcode() {
        return barcode;
    }

    public void setBarcode(String[] barcode) {
        this.barcode = barcode;
    }

    public Integer getNomenType() {
        return nomenType;
    }

    public void setNomenType(Integer nomenType) {
        this.nomenType = nomenType;
    }

    public String[] getMcArr() {
        return mcArr;
    }

    public void setMcArr(String[] mcArr) {
        this.mcArr = mcArr;
    }

    @Override
    public String toString() {
        return "NomenIn{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", capacity=" + capacity +
                ", alcVolume=" + alcVolume +
                ", barcode=" + Arrays.toString(barcode) +
                ", nomenType=" + nomenType +
                ", mcArr=" + Arrays.toString(mcArr) +
                '}';
    }
}
