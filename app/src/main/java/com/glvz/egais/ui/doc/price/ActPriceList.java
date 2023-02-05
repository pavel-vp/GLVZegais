package com.glvz.egais.ui.doc.price;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.price.PriceRec;
import com.glvz.egais.service.price.PriceArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocList;
import com.glvz.egais.ui.doc.ActBaseDocRec;

public class ActPriceList  extends ActBaseDocList {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_pricelist);
        adapter = new PriceArrayAdapter(this, R.layout.rec_price, list);
        super.setResources();
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActPriceList.this);
                builder.setTitle("Подтвердите")
                        .setMessage("Создать новый список для печати ценников?")
                        .setCancelable(true)
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                createAndShowNewDoc();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void createAndShowNewDoc() {
        PriceRec rec = DaoMem.getDaoMem().addNewPriceRec(DaoMem.getDaoMem().getShopId(), DaoMem.getDaoMem().getShopInName());
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
        for (PriceRec rec : DaoMem.getDaoMem().getPriceRecListOrdered()) {
            list.add(rec);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void pickRec(BaseRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActPriceList.this, ActPriceRec.class);
        in.putExtra(ActBaseDocRec.REC_DOCID, req.getDocId());
        startActivity(in);
    }
}
