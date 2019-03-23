package com.glvz.egais.model.move;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.move.MoveContentIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.integration.model.doc.move.MoveRecContentOutput;
import com.glvz.egais.integration.model.doc.move.MoveRecOutput;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.StringUtils;

import java.util.*;

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
    public BaseRecOutput formatAsOutput() {
        MoveRecOutput rec = new MoveRecOutput();
        rec.setDocId(this.moveIn.getDocId());
        rec.setNumber(this.moveIn.getNumber());
        rec.setDate(this.moveIn.getDate());
        rec.setSkladID(this.moveIn.getSkladID());
        rec.setSkladName(this.moveIn.getSkladName());
        rec.setPoluchID(this.moveIn.getPoluchID());
        rec.setPoluchName(this.moveIn.getPoluchName());
        rec.setContent(new MoveRecContentOutput[this.moveIn.getContent().length]);
        int idx = 0;
        for (MoveContentIn contentIn : this.moveIn.getContent()) {
            MoveRecContentOutput contentOutput = new MoveRecContentOutput();
            contentOutput.setPosition(contentIn.getPosition());
            contentOutput.setNomenId(contentIn.getNomenId());
            contentOutput.setQty(contentIn.getQty());

            MoveRecContent recContent = (MoveRecContent) DaoMem.getDaoMem().getRecContentByPosition(this, contentIn.getPosition());
            contentOutput.setQtyFact(recContent.getQtyAccepted());

            Set<BaseRecContentMark> scannedMarkSet = new HashSet<>();
            scannedMarkSet.addAll(recContent.getBaseRecContentMarkList());

            contentOutput.setMarks(new String[scannedMarkSet.size()]);
            int idx2 = 0;
            for (BaseRecContentMark mark : scannedMarkSet) {
                contentOutput.getMarks()[idx2] = mark.getMarkScanned();
                idx2++;
            }
            rec.getContent()[idx] = contentOutput;
            idx++;
        }
        return rec;

    }

    @Override
    public BaseRecContent tryGetNextRecContent() {
        for (BaseRecContent recContent : getRecContentList()) {
            if (recContent.getStatus() != BaseRecContentStatus.DONE && recContent.getStatus() != BaseRecContentStatus.REJECTED) {
                return recContent;
            }
        }
        return null;
    }

    @Override
    public String getStatusDesc() {
        switch (status) {
            case DONE:
                return "Выполнено";
            case INPROGRESS:
                return "В работе";
        }
        return status.getMessage();
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

    public Collection<MoveRecContent> getMoveRecContentList() {
        List<MoveRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((MoveRecContent) recContent);
        }
        return list;
    }

}
