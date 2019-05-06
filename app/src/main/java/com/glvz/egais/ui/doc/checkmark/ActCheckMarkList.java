package com.glvz.egais.ui.doc.checkmark;

import android.content.Intent;
import android.os.Bundle;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.service.checkmark.CheckMarkArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;

public class ActCheckMarkList extends ActBaseDocList {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_checkmarklist);
        adapter = new CheckMarkArrayAdapter(this, R.layout.rec_doc, list);
        super.setResources();
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        for (CheckMarkRec checkMarkRec : DaoMem.getDaoMem().getCheckMarkRecListOrdered()) {
            list.add(checkMarkRec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActCheckMarkList.this, ActCheckMarkRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }
}
