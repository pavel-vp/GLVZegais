package com.glvz.egais.model.findmark;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class FindMarkRecContent extends BaseRecContent {

    public FindMarkRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }
}
