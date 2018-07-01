package com.glvz.egais.model;

import com.glvz.egais.integration.model.IncomeIn;

import java.util.ArrayList;
import java.util.List;

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


    @Override
    public String toString() {
        return "IncomeRec{" +
                "wbRegId='" + wbRegId + '\'' +
                ", incomeIn=" + incomeIn +
                '}';
    }
}
