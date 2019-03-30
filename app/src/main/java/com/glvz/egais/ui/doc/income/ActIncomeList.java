package com.glvz.egais.ui.doc.income;

import android.content.Intent;
import android.os.Bundle;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.service.income.IncomeArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;

public class ActIncomeList extends ActBaseDocList {


    @Override
    protected void setResources() {
        setContentView(R.layout.activity_incomelist);
        adapter = new IncomeArrayAdapter(this, R.layout.rec_doc, list);
        super.setResources();
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        list.addAll(DaoMem.getDaoMem().getIncomeRecListOrdered());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActIncomeList.this, ActIncomeRec.class);
        in.putExtra(ActIncomeRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }

}
