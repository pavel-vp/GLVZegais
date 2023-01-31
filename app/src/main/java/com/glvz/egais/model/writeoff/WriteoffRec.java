package com.glvz.egais.model.writeoff;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.writeoff.WriteoffRecContentMarkOutput;
import com.glvz.egais.integration.model.doc.writeoff.WriteoffRecContentOutput;
import com.glvz.egais.integration.model.doc.writeoff.WriteoffRecOutput;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.StringUtils;

import java.util.*;

public class WriteoffRec extends BaseRec {

    public static final String TYEDOC_WRITEOFF = "WRITEOFF";
    public static final String TYEDOC_RETURN = "RETURN";
    public static final String KEY_DOCNUM = "key_docnum";
    public static final String KEY_DATE = "key_date";
    public static final String KEY_TYPEDOC = "key_typedoc";
    public static final String KEY_SKLADID = "key_skladid";
    public static final String KEY_SKLADNAME = "key_skladname";
    public static final String KEY_COMMENT = "key_comment";
    public static final String KEY_CONTENT_SIZE = "key_content_size";

    String docId;
    String docNum;
    String date;
    String comment;
    String skladId;
    String skladName;
    String typeDoc;
    List<WriteoffRecContentOutput> writeoffRecContentOutputList = new ArrayList<>();

    public WriteoffRec(String skladId, String skladName, String typeDoc) {
        this.skladId = skladId;
        this.skladName = skladName;
        this.typeDoc = typeDoc;
        this.date = StringUtils.formatDateDisplay(Calendar.getInstance().getTime());
        this.docNum = DaoMem.getDaoMem().getNewDocId();
        this.docId = TYEDOC_WRITEOFF +"-"+docNum;
    }

    public WriteoffRec(String docId) {
        super();
        this.docId = docId;
    }


    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public String getDocIdForExport() {
        return getDocId() + "-" + DaoMem.getDaoMem().getDeviceId();
    }

    @Override
    public String getAgentName() {
        return skladName;
    }

    @Override
    public Date getDate() {
        return StringUtils.simpleStringToDate(date);
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
        WriteoffRecOutput rec = new WriteoffRecOutput();
        rec.setDocId(this.getDocId());
        rec.setNumber(this.getDocNum());
        rec.setDate(StringUtils.formatDateJson(StringUtils.simpleStringToDate(this.date)));
        rec.setSkladID(this.skladId);
        rec.setSkladName(this.skladName);
        rec.setTypeDoc(this.typeDoc);
        rec.setComment(this.comment);
        rec.setContent(new WriteoffRecContentOutput[this.recContentList.size()]);
        int idx = 0;
        for (WriteoffRecContent writeoffRecContent : this.getWriteoffRecContentList()) {
            WriteoffRecContentOutput contentOutput = new WriteoffRecContentOutput();
            contentOutput.setPosition(writeoffRecContent.getPosition());

            contentOutput.setQtyFact(writeoffRecContent.getQtyAccepted());
            contentOutput.setNomenId(writeoffRecContent.getNomenIn().getId());

            Set<WriteoffRecContentMark> scannedMarkSet = new HashSet<>();
            for (BaseRecContentMark recContentMark : writeoffRecContent.getBaseRecContentMarkList()) {
                scannedMarkSet.add((WriteoffRecContentMark) recContentMark);
            }

            contentOutput.setMarks(new WriteoffRecContentMarkOutput[scannedMarkSet.size()]);
            int idx2 = 0;
            for (WriteoffRecContentMark mark : scannedMarkSet) {
                WriteoffRecContentMarkOutput out = new WriteoffRecContentMarkOutput();
                out.setMark(mark.getMarkScanned());
                out.setBox(mark.getBox());
                contentOutput.getMarks()[idx2] = out;
                idx2++;
            }
            rec.getContent()[idx] = contentOutput;
            idx++;
        }
        return rec;
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

    public Collection<WriteoffRecContent> getWriteoffRecContentList() {
        List<WriteoffRecContent> list = new ArrayList<>();
        for (BaseRecContent recContent : recContentList) {
            list.add((WriteoffRecContent) recContent);
        }
        return list;
    }

    public WriteoffRecContent removeWriteoffRecContent(int position) {
        WriteoffRecContent result = null;
        Iterator<BaseRecContent> iterator = recContentList.iterator();
        while (iterator.hasNext()) {
            BaseRecContent wrc = iterator.next();
            if (wrc.getPosition().equals(String.valueOf(position))) {
                result = (WriteoffRecContent) wrc;
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

    public void setDateStr(String date) {
        this.date = date;
    }

    public String getSkladId() {
        return skladId;
    }

    public void setSkladId(String skladId) {
        this.skladId = skladId;
    }

    public String getSkladName() {
        return skladName;
    }

    public void setSkladName(String skladName) {
        this.skladName = skladName;
    }

    public String getTypeDoc() {
        return typeDoc;
    }

    public void setTypeDoc(String typeDoc) {
        this.typeDoc = typeDoc;
    }
    public String getComment() {
        return comment == null ? "" : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDocNum(String docNum) {
        this.docNum = docNum;
    }

    public String getDateStr() {
        return date;
    }

    @Override
    public void rejectData() {
        recContentList.clear();
        setStatus(BaseRecStatus.REJECTED);
    }

}
