package com.glvz.egais.ui.doc.income;

import android.content.Intent;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.DocConstants;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.income.IncomeArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.income.alco.ActIncomeAlcoRec;
import com.glvz.egais.ui.doc.income.ciga.ActIncomeCigaRec;

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
        switch (((IncomeIn)(req.getDocIn())).getMarkType()) {
            case DocConstants.MARKTYPE_ALCO: {
                Intent in = new Intent();
                in.setClass(ActIncomeList.this, ActIncomeAlcoRec.class);
                in.putExtra(ActIncomeAlcoRec.REC_DOCID, req.getDocId());
                startActivity(in);
                break;
            }
            case DocConstants.MARKTYPE_CIGA: {
                Intent in = new Intent();
                in.setClass(ActIncomeList.this, ActIncomeCigaRec.class);
                in.putExtra(ActIncomeCigaRec.REC_DOCID, req.getDocId());
                startActivity(in);
                break;
            }
        }

    }

}
