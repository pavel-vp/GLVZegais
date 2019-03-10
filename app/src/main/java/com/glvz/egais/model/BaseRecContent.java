package com.glvz.egais.model;

import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.doc.DocContentIn;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecContent {
    // ссылка наисходный имопртированную позицию документа
    protected String position;
    protected DocContentIn contentIn;
    // данные заполненные локально на терминале
    protected BaseRecContentStatus status = BaseRecContentStatus.NOT_ENTERED;
    protected String id1c; // кодсопоставленной записи 1с
    protected NomenIn nomenIn; // ссылка на запись в 1с (сопоставленную)
    protected String barcode;
    protected Double qtyAccepted;
    protected List<BaseRecContentMark> baseRecContentMarkList = new ArrayList<>(); // список сканированных марок

    public BaseRecContent(String position, DocContentIn docContentIn) {
        this.position = position;
        this.contentIn = docContentIn;
    }

    abstract public BaseRecContentPositionType getPositionType();

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public DocContentIn getContentIn() {
        return contentIn;
    }

    public void setContentIn(DocContentIn contentIn) {
        this.contentIn = contentIn;
    }

    public BaseRecContentStatus getStatus() {
        return status;
    }

    public void setStatus(BaseRecContentStatus status) {
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

    public void setNomenIn(NomenIn nomenIn, String barcodeData) {
        this.nomenIn = nomenIn;
        this.barcode = barcodeData;
        if (this.nomenIn == null) {
            this.id1c = null;
        } else {
            this.id1c = nomenIn.getId();
        }
    }
    public Double getQtyAccepted() {
        return qtyAccepted;
    }

    public void setQtyAccepted(Double qtyAccepted) {
        this.qtyAccepted = qtyAccepted;
    }


    public List<BaseRecContentMark> getBaseRecContentMarkList() {
        return baseRecContentMarkList;
    }

    public void setBaseRecContentMarkList(List<BaseRecContentMark> baseRecContentMarkList) {
        this.baseRecContentMarkList = baseRecContentMarkList;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

}
