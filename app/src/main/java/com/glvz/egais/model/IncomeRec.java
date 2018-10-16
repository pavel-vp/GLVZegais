package com.glvz.egais.model;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IncomeRec {
    // ссылка наисходный имопртированный документ
    private String wbRegId;
    private IncomeIn incomeIn;
    // данные заполненные локально на терминале
    private int cntDone;
    private IncomeRecStatus status = IncomeRecStatus.NEW;
    // список строк (собранный из импортированных и локальных данных)
    private List<IncomeRecContent> incomeRecContentList = new ArrayList<>();
    private boolean exported;


    public int getCntDone() {
        return cntDone;
    }

    public void setCntDone(int cntDone) {
        this.cntDone = cntDone;
    }

    public IncomeRecStatus getStatus() {
        return status;
    }

    public void setStatus(IncomeRecStatus status) {
        if (status != this.status) {
            this.exported = false;
        }
        this.status = status;
    }



    public IncomeRec(String wbRegId, IncomeIn incomeIn) {
        this.wbRegId = wbRegId;
        this.incomeIn = incomeIn;
    }

    public String getWbRegId() {

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

    public List<IncomeRecContent> getIncomeRecContentList() {
        return incomeRecContentList;
    }

    public void setIncomeRecContentList(List<IncomeRecContent> incomeRecContentList) {
        this.incomeRecContentList = incomeRecContentList;
    }


    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
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
            contentOutput.setQtyDirectInput(recContent.getIncomeContentIn().getQtyDirectInput());

            Set<IncomeRecContentMark> scannedMarkSet = new HashSet<>();
            scannedMarkSet.addAll(recContent.getIncomeRecContentMarkList());

            contentOutput.setMarks(new IncomeContentMarkIn[scannedMarkSet.size()]);
            int idx2 = 0;
            for (IncomeRecContentMark mark : scannedMarkSet) {
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
}
