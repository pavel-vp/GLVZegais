package com.glvz.egais.model.checkmark;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.checkmark.*;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.StringUtils;

import java.util.*;

public class CheckMarkRec extends BaseRec {

    // ссылка наисходный имопртированный документ
    private String docId;
    private CheckMarkIn checkMarkIn;

    public CheckMarkRec(String docId, CheckMarkIn checkMarkIn) {
        this.docId = docId;
        this.checkMarkIn = checkMarkIn;
    }


    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public String getAgentName() {
        return null;
    }

    @Override
    public Date getDate() {
        return StringUtils.jsonStringToDate(checkMarkIn.getDate());    }

    @Override
    public String getDocNum() {
        return checkMarkIn.getNumber();
    }

    @Override
    public DocIn getDocIn() {
        return checkMarkIn;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        List<DocContentIn> list = new ArrayList<>();
        for (CheckMarkContentIn checkMarkContentIn : checkMarkIn.getContent()) {
            list.add(checkMarkContentIn);
        }
        return list;
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        CheckMarkContentIn checkMarkContentIn = (CheckMarkContentIn)docContentIn;
        return new CheckMarkRecContent(checkMarkContentIn.getPosition(), checkMarkContentIn);

    }

    @Override
    public BaseRecOutput formatAsOutput() {
        CheckMarkRecOutput rec = new CheckMarkRecOutput();
        rec.setDocId(this.checkMarkIn.getDocId());
        rec.setNumber(this.checkMarkIn.getNumber());
        rec.setDate(this.checkMarkIn.getDate());
        rec.setSkladID(this.checkMarkIn.getSkladID());
        rec.setContent(new CheckMarkRecContentOutput[this.checkMarkIn.getContent().length]);
        int idx = 0;
        for (CheckMarkContentIn contentIn : this.checkMarkIn.getContent()) {
            CheckMarkRecContentOutput contentOutput = new CheckMarkRecContentOutput();
            contentOutput.setPosition(contentIn.getPosition());
            contentOutput.setNomenId(contentIn.getNomenId());
            contentOutput.setQty(contentIn.getQty());

            CheckMarkRecContent recContent = (CheckMarkRecContent) DaoMem.getDaoMem().getRecContentByPosition(this, contentIn.getPosition());
            contentOutput.setQtyFact(recContent.getQtyAccepted());
            contentOutput.setQtyNewFact(recContent.getQtyAcceptedNew());

            CheckMarkMark[] scannedMarkArr = new CheckMarkMark[recContent.getCheckMarkRecContentMarkList().size()];
            int idx2 = 0;
            for (CheckMarkRecContentMark checkMarkContentMark : recContent.getCheckMarkRecContentMarkList()) {
                CheckMarkMark checkMarkMark = new CheckMarkMark();
                checkMarkMark.setMark(checkMarkContentMark.getMarkScanned());
                checkMarkMark.setState(checkMarkContentMark.getState());
                scannedMarkArr[idx2] = checkMarkMark;
                idx2++;
            }
            contentOutput.setMarks(scannedMarkArr);

            rec.getContent()[idx] = contentOutput;
            idx++;
        }
        return rec;
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

    public Collection<CheckMarkRecContent> getCheckMarkRecContentList() {
        List<CheckMarkRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((CheckMarkRecContent) recContent);
        }
        return list;
    }

    @Override
    public void rejectData() {
        // Пройтись по всем строкам, очистить связки с товаром и проставить везде нули
        for (CheckMarkRecContent recContent : getCheckMarkRecContentList()) {
            recContent.setNomenIn(null, null);
            recContent.setQtyAccepted(Double.valueOf(0));
            recContent.setQtyAcceptedNew(Double.valueOf(0));

            recContent.getCheckMarkRecContentMarkList().clear();
            recContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
        }
        setStatus(BaseRecStatus.NEW);
    }

    public String getSkladId() {
        return checkMarkIn.getSkladID();
    }

    public String getDateStr() {
        return checkMarkIn.getDate();
    }

}
