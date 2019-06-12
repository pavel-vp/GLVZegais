package com.glvz.egais.model.checkmark;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkMark;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentPositionType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class CheckMarkRecContent extends BaseRecContent {

    protected Double qtyAcceptedNew;


    public CheckMarkRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }
    public Double getQtyAcceptedNew() {
        return qtyAcceptedNew;
    }

    public void setQtyAcceptedNew(Double qtyAcceptedNew) {
        this.qtyAcceptedNew = qtyAcceptedNew;
    }


    public Collection<CheckMarkRecContentMark> getCheckMarkRecContentMarkList() {
        List<CheckMarkRecContentMark> list = new ArrayList<>();
        for (BaseRecContentMark recContent : baseRecContentMarkList) {
            list.add((CheckMarkRecContentMark) recContent);
        }
        return list;
    }


}
