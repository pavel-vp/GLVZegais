package com.glvz.egais.integration.model.doc.move;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocContentIn;

public class MoveContentIn implements DocContentIn {
    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="QTY")
    private Double qty;


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

    @Override
    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "MoveContentIn{" +
                "position='" + position + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", qty=" + qty +
                '}';
    }
}
