package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.service.IncomeArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActIncomeList extends Activity implements BarcodeReader.BarcodeListener {

    ListView lv;
    volatile List<IncomeRec> list = new ArrayList<>();
    IncomeArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomelist);

        setResources();

        updateList();
    }

    private void setResources() {
        lv = (ListView)findViewById(R.id.listView);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(list.get(position));
            }
        });
        adapter = new IncomeArrayAdapter(this, R.layout.rec_prih, list);
        lv.setAdapter(adapter);

    }

    private void updateList() {
        // Прочитать список накладных
        Collection<IncomeRec> newList = DaoMem.getDaoMem().getIncomeRecListOrdered();
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    private void pickRec(IncomeRec req) {
        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActIncomeList.this, ActIncomeRec.class);
        in.putExtra(ActIncomeRec.INCOMEREC_WBREGID, req.getWbRegId());
        startActivity(in);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        BarcodeObject.linkToListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.unLinkFromListener(this);
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        MessageUtils.playSound(R.raw.choose_nakl_from_list);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }
}
