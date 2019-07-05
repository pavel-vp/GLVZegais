package com.glvz.egais.ui.doc.writeoff;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.service.writeoff.WriteoffArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;

public class ActWriteoffList extends ActBaseDocList {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_writeofflist);
        adapter = new WriteoffArrayAdapter(this, R.layout.rec_writeoff, list);
        super.setResources();
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActWriteoffList.this);
                builder.setTitle("Подтвердите")
                        .setMessage("Выберите тип нового документа")
                        .setCancelable(true)
                        .setNegativeButton("Списание (или перемещение)", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createAndShowNewDoc(WriteoffRec.TYEDOC_WRIEOFF);
                            }
                        })
                        .setPositiveButton("Возврат поставщику", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createAndShowNewDoc(WriteoffRec.TYEDOC_RETURN);
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void createAndShowNewDoc(String type) {
        WriteoffRec rec = DaoMem.getDaoMem().addNewWriteoffRec(DaoMem.getDaoMem().getShopId(), DaoMem.getDaoMem().getShopInName(), type);
        updateList();
        // scroll to it
        lv.smoothScrollToPosition(0);
        // open it
        pickRec(rec);
    }

    @Override
    protected void updateList() {
        // Прочитать список накладных
        list.clear();
        for (WriteoffRec rec : DaoMem.getDaoMem().getWriteoffRecListOrdered()) {
            list.add(rec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActWriteoffList.this, ActWriteoffRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }
}
