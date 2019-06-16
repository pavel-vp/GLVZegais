package com.glvz.egais.ui.doc.inv;

import android.app.AlertDialog;
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
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.service.PickMRCCallback;
import com.glvz.egais.service.inv.InvContentArrayAdapter;
import com.glvz.egais.service.inv.InvRecHolder;
import com.glvz.egais.ui.ActEnterNomenId;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.*;

public class ActInvRec extends ActBaseDocRec implements PickMRCCallback{

    private static final int ENTERNOMENID_RETCODE = 1;
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
        Collection<InvRecContent> newList = DaoMem.getDaoMem().getInvRecContentList(invRec.getDocId(), InvRecContent.INV_FILTER_TYPE_NO, InvRecContent.INV_SORT_TYPE_POSITION);
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
                // Открыть форму с редактироанием комментария
                Intent i = new Intent(ActInvRec.this, ActEnterNomenId.class);
                startActivityForResult(i, ENTERNOMENID_RETCODE);
                return true;
            case R.id.action_showraw:
                // перейти в форму «Список расхождений»
                // фильтр по товарным позициям со статусом «Не обработана»
                // сортировку по «Наименование и МРЦ»
                Intent in = new Intent();
                in.setClass(this, ActInvRecDiff.class);
                in.putExtra(ActInvRecDiff.INV_DOCID, invRec.getDocId());
                in.putExtra(ActInvRecDiff.INV_FILTER_TYPE, InvRecContent.INV_FILTER_TYPE_STATUS);
                in.putExtra(ActInvRecDiff.INV_SORT_TYPE, InvRecContent.INV_SORT_TYPE_NAME);
                this.startActivity(in);
                return true;
            case R.id.action_showdiff:
                // фильтр по товарным позициям у которых есть расхождение в количестве
                // сортировку по «Наименование и МРЦ»
                in = new Intent();
                in.setClass(this, ActInvRecDiff.class);
                in.putExtra(ActInvRecDiff.INV_DOCID, invRec.getDocId());
                in.putExtra(ActInvRecDiff.INV_FILTER_TYPE, InvRecContent.INV_FILTER_TYPE_DIFF);
                in.putExtra(ActInvRecDiff.INV_SORT_TYPE, InvRecContent.INV_SORT_TYPE_NAME);
                this.startActivity(in);
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

    public void onActivityResult(int requestCode, int resultCode, Intent i){
        if(requestCode == ENTERNOMENID_RETCODE ){
            if(resultCode==RESULT_OK){
                String nomenId = i.getData().toString();
                // поиск товара в nomen.json, определение его NomenType
                final NomenIn nomenIn = DaoMem.getDaoMem().findNomenInByNomenId(nomenId);
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Номенклатура с таким кодом не найдена");
                    return;
                }
                // Если NomenType = 3: отобразить список МРЦ этой номенклатуры для выбора пользователем.
                if (nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_TOBACCO) {
                    ActInvRec.chooseMRC(this, nomenIn, nomenIn.getMcArr(), this);
                    return;
                }
                onSelectMRCCallback(nomenIn, null);
            }
        }
    }

    static void chooseMRC(final Context ctx, final NomenIn nomenIn, final String[] mrcArr, final PickMRCCallback cb) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                final int[] choice = {0};
                new AlertDialog.Builder(ctx)
                        .setTitle("Выберите МРЦ")
                        .setSingleChoiceItems(mrcArr, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                choice[0] = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Найти  первую
                                String mrc = mrcArr[choice[0]];
                                cb.onSelectMRCCallback(nomenIn, Double.valueOf(mrc));
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onSelectMRCCallback(NomenIn nomenIn, Double mrc) {
        // по комбинации NomenID и МРЦ: поиск в товарных позициях документа
        int maxPos = 0;
        InvRecContent irc = null;
        for (BaseRecContent brc : invRec.getRecContentList()) {
            InvRecContent ircTemp = (InvRecContent)brc ;
            maxPos = Math.max(maxPos, Integer.parseInt(brc.getPosition()));
            if (brc.getId1c().equals(nomenIn.getId()) &&
                    (nomenIn.getNomenType() != NomenIn.NOMENTYPE_ALCO_TOBACCO ||
                            (ircTemp.getContentIn() != null && ircTemp.getContentIn().getMrc() != null && ircTemp.getContentIn().getMrc().equals(mrc)) ||
                            (ircTemp.getManualMrc() != null && ircTemp.getManualMrc().equals(mrc))
                    )
                    ) {
                irc = (InvRecContent) brc;
            }
        }

        if (irc == null) {
            // Создать новую запись
            irc = new InvRecContent(String.valueOf(maxPos+1));
            irc.setNomenIn(nomenIn, null);
            irc.setId1c(nomenIn.getId());
            irc.setStatus(BaseRecContentStatus.DONE);
            irc.setManualMrc(mrc);
            invRec.getRecContentList().add(irc);
            DaoMem.getDaoMem().writeLocalDataInvRec(invRec);
        }
        // переход к карточке этой строки
        pickRec(this, invRec.getDocId(), irc, 0, null, false, false);
    }
}

