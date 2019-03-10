package com.glvz.egais.model.income;

import android.content.SharedPreferences;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.dao.Dictionary;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.income.*;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.StringUtils;

import java.util.*;

public class IncomeRec extends BaseRec {
    // ссылка наисходный имопртированный документ
    private String wbRegId;
    private IncomeIn incomeIn;

    public IncomeRec(String wbRegId, IncomeIn incomeIn) {
        this.wbRegId = wbRegId;
        this.incomeIn = incomeIn;
    }

    @Override
    public String getDocId() {
        return wbRegId;
    }

    public void setWbRegId(String wbRegId) {
        this.wbRegId = wbRegId;
    }

    public IncomeIn getIncomeIn() {
        return incomeIn;
    }

    public void setIncomeIn(IncomeIn incomeIn) {
        this.incomeIn = incomeIn;
    }


    public IncomeRecOutput formatAsOutput() {
        IncomeRecOutput rec = new IncomeRecOutput();
        rec.setWbRegId(this.incomeIn.getWbRegId());
        rec.setNumber(this.incomeIn.getNumber());
        rec.setDate(this.incomeIn.getDate());
        rec.setSkladID(this.incomeIn.getSkladID());
        rec.setSkladName(this.incomeIn.getSkladName());
        rec.setPostID(this.incomeIn.getPostID());
        rec.setPostName(this.incomeIn.getPostName());
        rec.setContent(new IncomeRecContentOutput[this.incomeIn.getContent().length]);
        int idx = 0;
        for (IncomeContentIn contentIn : this.incomeIn.getContent()) {
            IncomeRecContentOutput contentOutput = new IncomeRecContentOutput();
            contentOutput.setPosition(contentIn.getPosition());
            contentOutput.setName(contentIn.getName());
            contentOutput.setAlccode(contentIn.getAlccode());
            contentOutput.setQty(contentIn.getQty());

            IncomeRecContent recContent = DaoMem.getDaoMem().getIncomeRecContentByPosition(this, contentIn.getPosition());
            contentOutput.setBarCode(recContent.getBarcode());
            contentOutput.setQtyFact(recContent.getQtyAccepted());
            contentOutput.setQtyDirectInput(recContent.getContentIn().getQtyDirectInput());

            Set<BaseRecContentMark> scannedMarkSet = new HashSet<>();
            scannedMarkSet.addAll(recContent.getBaseRecContentMarkList());

            contentOutput.setMarks(new IncomeContentMarkIn[scannedMarkSet.size()]);
            int idx2 = 0;
            for (BaseRecContentMark mark : scannedMarkSet) {
                IncomeContentMarkIn markOutput = new IncomeContentMarkIn();
                markOutput.setMark(mark.getMarkScanned());
                markOutput.setBox(mark.getMarkScannedReal());
                contentOutput.getMarks()[idx2] = markOutput;
                idx2++;
            }
            contentOutput.setBoxTree(contentIn.getBoxTree());
            rec.getContent()[idx] = contentOutput;
            idx++;
        }
        return rec;
    }

    @Override
    public String toString() {
        return "IncomeRec{" +
                "wbRegId='" + wbRegId + '\'' +
                ", incomeIn=" + incomeIn +
                '}';
    }


    @Override
    public Date getDate() {
        return StringUtils.jsonStringToDate(incomeIn.getDate());
    }

    @Override
    public String getAgentName() {
        return incomeIn.getPostName();
    }

    @Override
    public String getDocNum() {
        return incomeIn.getNumber();
    }

    @Override
    public DocIn getDocIn() {
        return incomeIn;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        List<DocContentIn> list = new ArrayList<>();
        for (IncomeContentIn incomeContentIn : incomeIn.getContent()) {
            list.add(incomeContentIn);
        }
        return list;
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        IncomeContentIn incomeContentIn = (IncomeContentIn)docContentIn;
        return new IncomeRecContent(incomeContentIn.getPosition(), incomeContentIn);
    }

    public List<IncomeRecContent> getIncomeRecContentList() {
        List<IncomeRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((IncomeRecContent) recContent);
        }
        return list;
    }
}
