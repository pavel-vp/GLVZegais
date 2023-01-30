package com.glvz.egais.model.price;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PriceRec  extends BaseRec {
    public static final String TYEDOC_PRICE = "PRICE";
    public static final String KEY_DOCNUM = "key_docnum";
    public static final String KEY_DATE = "key_date";
    public static final String KEY_TYPEDOC = "key_typedoc";
    public static final String KEY_SKLADID = "key_skladid";
    public static final String KEY_SKLADNAME = "key_skladname";
    public static final String KEY_COMMENT = "key_comment";

    String docId;
    String docNum;
    String date;
    String comment;
    String skladId;
    String skladName;
    String typeDoc;

    public PriceRec(String shopId, String shopName) {
        super();
        this.skladId = shopId;
        this.skladName = shopName;
        this.docNum = DaoMem.getDaoMem().getNewDocId();
        this.date = StringUtils.formatDateDisplay(Calendar.getInstance().getTime());
        this.docId = TYEDOC_PRICE +"-"+docNum;
    }

    public PriceRec(String docId) {
        super();
        this.docId = docId;
    }

    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public String getAgentName() {
        return skladName;
    }

    @Override
    public Date getDate() {
        return StringUtils.simpleStringToDate(date);
    }

    public void setDate(String date) {
        this.date = date;
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
        return null;
    }

    public Collection<PriceRecContent> getPriceRecContentList() {
        List<PriceRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((PriceRecContent) recContent);
        }
        return list;
    }
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public PriceRecContent removePriceRecContent(int position) {
        PriceRecContent result = null;
        Iterator<BaseRecContent> iterator = recContentList.iterator();
        while (iterator.hasNext()) {
            BaseRecContent wrc = iterator.next();
            if (wrc.getPosition().equals(String.valueOf(position))) {
                result = (PriceRecContent) wrc;
                iterator.remove();
            }
        }
        int pos = 1;
        for (BaseRecContent wrc : recContentList) {
            wrc.setPosition(String.valueOf(pos));
            pos++;
        }
        return result;
    }


    public String buildNomenList() {
        List<String> nomens = recContentList.stream().map(el -> el.getNomenIn().getId()).collect(Collectors.toList());
        return String.join(";", nomens);
    }
}
