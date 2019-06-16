package com.glvz.egais.ui.doc.inv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.inv.InvContentArrayAdapter;
import com.glvz.egais.service.inv.InvDiffArrayAdapter;
import com.glvz.egais.service.inv.InvRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActInvRecDiff extends Activity {

    public static final String INV_DOCID = "docid";
    public static final String INV_FILTER_TYPE = "filterype";
    public static final String INV_SORT_TYPE = "sorttype";

    private InvRec invRec;
    private int filterType;
    private int sortType;
    private ListView lvContent;
    private DocContentArrayAdapter adapter;
    private List<BaseRecContent> list = new ArrayList<>();
    private Spinner spSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRec();
        setResources();
        updateData();
    }

    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(INV_DOCID);
        this.filterType = extras.getInt(INV_FILTER_TYPE);
        this.sortType = extras.getInt(INV_SORT_TYPE);
        this.invRec = DaoMem.getDaoMem().getMapInvRec().get(key);
    }

    protected void setResources() {
        setContentView(R.layout.activity_invrecdiff);

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActInvRecDiff.this, invRec.getDocId(), list.get(position));
            }
        });
        adapter = new InvDiffArrayAdapter(this, R.layout.rec_inv_diff, list);
        lvContent.setAdapter(adapter);
        spSort = (Spinner) findViewById(R.id.spSort);
        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortType = position+1;
                updateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void pickRec(Context ctx, String docId, BaseRecContent req) {
        // Открыть карточку этой позиции
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActInvRecContent.class);
        in.putExtra(ActInvRec.REC_DOCID, docId);
        in.putExtra(ActInvRec.RECCONTENT_POSITION, req.getPosition().toString());
        ctx.startActivity(in);
    }

    protected void updateData() {
        // Достать список позиций по накладной
        Collection<InvRecContent> newList = DaoMem.getDaoMem().getInvRecContentList(invRec.getDocId(), filterType, sortType);
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
    }

}
