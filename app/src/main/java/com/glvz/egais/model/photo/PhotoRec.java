package com.glvz.egais.model.photo;

import android.util.Base64;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PhotoRec extends BaseRec {

    public static final String KEY_DATE = "key_date";
    public static final String KEY_FILENAME = "key_filename";
    public static final String KEY_DATA = "key_data";
    public static final String KEY_DATAMINI = "key_datamini";

    String docId;
    String docNum;
    String date;
    String skladId;
    String skladName;
    String data;
    String dataMini;

    public PhotoRec(String skladId, String skladName, byte[] data, byte[] dataMini, String date) {
        this.skladId = skladId;
        this.skladName = skladName;
        this.date = date;
        this.docNum = DaoMem.getDaoMem().getNewDocId();
        this.docId = "IMG-"+date;
        this.data = Base64.encodeToString(data, Base64.DEFAULT);
        this.dataMini = Base64.encodeToString(dataMini, Base64.DEFAULT);
    }

    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public String getDocIdForExport() {
        return date;
    }

    @Override
    public String getAgentName() {
        return skladName;
    }

    @Override
    public Date getDate() {
        return StringUtils.imgStringToDate(date);
    }

    @Override
    public String getDocNum() {
        return docNum;
    }

    @Override
    public DocIn getDocIn() {
        return null;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        return Collections.emptyList();
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        return null;
    }

    @Override
    public BaseRecOutput formatAsOutput() {
        return null;
    }

    @Override
    public String getStatusDesc() {
        return "";
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataMini() {
        return dataMini;
    }


    public String getSkladId() {
        return skladId;
    }


}
