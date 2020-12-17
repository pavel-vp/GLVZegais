package com.glvz.egais.integration.model.doc.writeoff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glvz.egais.integration.model.doc.BaseRecContentOutput;

public class WriteoffRecContentOutput extends BaseRecContentOutput {
    @JsonProperty(value="position")
    private String position;
    @JsonProperty(value="NomenID")
    private String nomenId;
    @JsonProperty(value="QTYFact")
    private Double qtyFact;
    @JsonProperty(value="Marks")
    private WriteoffRecContentMarkOutput[] marks;

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

    public Double getQtyFact() {
        return qtyFact;
    }

    public void setQtyFact(Double qtyFact) {
        this.qtyFact = qtyFact;
    }

    public WriteoffRecContentMarkOutput[] getMarks() {
        return marks;
    }

    public void setMarks(WriteoffRecContentMarkOutput[] marks) {
        this.marks = marks;
    }
}
