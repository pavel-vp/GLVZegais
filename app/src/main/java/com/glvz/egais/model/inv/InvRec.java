package com.glvz.egais.model.inv;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.integration.model.doc.inv.InvRecContentOutput;
import com.glvz.egais.integration.model.doc.inv.InvRecOutput;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.StringUtils;
import java.util.*;

public class InvRec extends BaseRec {
    public static final String KEY_CONTENT_SIZE = "content_size";
    // ссылка наисходный имопртированный документ
    private String docId;
    private InvIn invIn;

    public InvRec(String docId, InvIn invIn) {
        this.docId = docId;
        this.invIn = invIn;
    }

    @Override
    public Date getDate() {
        return StringUtils.jsonStringToDate(invIn.getDate());
    }

    @Override
    public String getAgentName() {
        return null;
    }

    @Override
    public String getDocNum() {
        return invIn.getNumber();
    }

    @Override
    public DocIn getDocIn() {
        return invIn;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        List<DocContentIn> list = new ArrayList<>();
        for (InvContentIn contentIn : invIn.getContent()) {
            list.add(contentIn);
        }
        return list;
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        InvContentIn contentIn = (InvContentIn)docContentIn;
        return new InvRecContent(contentIn.getPosition());
    }

    @Override
    public BaseRecOutput formatAsOutput() {
        InvRecOutput rec = new InvRecOutput();
        rec.setDocId(this.invIn.getDocId());
        rec.setNumber(this.invIn.getNumber());
        rec.setDate(this.invIn.getDate());
        rec.setSkladID(this.invIn.getSkladID());
        rec.setSkladName(this.invIn.getSkladName());
        rec.setComment(this.invIn.getComment());
        rec.setContent(new InvRecContentOutput[this.getRecContentList().size()]);
        rec.setCheckMark(this.invIn.getCheckMark());
        int idx = 0;
        for (BaseRecContent recContent : this.getRecContentList()) {
            InvRecContent invRecContent = (InvRecContent)recContent;

            InvRecContentOutput contentOutput = new InvRecContentOutput();
            contentOutput.setPosition(invRecContent.getPosition());
            contentOutput.setNomenId(invRecContent.getId1c());
            if (invRecContent.getContentIn() != null) {
                contentOutput.setQty(invRecContent.getContentIn().getQty());
            }

            contentOutput.setQtyFact(invRecContent.getQtyAccepted());

            Set<BaseRecContentMark> scannedMarkSet = new HashSet<>(invRecContent.getBaseRecContentMarkList());

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
    public void rejectData() {
        // Пройтись по всем строкам, и проставить везде нули
        for (BaseRecContent recContent : getRecContentList()) {
            recContent.setQtyAccepted(null);
            recContent.getBaseRecContentMarkList().clear();
            recContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
        }
        // у документа статус — Новый
        setStatus(BaseRecStatus.NEW);
    }

    @Override
    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public InvIn getInvIn() {
        return invIn;
    }

    public void setInvIn(InvIn invIn) {
        this.invIn = invIn;
    }

    public Collection<InvRecContent> getInvRecContentList() {
        List<InvRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((InvRecContent) recContent);
        }
        return list;
    }

}
