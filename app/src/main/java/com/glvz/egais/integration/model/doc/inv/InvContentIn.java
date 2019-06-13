package com.glvz.egais.integration.model.doc.inv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.DocContentIn;

public class InvContentIn implements DocContentIn {

    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="MRC")
    private Double mrc;
    @JsonProperty(value="QTY")
    private Double qty;

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

    public Double getMrc() {
        return mrc;
    }

    public void setMrc(Double mrc) {
        this.mrc = mrc;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    @Override
    public String toString() {
        return "InvContentIn{" +
                "position='" + position + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", mrc=" + mrc +
                ", qty=" + qty +
                '}';
    }
}
