package com.glvz.egais.model.move;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.move.MoveContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class MoveRecContent extends BaseRecContent {

    public MoveRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }

    @Override
    public MoveContentIn getContentIn() {
        return (MoveContentIn) contentIn;
    }

    @Override
    public String getId1c() {
        return ((MoveContentIn)contentIn).getNomenId();
    }

}
