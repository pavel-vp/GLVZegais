package com.glvz.egais.dao;

import com.glvz.egais.integration.model.doc.move.MoveContentIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;

public interface DocumentMove {
    MoveIn findMoveInById(String s);

    MoveContentIn findMoveContentByMarkDM(String wbRegId, String markDM);

}
