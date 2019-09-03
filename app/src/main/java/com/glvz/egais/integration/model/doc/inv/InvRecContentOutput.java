package com.glvz.egais.integration.model.doc.inv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.BaseRecContentOutput;

import java.util.Arrays;

public class InvRecContentOutput extends BaseRecContentOutput {
    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="QTY")
    private Double qty;
    @JsonProperty(value="QTYFact")
    private Double qtyFact;
    @JsonProperty(value="Marks")
    private String[] marks;
    @JsonProperty(value="MRC")
    private Double mrc;

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

    public Double getQtyFact() {
        return qtyFact;
    }

    public void setQtyFact(Double qtyFact) {
        this.qtyFact = qtyFact;
    }

    public String[] getMarks() {
        return marks;
    }

    public void setMarks(String[] marks) {
        this.marks = marks;
    }

    public Double getMrc() {
        return mrc;
    }

    public void setMrc(Double mrc) {
        this.mrc = mrc;
    }


    @Override
    public String toString() {
        return "InvRecContentOutput{" +
                "position='" + position + '\'' +
                ", nomenId='" + nomenId + '\'' +
                ", qty=" + qty +
                ", qtyFact=" + qtyFact +
                ", mrc=" + mrc +
                ", marks=" + Arrays.toString(marks) +
                '}';
    }
}
