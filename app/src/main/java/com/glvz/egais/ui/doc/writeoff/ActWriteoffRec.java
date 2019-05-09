package com.glvz.egais.ui.doc.writeoff;

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
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.service.writeoff.WriteoffContentArrayAdapter;
import com.glvz.egais.service.writeoff.WriteoffRecHolder;
import com.glvz.egais.ui.ActCommentEdit;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActWriteoffRec extends ActBaseDocRec {

    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    private final static int COMMENT_RETCODE = 1;

    private int currentState = STATE_SCAN_MARK;
    private MarkIn scannedMarkIn = null;
    private WriteoffRecContent writeoffRecContentLocal;

    private WriteoffRec writeoffRec;
    Button btnChangeComment;
    TextView tvCaption;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.writeoffRec = DaoMem.getDaoMem().getMapWriteoffRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_writeoffrec);

        View container = findViewById(R.id.inclRecWriteoff);
        docRecHolder = new WriteoffRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        btnChangeComment = (Button)findViewById(R.id.btnChangeComment);
        btnChangeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открыть форму с редактироанием комментария
                Intent i = new Intent(ActWriteoffRec.this, ActCommentEdit.class);
                i.putExtra(ActCommentEdit.COMMENT_VALUE, writeoffRec.getComment());
                startActivityForResult(i, COMMENT_RETCODE);
            }
        });

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActWriteoffRec.this, writeoffRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new WriteoffContentArrayAdapter(this, R.layout.rec_writeoff_position, list);
        lvContent.setAdapter(adapter);
        tvCaption = (TextView)findViewById(R.id.tvCaption);

    }

    @Override
    protected void updateData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                docRecHolder.setItem(writeoffRec);
                // Достать список позиций по накладной
                Collection<WriteoffRecContent> newList = DaoMem.getDaoMem().getWriteoffRecContentList(writeoffRec.getDocId());
                list.clear();
                list.addAll(newList);
                adapter.notifyDataSetChanged();
                switch (currentState) {
                    case STATE_SCAN_MARK:
                        tvCaption.setText("Сканиуйте марку");
                    case STATE_SCAN_EAN:
                        tvCaption.setText("Сканиуйте штрихкод");
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent i){
        if(requestCode == COMMENT_RETCODE ){
            if(resultCode==RESULT_OK){
                writeoffRec.setComment(i.getData().toString());
                DaoMem.getDaoMem().writeLocalWriteoffRec(writeoffRec);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_writeoffrec, menu);
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
            case R.id.action_clear:
                // - запрос на подтверждение очистки «Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу» Да/Нет
                //- удаляются данные о фактически собранном количестве и маркам по всему документу
                //- статус задания устанавливается в «новый»
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(writeoffRec);
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
        // - проверить, что все строки задания выполнены (или статус задания — завершено). Если проверка не пройдена — модальное сообщение
        //   «Задание еще не выполнено, выгрузка невозможна» и завершение обработки.
            //- собранные данные выгружаются в JSON-файл во внутреннюю память терминала в каталог «GLVZ\Shops\#ShopID#\Out»
            boolean success = DaoMem.getDaoMem().exportData(writeoffRec);
            if (success) {
                MessageUtils.showToastMessage("Накладная выгружена!");
                updateData();
                syncDoc();
            } else {
                MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
            }
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barCode = barcodeReadEvent.getBarcodeData();
        switch (barCodeType) {
            case EAN13:
                // 1) Если форма находилась в режиме ожидания сканирования ШК PDF-417 (DataMatrix): вывести модальное сообщение , прервать обработку события.
                if (currentState == STATE_SCAN_MARK) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте марку с " + writeoffRecContentLocal.getNomenIn().getName());
                    return;
                }
                if (currentState == STATE_SCAN_EAN) {
                    // определение по нему номенклатуры из «nomen.json» (только среди позиций номенклатуры с "NomenType": 1)
                    NomenIn nomenIn = DaoMem.getDaoMem().findNomenInAlcoByBarCode(barCode);
                    //  Если номенклатура по ШК не найдена — модальное сообщение «», прерывание обработки события
                    if (nomenIn == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Номенклатура по штрихкоду " + barCode + ", не найдена. Обратитесь к категорийному менеджеру");
                        return;
                    }
                    proceedOneBottle(nomenIn);
                    return;
                }
                break;
            case PDF417:
            case DATAMATRIX:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод с " + writeoffRecContentLocal.getNomenIn().getName());
                    return;
                }
                // выполнить проверку корректности ШК по длине:  PDF-417 должна быть 68 символов,  DataMatrix – 150
                if (barCodeType == PDF417 && barCode.length() != 68) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 68, фактически " + barCode.length());
                    return;
                }
                if (barCodeType == DATAMATRIX && barCode.length() != 150) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 150, фактически " + barCode.length());
                    return;
                }
                // Проверить наличие этой марки среди ранее сохраненных марок всех товарных позиций этого задания.
                // Проверить что этот ШК ранее не сканировался в данной ТТН
                DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(writeoffRec, barCode);
                if (markScanned != null && markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK) {
                    // Если марка найдена — модальное сообщение «», прервать обработку события
                    MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                    return;
                }
                //- для списания — всегда «DataMatrix»
                // - для Возврата - выполнить проверку допустимости добавления этой марки, типы проверяемых марок зависят от состояния CheckMark в справочнике магазинов shops.json:
                //   - «DataMatrix» - проверяются только марки DataMatrix (проверка PDF417 - пропускается)
                //   - «DataMatrixPDF417» - проверяются марки DataMatrix и PDF417
                MarkIn markIn = null;
                if (DaoMem.getDaoMem().isNeedToCheckMarkForWriteoff(writeoffRec.getTypeDoc(), barCodeType)) {
                    //
                    // алгоритм допустимости добавления марки
                    //
                    // искать марку в справочнике «marks.json»
                    markIn = DaoMem.getDaoMem().findMarkByBarcode(barCode);
                    if (markIn == null) {
                        // если не найдена: модальное сообщение , прерывание обработки события.
                        MessageUtils.showModalMessage(this, "Внимание!", "Марка не состоит на учете в магазине. Перемещение невозможно. Отложите эту бутылку для постановки на баланс и сканируйте другую!");
                        return;
                    }
                } else {
                    // создаем фейковую марку
                    markIn = new MarkIn();
                    markIn.setMark(barCode);
                }
                this.scannedMarkIn = markIn;
                // если NomenID не определен (может быть определен на предыдущих шагах) — попытка определения по справочнику «alccodes.json»
                if (StringUtils.isEmptyOrNull(markIn.getNomenId())) {
                    // Если AlcCode не определен, то:
                    //- для марок DataMatrix: пропустить этап определения по справочнику «alccodes.json»
                    //- для марок PDF417: декодировать текст марки в алкокод
                    if (barCodeType == PDF417 && StringUtils.isEmptyOrNull(markIn.getAlcCode())) {
                        markIn.setAlcCode(BarcodeObject.extractAlcode(barCode));
                    }
                    // искать алкокод в справочнике «alccodes.json». Если найден -  сохранить значение NomenID из записи с алкокодом
                    AlcCodeIn alcCodeIn = DaoMem.getDaoMem().findAlcCode(markIn.getAlcCode());
                    if (alcCodeIn != null) {
                        markIn.setNomenId(alcCodeIn.getNomenId());
                    }
                }
                // если NomenID не определен
                if (StringUtils.isEmptyOrNull(markIn.getNomenId())) {
                    // 8.1) подсказку изменить на «Сканируйте штрихкод»
                    this.currentState = STATE_SCAN_EAN;
                    // 8.2) Звуковое сообщение «Сканируйте штрихкод»
                    MessageUtils.playSound(R.raw.scan_ean);
                    updateData();
                    return;
                }
                NomenIn nomenIn = DaoMem.getDaoMem().findNomenInAlcoByNomenId(markIn.getNomenId());
                proceedOneBottle(nomenIn);
                break;
            default:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте штрихкод");
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте марку" );
                }
        }

    }

    private void proceedOneBottle(NomenIn nomenIn) {
        writeoffRecContentLocal = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId())) {
                writeoffRecContentLocal = recContent;
            }
            position++;
        }
        if (writeoffRecContentLocal == null) {
            writeoffRecContentLocal = new WriteoffRecContent(String.valueOf(position+1), null);
            writeoffRecContentLocal.setNomenIn(nomenIn, null);
            writeoffRec.getRecContentList().add(writeoffRecContentLocal);
        }
        //10) поле «Количество факт» добавить 1 шт к предыдущему значению
        writeoffRecContentLocal.setQtyAccepted((writeoffRecContentLocal.getQtyAccepted() == null ? 0 : writeoffRecContentLocal.getQtyAccepted()) + 1);
        //11) добавить марку к списку марок текущей позиции.
        writeoffRecContentLocal.getBaseRecContentMarkList().add(new BaseRecContentMark(scannedMarkIn.getMark(), BaseRecContentMark.MARK_SCANNED_AS_MARK, scannedMarkIn.getMark()));
        //12) проиграть файл «bottle_one.mp3»
        MessageUtils.playSound(R.raw.bottle_one);
        //13) установить статус документа «в работе»
        writeoffRecContentLocal.setStatus(BaseRecContentStatus.IN_PROGRESS);
        writeoffRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalWriteoffRec(writeoffRec);
        this.currentState = STATE_SCAN_MARK;
        this.scannedMarkIn = null;
        updateData();
        // 9.0) желательно как-то выделить эту позицию для пользователя
        lvContent.smoothScrollToPosition(position);
    }


    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }


    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // no action
    }
}
