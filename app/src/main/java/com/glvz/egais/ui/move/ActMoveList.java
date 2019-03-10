package com.glvz.egais.ui.move;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.service.move.MoveArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActMoveList extends Activity implements BarcodeReader.BarcodeListener {


    ListView lv;
    volatile List<MoveRec> list = new ArrayList<>();
    MoveArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movelist);

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
        adapter = new MoveArrayAdapter(this, R.layout.rec_move, list);
        lv.setAdapter(adapter);

    }

    private void updateList() {
        // Прочитать список накладных
        Collection<MoveRec> newList = DaoMem.getDaoMem().getMoveRecListOrdered();
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
    }


    private void pickRec(MoveRec req) {
/*        // Перейти в форму одного документа
        Intent in = new Intent();
        in.setClass(ActMoveList.this, ActMoveRec.class);
        in.putExtra(ActMoveRec.INCOMEREC_WBREGID, req.getDocId());
        startActivity(in);
        */
    }

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
