package com.glvz.egais.ui.doc.inv;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.service.inv.InvContentArrayAdapter;
import com.glvz.egais.service.inv.InvRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

public class ActInvRec extends ActBaseDocRec {

    private InvRec invRec;
    private TextView tvCaption;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.invRec = DaoMem.getDaoMem().getMapInvRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_invrec);

        View container = findViewById(R.id.inclRecInv);
        docRecHolder = new InvRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActInvRec.this, invRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new InvContentArrayAdapter(this, R.layout.rec_inv_position, list);
        lvContent.setAdapter(adapter);

        tvCaption = (TextView) findViewById(R.id.tvCaption);

    }

    @Override
    protected void updateData() {
        docRecHolder.setItem(invRec);
        // Достать список позиций по накладной
        Collection<InvRecContent> newList = DaoMem.getDaoMem().getInvRecContentList(invRec.getDocId());
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
        // В зависимости от состояния - вывести текст на кнопке
        tvCaption.setText("Сканируйте марку или штрихкод");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invrec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_export:
                exportDoc();
                return true;
            case R.id.action_findbycode:
                //
                return true;
            case R.id.action_showraw:
                //
                return true;
            case R.id.action_showdiff:
                //
                return true;
            case R.id.action_clear:
                // - запрос на подтверждение очистки «Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу» Да/Нет
                //- удаляются данные о фактически собранном количестве и маркам по всему документу
                //- статус задания устанавливается в «новый»
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(invRec);
                                MessageUtils.showToastMessage("Данные по накладной удалены!");
                                updateData();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exportDoc() {
            //- собранные данные выгружаются в JSON-файл во внутреннюю память терминала в каталог «GLVZ\Shops\#ShopID#\Out»
            boolean success = DaoMem.getDaoMem().exportData(invRec);
            if (success) {
                MessageUtils.showToastMessage("Накладная выгружена!");
                updateData();
                syncDoc();
            } else {
                MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
            }
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // Открыть форму строки
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActInvRecContent.class);
        in.putExtra(ActInvRec.REC_DOCID, docId);
        in.putExtra(ActInvRec.RECCONTENT_POSITION, req.getPosition().toString());
        ctx.startActivity(in);

    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        MessageUtils.playSound(R.raw.docmove_tap_position);
    }

}

