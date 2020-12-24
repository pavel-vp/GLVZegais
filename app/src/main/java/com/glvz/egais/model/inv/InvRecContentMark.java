package com.glvz.egais.model.inv;

import com.glvz.egais.model.BaseRecContentMark;

public class InvRecContentMark extends BaseRecContentMark {
    private String box;

    public InvRecContentMark(String markScanned, Integer markScannedAsType, String markScannedReal, String box) {
        super(markScanned, markScannedAsType, markScannedReal);
        this.box = box;
    }

    public String getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "InvRecContentMark{" +
                "box='" + box + '\'' +
                ", markScanned='" + markScanned + '\'' +
                ", markScannedAsType=" + markScannedAsType +
                ", markScannedReal='" + markScannedReal + '\'' +
                '}';
    }

}
