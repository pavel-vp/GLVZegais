package com.glvz.egais.model.price;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class PriceRecContent extends BaseRecContent {


    public PriceRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }
}
