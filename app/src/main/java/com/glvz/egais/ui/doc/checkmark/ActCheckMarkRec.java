package com.glvz.egais.ui.doc.checkmark;

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
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.model.checkmark.CheckMarkRecContent;
import com.glvz.egais.model.checkmark.CheckMarkRecContentMark;
import com.glvz.egais.service.checkmark.CheckMarkContentArrayAdapter;
import com.glvz.egais.service.checkmark.CheckMarkRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActCheckMarkRec extends ActBaseDocRec {
    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    CheckMarkRec checkMarkRec;
    TextView tvCaption;

    private int currentState = STATE_SCAN_MARK;
    private MarkIn scannedMarkIn = null;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.checkMarkRec = DaoMem.getDaoMem().getMapCheckMarkRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_checkmarkrec);

        View container = findViewById(R.id.inclRecCheckMark);
        docRecHolder = new CheckMarkRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        adapter = new CheckMarkContentArrayAdapter(this, R.layout.rec_checkmark_position, list);
        lvContent.setAdapter(adapter);
        tvCaption = (TextView)findViewById(R.id.tvCaption);


    }

    @Override
    protected void updateData() {
        updateDataWithScroll(null);
    }

    private void updateDataWithScroll(final Integer position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                docRecHolder.setItem(checkMarkRec);
                // Достать список позиций по накладной
                Collection<CheckMarkRecContent> newList = DaoMem.getDaoMem().getCheckMarkRecContentList(checkMarkRec.getDocId());
                list.clear();
                list.addAll(newList);
                adapter.notifyDataSetChanged();
                if (position != null) {
                    lvContent.smoothScrollToPosition(position);
                }
                switch (currentState) {
                    case STATE_SCAN_MARK:
                        tvCaption.setText("Сканируйте марку нового образца с товаров по заданию");
                        break;
                    case STATE_SCAN_EAN:
                        tvCaption.setText("Сканируйте штрихкод");
                        break;
                }
            }
        });
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_checkmarkrec, menu);
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
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление марок, собранных по заданию",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(checkMarkRec);
                                currentState = STATE_SCAN_MARK;
                                MessageUtils.showToastMessage("Данные по заданию удалены!");
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
        boolean success = DaoMem.getDaoMem().exportData(checkMarkRec);
        if (success) {
            MessageUtils.showToastMessage("Задание выгружено!");
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
        CheckMarkRecContent checkMarkRecContentLocal;
        switch (barCodeType) {
            case EAN8:
            case EAN13:
                // 1) Если форма находилась в режиме ожидания сканирования ШК DataMatrix: вывести модальное сообщение , прервать обработку события.
                if (currentState == STATE_SCAN_MARK) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте марку нового образца");
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
                    // Проверить его наличие в задании
                    CheckMarkRecContent recContent = null;
                    for (BaseRecContent rc : checkMarkRec.getRecContentList()) {
                        if (rc.getNomenIn().getId().equals(nomenIn.getId())) {
                            recContent = (CheckMarkRecContent) rc;
                            break;
                        }
                    }
                    if (recContent == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Данный товар "+nomenIn.getName()+" отсутствует в задании на поиск марки. Убедитесь что вы сканировали правильный товар или штрихкод с него. Повторите действие со сканирования марки");
                        currentState = STATE_SCAN_MARK;
                        updateData();
                        return;
                    }

                    proceedOneBottle_New(recContent, scannedMarkIn, nomenIn);
                    return;
                }
                break;
            case DATAMATRIX:
            case PDF417:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод с бутылки");
                    return;
                }
                // выполнить проверку корректности ШК по длине:  PDF-417 должна быть 68 символов,  DataMatrix – 150
                if (barCodeType == DATAMATRIX && barCode.length() != 150) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина считанного штрихкода ("+ barCode.length()+"). Сканируйте марку нового образца");
                    return;
                }
                if (barCodeType == PDF417 && barCode.length() != 68) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 68, фактически " + barCode.length());
                    return;
                }
                // Проверить наличие считанного ШК среди всех марок задания (по всем позициям)
                // Проверить что этот ШК ранее не сканировался в данной ТТН
                DaoMem.CheckMarkScannedResultForCheckMark markScanned = DaoMem.getDaoMem().checkMarkScannedForCheckMark(checkMarkRec, barCode);
                // Если у этой марки признак наличия «На учете, наличие не подтверждено»
                if (markScanned != null && markScanned.state == CheckMarkRecContentMark.CHECKMARK_MARKSTATE_NOTCONFIRMED) {
                    // установить по ней признак «На учете, наличие подтверждено»;
                    // у позиции (которой принадлежит марка) увеличить «Количество факт» на 1
                    proceedOneBottle_Confirmed((CheckMarkRecContent) markScanned.recContent, barCode);
                    return;
                }
                // Если у этой марки признак наличия «На учете, наличие подтверждено» или «Выявлена неучтенная марка»
                if (markScanned != null && (markScanned.state == CheckMarkRecContentMark.CHECKMARK_MARKSTATE_CONFIRMED || markScanned.state == CheckMarkRecContentMark.CHECKMARK_MARKSTATE_NEWMARK)) {
                    MessageUtils.showToastMessage("Эта марка уже сканировалась ранее");
                    return;
                }
                //  Если марка не найдена ни в одной позиции задания
                if (markScanned == null) {
                    // Звуковое оповещение «Тревога»
                    MessageUtils.playSound(R.raw.check_mark_new);
                    // запомнить марку
                    scannedMarkIn = new MarkIn();
                    scannedMarkIn.setMark(barCode);
                    // Подсказку изменить на «Сканируйте штихкод»
                    this.currentState = STATE_SCAN_EAN;
                    MessageUtils.showModalMessage(this, "Внимание!", "Марка отсутствует в задании. Для определения номенклатуры нажмите ОК и сканируйте штрихкод с этой же бутылки");
                    updateData();
                    return;
                }
                break;
            default:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте штрихкод");
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте марку");
                }
        }

    }

    private void proceedOneBottle_Confirmed(CheckMarkRecContent recContent, String barCode) {
        // установить по ней признак «На учете, наличие подтверждено»;
        recContent.getBaseRecContentMarkList().add(new CheckMarkRecContentMark(barCode, BaseRecContentMark.MARK_SCANNED_AS_MARK, barCode, CheckMarkRecContentMark.CHECKMARK_MARKSTATE_CONFIRMED));
        // у позиции (которой принадлежит марка) увеличить «Количество факт» на 1
        recContent.setQtyAccepted((recContent.getQtyAccepted() == null ? 0 : recContent.getQtyAccepted()) + 1);
        // установить статус документа «в работе»
        recContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
        checkMarkRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataCheckMarkRec(checkMarkRec);
        MessageUtils.playSound(R.raw.bottle_one);
        updateData();
    }

    private void proceedOneBottle_New(CheckMarkRecContent recContent, MarkIn scannedMarkIn, NomenIn nomenIn) {
        // добавить марку (сканированную ранее) с признаком «Выявлена неучтенная марка»
        recContent.getBaseRecContentMarkList().add(new CheckMarkRecContentMark(scannedMarkIn.getMark(), BaseRecContentMark.MARK_SCANNED_AS_MARK, scannedMarkIn.getMark(), CheckMarkRecContentMark.CHECKMARK_MARKSTATE_NEWMARK));
        // у позиции «Количество новых факт» увеличить на 1
        recContent.setQtyAcceptedNew((recContent.getQtyAcceptedNew() == null ? 0 : recContent.getQtyAcceptedNew()) + 1);
        // подсказку заменить на «Сканируйте марку нового образца с товаров по заданию»
        currentState = STATE_SCAN_MARK;

        recContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
        checkMarkRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataCheckMarkRec(checkMarkRec);
        updateData();

        MessageUtils.showModalMessage(this, "Внимание!", "Выявлена неучтенная марка. Отложите бутылку, продавать на кассе ее нельзя до особого указания. Продолжайте сканированием следующей марки.");
        MessageUtils.playSound(R.raw.bottle_one);

    }


}
