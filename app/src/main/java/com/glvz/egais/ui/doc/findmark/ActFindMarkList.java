package com.glvz.egais.ui.doc.findmark;

import android.content.Intent;
import android.os.Bundle;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.service.findmark.FindMarkArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;

public class ActFindMarkList extends ActBaseDocList {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_findmarklist);
        adapter = new FindMarkArrayAdapter(this, R.layout.rec_findmark, list);
        super.setResources();
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        for (FindMarkRec findMarkRec : DaoMem.getDaoMem().getFindMarkRecListOrdered()) {
            list.add(findMarkRec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActFindMarkList.this, ActFindMarkRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }

}
