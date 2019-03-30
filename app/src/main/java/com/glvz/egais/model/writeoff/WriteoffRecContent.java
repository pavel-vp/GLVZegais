package com.glvz.egais.model.writeoff;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class WriteoffRecContent extends BaseRecContent {


    public WriteoffRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }
}
