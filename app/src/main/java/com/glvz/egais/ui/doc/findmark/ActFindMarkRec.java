package com.glvz.egais.ui.doc.findmark;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.findmark.FindMarkContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.model.findmark.FindMarkRecContent;
import com.glvz.egais.service.findmark.FindMarkContentArrayAdapter;
import com.glvz.egais.service.findmark.FindMarkRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;

public class ActFindMarkRec extends ActBaseDocRec {

    FindMarkRec findMarkRec;


    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.findMarkRec = DaoMem.getDaoMem().getMapFindMarkRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_findmarkrec);

        View container = findViewById(R.id.inclRecFindMark);
        docRecHolder = new FindMarkRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        adapter = new FindMarkContentArrayAdapter(this, R.layout.rec_findmark_position, list);
        lvContent.setAdapter(adapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_findmarkrec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_clear:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Очистить все отсканированные марки?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().writeLocalDataRec_ClearAllMarks(findMarkRec);
                                for (FindMarkRecContent rc : findMarkRec.getFindMarkRecContentList()) {
                                    rc.getBaseRecContentMarkList().clear();
                                }
                                MessageUtils.showToastMessage("Марки очищены!");
                                DaoMem.getDaoMem().writeLocalDataFindMarkRec(findMarkRec);
                                updateData();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void updateData() {
        updateDataWithScroll(null);
    }

    private void updateDataWithScroll(final Integer position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                docRecHolder.setItem(findMarkRec);
                // Достать список позиций по накладной
                Collection<FindMarkRecContent> newList = DaoMem.getDaoMem().getFindMarkRecContentList(findMarkRec.getDocId());
                list.clear();
                list.addAll(newList);
                adapter.notifyDataSetChanged();
                if (position != null) {
                    lvContent.smoothScrollToPosition(position);
                }
            }
        });
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {

    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barCode = barcodeReadEvent.getBarcodeData();
        FindMarkRecContent findMarkRecContentLocal;
        switch (barCodeType) {
            case EAN8:
            case EAN13:
                MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте марки с алкогольной продукции");
                break;
            case PDF417:
            case DATAMATRIX:
                // выполнить проверку корректности ШК по длине:  PDF-417 должна быть 68 символов,  DataMatrix – 150
                if (barCodeType == DATAMATRIX && barCode.length() != 150) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина считанного штрихкода ("+ barCode.length()+"). Сканируйте марки с алкогольной продукции");
                    return;
                }
                // проверить марку на соответствие маркам в задании
                DaoMem.CheckMarkScannedResultForFindMark markScanned = DaoMem.getDaoMem().checkMarkScannedForFindMark(findMarkRec, barCode);
                if (markScanned == null) {
                    MessageUtils.showToastMessage("Это не искомая марка, попробуйте со следующей бутылки");
                    return;
                }
                // Звуковое оповещение «Тревога»
                MessageUtils.playSound(R.raw.find_mark);
                if (!markScanned.scanned) {
                    // у марки в документе установить признак «найдена»
                    markScanned.recContent.getBaseRecContentMarkList().add(new BaseRecContentMark(barCode,BaseRecContentMark.MARK_SCANNED_AS_MARK, barCode));
                }
                // пересчитать «Количество найдено» по количеству найденных марок
                int cntAll = 0;
                for (FindMarkRecContent recContent : findMarkRec.getFindMarkRecContentList()) {
                    // в каждой позиции пройтись по маркам во входном документе
                    FindMarkContentIn findMarkContentIn = (FindMarkContentIn) recContent.getContentIn();
                    cntAll+=findMarkContentIn.getMark().length;
                }
                int cntScanned = 0;
                for (FindMarkRecContent recContent : findMarkRec.getFindMarkRecContentList()) {
                    cntScanned+=recContent.getBaseRecContentMarkList().size();
                }

                // если по всем позициям «Количество» = «Количество найдено»
                if (cntAll == cntScanned) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Все марки найдены, следуйте инструкции к заданию");
                }
                // если по всем позициям «Количество» > «Количество найдено» - модальное сообщение
                if (cntAll > cntScanned) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Эту марку требовалось найти! Отложите бутылку отдельно и продолжайте поиск других марок");
                }
                DaoMem.getDaoMem().writeLocalDataFindMarkRec(findMarkRec);
                updateData();
                break;
            default:
                MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте марки с алкогольной продукции");
        }

    }

}
