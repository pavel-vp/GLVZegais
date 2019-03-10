package com.glvz.egais.model.move;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.move.MoveContentIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoveRec extends BaseRec {
    // ссылка наисходный имопртированный документ
    private String docId;
    private MoveIn moveIn;

    public MoveRec(String docId, MoveIn moveIn) {
        this.docId = docId;
        this.moveIn = moveIn;
    }

    @Override
    public Date getDate() {
        return StringUtils.jsonStringToDate(moveIn.getDate());
    }

    @Override
    public String getAgentName() {
        return moveIn.getPoluchName();
    }

    @Override
    public String getDocNum() {
        return moveIn.getNumber();
    }

    @Override
    public DocIn getDocIn() {
        return moveIn;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        List<DocContentIn> list = new ArrayList<>();
        for (MoveContentIn moveContentIn : moveIn.getContent()) {
            list.add(moveContentIn);
        }
        return list;
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        MoveContentIn moveContentIn = (MoveContentIn)docContentIn;
        return new MoveRecContent(moveContentIn.getPosition(), moveContentIn);
    }

    @Override
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public MoveIn getMoveIn() {
        return moveIn;
    }

    public void setMoveIn(MoveIn moveIn) {
        this.moveIn = moveIn;
    }

}
