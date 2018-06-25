package com.glvz.egais.model;

public class IncomeRecContentMark {
    public static final int MARK_SCANNED_AS_MARK = 0;
    public static final int MARK_SCANNED_AS_BOX = 1;

    private String markScanned;
    private Integer markScannedAsType = null;

    public IncomeRecContentMark(String markScanned, Integer markScannedAsType) {
        this.markScanned = markScanned;
        this.markScannedAsType = markScannedAsType;
    }

    public String getMarkScanned() {
        return markScanned;
    }

    public void setMarkScanned(String markScanned) {
        this.markScanned = markScanned;
    }

    public Integer getMarkScannedAsType() {
        return markScannedAsType;
    }

    public void setMarkScannedAsType(Integer markScannedAsType) {
        this.markScannedAsType = markScannedAsType;
    }


}
