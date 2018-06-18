package com.glvz.egais.model;

import com.glvz.egais.integration.model.IncomeContentIn;
import com.glvz.egais.integration.model.NomenIn;

public class IncomeRecContent {
    // ссылка наисходный имопртированную позицию документа
    private Integer position;
    private IncomeContentIn incomeContentIn;
    // данные заполненные локально на терминале
    private IncomeRecContentStatus status = IncomeRecContentStatus.NOT_ENTERED;
    private String id1c; // кодсопоставленной записи 1с
    private NomenIn nomenIn; // ссылка на запись в 1с (сопоставленную)
    private Double qtyAccepted;

    public IncomeRecContent(Integer position, IncomeContentIn incomeContentIn) {
        this.position = position;
        this.incomeContentIn = incomeContentIn;
    }


    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public IncomeContentIn getIncomeContentIn() {
        return incomeContentIn;
    }

    public void setIncomeContentIn(IncomeContentIn incomeContentIn) {
        this.incomeContentIn = incomeContentIn;
    }

    public IncomeRecContentStatus getStatus() {
        return status;
    }

    public void setStatus(IncomeRecContentStatus status) {
        this.status = status;
    }

    public String getId1c() {
        return id1c;
    }

    public void setId1c(String id1c) {
        this.id1c = id1c;
    }

    public NomenIn getNomenIn() {
        return nomenIn;
    }

    public void setNomenIn(NomenIn nomenIn) {
        this.nomenIn = nomenIn;
    }
    public Double getQtyAccepted() {
        return qtyAccepted;
    }

    public void setQtyAccepted(Double qtyAccepted) {
        this.qtyAccepted = qtyAccepted;
    }


}
