package com.glvz.egais.model;

import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class BaseRec {

    public static final String KEY_SHOPID = "shopid";
    public static final String KEY_CNTDONE = "cntdone";
    public static final String KEY_STATUS = "status";
    public static final String KEY_EXPORTED = "exported";
    public static final String KEY_FILTER = "incomefilter";
    public static final String KEY_POS_ID1C = "pos_id1c";
    public static final String KEY_POS_BARCODE = "pos_barcode";
    public static final String KEY_POS_STATUS = "pos_status";
    public static final String KEY_POS_QTYACCEPTED = "pos_qtyaccepted";
    public static final String KEY_POS_QTYACCEPTED_NEW = "pos_qtyaccepted_new";
    public static final String KEY_POS_MARKSCANNED_CNT = "pos_markscanned_cnt";
    public static final String KEY_POS_MARKSCANNED = "pos_markscanned";
    public static final String KEY_POS_MARKSCANNED_ASTYPE = "pos_markscanned_astype";
    public static final String KEY_POS_MARKSCANNEDREAL = "pos_markscannedreal";
    public static final String KEY_POS_MARKBOX = "pos_markbox";
    public static final String KEY_POS_MARKSTATE = "pos_markstate";
    public static final String KEY_POS_POSITION = "pos_position";
    public static final String KEY_POS_MANUAL_MRC = "pos_manual_mrc";
    public static final String KEY_DOCID = "docid";
    public static final String KEY_DOC_CONTENTID = "doc_contentid";
    public static final String KEY_DOC_CONTENT_MARKID = "doc_content_markid";


    // данные заполненные локально на терминале
    protected int cntDone;
    protected BaseRecStatus status = BaseRecStatus.NEW;
    // список строк (собранный из импортированных и локальных данных)
    protected List<BaseRecContent> recContentList = new ArrayList<>();
    protected boolean exported;


    abstract public String getDocId();
    public String getDocIdForExport(){
        return getDocId();
    }
    abstract public String getAgentName();

    abstract public Date getDate();
    abstract public String getDocNum();

    public int getCntDone() {
        return cntDone;
    }

    public void setCntDone(int cntDone) {
        this.cntDone = cntDone;
    }

    public BaseRecStatus getStatus() {
        return status;
    }

    public void setStatus(BaseRecStatus status) {
        if (status != this.status) {
            this.exported = false;
        }
        this.status = status;
    }

    public List<BaseRecContent> getRecContentList() {
        return recContentList;
    }

    public void setRecContentList(List<BaseRecContent> recContentList) {
        this.recContentList = recContentList;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public abstract DocIn getDocIn();

    public abstract List<DocContentIn> getDocContentInList();

    public abstract BaseRecContent buildRecContent(DocContentIn docContentIn);

    public abstract BaseRecOutput formatAsOutput();

    public void clearData() throws Exception {
        throw new Exception("Not implemented method");
    };

    public void rejectData() {
        // Пройтись по всем строкам, очистить связки с товаром и проставить везде нули
        for (BaseRecContent recContent : getRecContentList()) {
            recContent.setNomenIn(null, null);
            recContent.setQtyAccepted(Double.valueOf(0));
            recContent.getBaseRecContentMarkList().clear();
            recContent.setStatus(BaseRecContentStatus.REJECTED);
        }
        setStatus(BaseRecStatus.REJECTED);
    }

    public BaseRecContent tryGetNextRecContent() {
        return null;
    }

    public abstract String getStatusDesc();

}
