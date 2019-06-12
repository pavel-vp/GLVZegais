package com.glvz.egais.model.checkmark;

import com.glvz.egais.model.BaseRecContentMark;

public class CheckMarkRecContentMark extends BaseRecContentMark {

    public static final int CHECKMARK_MARKSTATE_NOTCONFIRMED = 1;
    public static final int CHECKMARK_MARKSTATE_CONFIRMED = 2;
    public static final int CHECKMARK_MARKSTATE_NEWMARK = 3;

    private Integer state = CHECKMARK_MARKSTATE_NOTCONFIRMED;

    public CheckMarkRecContentMark(String markScanned, Integer markScannedAsType, String markScannedReal, Integer state) {
        super(markScanned, markScannedAsType, markScannedReal);
        this.state = state;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

}
