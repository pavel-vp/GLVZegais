package com.glvz.egais.ui.doc.inv;

import android.content.Intent;
import android.os.Bundle;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.service.inv.InvArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;

public class ActInvList extends ActBaseDocList {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_invlist);
        adapter = new InvArrayAdapter(this, R.layout.rec_doc, list);
        super.setResources();
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        for (InvRec rec : DaoMem.getDaoMem().getInvRecListOrdered()) {
            list.add(rec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActInvList.this, ActInvRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }

}

