package com.glvz.egais.model.inv;

import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class InvRecContent extends BaseRecContent {

    public static final int INV_FILTER_TYPE_NO = 0;
    public static final int INV_FILTER_TYPE_STATUS = 1;
    public static final int INV_FILTER_TYPE_DIFF = 2;

    public static final int INV_SORT_TYPE_POSITION = 0;
    public static final int INV_SORT_TYPE_NAME = 1;
    public static final int INV_SORT_TYPE_DIFF = 2;

    private Double manualMrc;

    public InvRecContent(String position) {
        super(position, null);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }

    @Override
    public InvContentIn getContentIn() {
        return (InvContentIn) contentIn;
    }

    @Override
    public String getId1c() {
        if (contentIn != null)
            return ((InvContentIn)contentIn).getNomenId();
        return this.id1c;
    }

    public Double getManualMrc() {
        return manualMrc;
    }

    public void setManualMrc(Double manualMrc) {
        this.manualMrc = manualMrc;
    }

    public double getDiff() {
        double diff = 0;
        if (contentIn != null && contentIn.getQty() != null) {
            diff = contentIn.getQty();
        }
        if (qtyAccepted != null) {
            diff = diff - qtyAccepted;
        }
        return diff;
    }

}
