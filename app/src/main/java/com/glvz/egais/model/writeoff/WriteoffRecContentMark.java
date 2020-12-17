package com.glvz.egais.model.writeoff;

import com.glvz.egais.model.BaseRecContentMark;

public class WriteoffRecContentMark extends BaseRecContentMark {
    private String box;

    public WriteoffRecContentMark(String markScanned, Integer markScannedAsType, String markScannedReal, String box) {
        super(markScanned, markScannedAsType, markScannedReal);
        this.box = box;
    }

    public String getBox() {
        return box;
    }

    @Override
    public String toString() {
        return "WriteoffRecContentMark{" +
                "box='" + box + '\'' +
                ", markScanned='" + markScanned + '\'' +
                ", markScannedAsType=" + markScannedAsType +
                ", markScannedReal='" + markScannedReal + '\'' +
                '}';
    }
}
