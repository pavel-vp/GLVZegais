package com.glvz.egais.model;

import java.util.Objects;

public class BaseRecContentMark {
    public static final int MARK_SCANNED_AS_MARK = 0;
    public static final int MARK_SCANNED_AS_BOX = 1;

    protected String markScanned;
    protected Integer markScannedAsType = null;
    protected String markScannedReal;

    public BaseRecContentMark(String markScanned, Integer markScannedAsType, String markScannedReal) {
        this.markScanned = markScanned;
        this.markScannedAsType = markScannedAsType;
        this.markScannedReal = markScannedReal;
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


    public String getMarkScannedReal() {
        return markScannedReal;
    }

    public void setMarkScannedReal(String markScannedReal) {
        this.markScannedReal = markScannedReal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseRecContentMark that = (BaseRecContentMark) o;
        return Objects.equals(markScanned, that.markScanned);
    }

    @Override
    public int hashCode() {

        return Objects.hash(markScanned);
    }
}
