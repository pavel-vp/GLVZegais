package com.glvz.egais.ui.doc.move;

import android.content.Intent;
import android.os.Bundle;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;

public class ActMoveList extends ActBaseDocList {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_incomelist);
        super.setResources();
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        for (MoveRec moveRec : DaoMem.getDaoMem().getMoveRecListOrdered()) {
            list.add(moveRec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActMoveList.this, ActMoveRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }

}
