package com.glvz.egais.ui.doc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.DocArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

public abstract class ActBaseDocList extends Activity implements BarcodeReader.BarcodeListener {

    ListView lv;
    protected volatile List<BaseRec> list = new ArrayList<>();
    protected DocArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomelist);

        setResources();
        updateList();
    }

    protected void setResources() {
        lv = (ListView)findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(list.get(position));
            }
        });
        adapter = new DocArrayAdapter(this, R.layout.rec_doc, list);
        lv.setAdapter(adapter);
    }

    abstract protected void updateList();

    abstract protected void pickRec(BaseRec req);

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        BarcodeObject.setCurrentListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
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

