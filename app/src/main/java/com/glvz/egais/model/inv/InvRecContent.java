package com.glvz.egais.model.inv;

import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class InvRecContent extends BaseRecContent {

    public InvRecContent(String position) {
        super(position, null);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        return null;
    }

    @Override
    public InvContentIn getContentIn() {
        return (InvContentIn) contentIn;
    }

    @Override
    public String getId1c() {
        if (contentIn != null)
            return ((InvContentIn)contentIn).getNomenId();
        return this.id1c;
    }

}
