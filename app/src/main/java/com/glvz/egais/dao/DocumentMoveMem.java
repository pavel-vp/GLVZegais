package com.glvz.egais.dao;

import com.glvz.egais.integration.model.doc.move.MoveContentIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentMoveMem implements DocumentMove {

    Map<String, MoveIn> mapMoveIn = new HashMap<>();

    public DocumentMoveMem(List<MoveIn> listMoveIn) {
        for (MoveIn moveIn : listMoveIn) {
            mapMoveIn.put(moveIn.getDocId(), moveIn);
        }
    }

    @Override
    public MoveIn findMoveInById(String s) {
        return null;
    }

    @Override
    public MoveContentIn findMoveContentByMarkDM(String wbRegId, String markDM) {
        return null;
    }
}
