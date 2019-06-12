package com.glvz.egais.model.findmark;

import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.findmark.FindMarkContentIn;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class FindMarkRec extends BaseRec {

    // ссылка наисходный имопртированный документ
    private String docId;
    private FindMarkIn findMarkIn;

    public FindMarkRec(String docId, FindMarkIn findMarkIn) {
        this.docId = docId;
        this.findMarkIn = findMarkIn;
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
        return StringUtils.jsonStringToDate(findMarkIn.getDate());
    }

    @Override
    public String getDocNum() {
        return findMarkIn.getNumber();
    }

    @Override
    public DocIn getDocIn() {
        return findMarkIn;
    }

    @Override
    public List<DocContentIn> getDocContentInList() {
        List<DocContentIn> list = new ArrayList<>();
        for (FindMarkContentIn findMarkContentIn : findMarkIn.getContent()) {
            list.add(findMarkContentIn);
        }
        return list;
    }

    @Override
    public BaseRecContent buildRecContent(DocContentIn docContentIn) {
        FindMarkContentIn findMarkContentIn = (FindMarkContentIn)docContentIn;
        return new FindMarkRecContent(findMarkContentIn.getPosition(), findMarkContentIn);
    }

    @Override
    public BaseRecOutput formatAsOutput() {
        return null;
    }

    @Override
    public String getStatusDesc() {
        return null;
    }

    public Collection<FindMarkRecContent> getFindMarkRecContentList() {
        List<FindMarkRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((FindMarkRecContent) recContent);
        }
        return list;
    }


}
