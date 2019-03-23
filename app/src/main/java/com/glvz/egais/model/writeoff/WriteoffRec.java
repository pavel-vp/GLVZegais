package com.glvz.egais.model.writeoff;

import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;

import java.util.Date;
import java.util.List;

public class WriteoffRec extends BaseRec {

    String docId;

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
        return null;
    }

    @Override
    public String getDocNum() {
        return null;
    }

    @Override
    public DocIn getDocIn() {
        return null;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        return null;
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
        switch (status) {
            case DONE:
                return "Выполнено";
            case INPROGRESS:
                return "В работе";
        }
        return status.getMessage();
    }
}
