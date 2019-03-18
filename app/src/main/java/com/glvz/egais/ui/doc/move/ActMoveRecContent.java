package com.glvz.egais.ui.doc.move;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.move.MoveRecContentHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActMoveRecContent extends Activity implements BarcodeReader.BarcodeListener {

    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    private MoveRecContentHolder moveRecContentHolder;
    private TextView tvAction;

    private Button btnBack;
    private Button btnNext;

    private MoveRec moveRec;
    private MoveRecContent moveRecContent;
    private int currentState = STATE_SCAN_MARK;
    private MarkIn scannedMarkIn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movereccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String docId = extras.getString(ActBaseDocRec.REC_DOCID);
        moveRec = DaoMem.getDaoMem().getMapMoveRec().get(docId);

        String position = extras.getString(ActBaseDocRec.RECCONTENT_POSITION);
        moveRecContent = (MoveRecContent) DaoMem.getDaoMem().getRecContentByPosition(moveRec, position);

        updateData();

    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.setCurrentListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
    }

    private void setResources() {
        View container = findViewById(R.id.inclRecMoveContent);
        moveRecContentHolder = new MoveRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возврат к форме «Карточка задания на перемещение».
                ActMoveRecContent.this.finish();
            }
        });
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Если «Количество факт» = 0, то запросить подтверждение
                if (moveRecContent.getQtyAccepted() == null || moveRecContent.getQtyAccepted() == 0) {
                    MessageUtils.ShowModalAndConfirm(ActMoveRecContent.this, "Внимание!", "«Количество факт не установлено. Если товар для перемещения отсутствует — нажмите Да. Для возврата к вводу количества — нажмите Нет.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    proceedNext();
                                }
                            });

                } else {
                    proceedNext();
                }
            }
        });
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.ShowModalAndConfirm(ActMoveRecContent.this, "Внимание!", "Подтвердите действие: у текущей позиции фактическое количество и все сканированные марки будут удалены",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //  «Количество факт» = 0; в позиции удалить все марки; статус задания = «в работе».
                                moveRecContent.setQtyAccepted((double) 0);
                                moveRecContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
                                moveRecContent.getBaseRecContentMarkList().clear();
                                moveRec.setStatus(BaseRecStatus.INPROGRESS);
                                DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
                                scannedMarkIn = null;
                                currentState = STATE_SCAN_MARK;
                                updateData();
                            }
                        });

            }
        });

    }

    private void proceedNext() {
        if (moveRecContent.getQtyAccepted() == null || moveRecContent.getQtyAccepted() == 0) {
            moveRecContent.setQtyAccepted((double) 0);
        }
        //  Текущую позицию отметить как выполненную
        moveRecContent.setStatus(BaseRecContentStatus.DONE);
        DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
        // искать следующую не выполненную товарную позицию задания
        MoveRecContent nextRecContent = (MoveRecContent) moveRec.tryGetNextRecContent();
        if (nextRecContent != null) {
            // если найдена: обновить данные текущей формы найденной позицией
            this.moveRecContent = nextRecContent;
            this.currentState = STATE_SCAN_MARK;
            this.scannedMarkIn = null;
            updateData();
        } else {
            // если не найдена: статус задания = Выполнено, перейти к форме «Карточка задания на расход»
            moveRec.setStatus(BaseRecStatus.DONE);
            DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
            this.finish();
        }
    }

    private void updateData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Текст надписи зависит от текущего статуса
                switch (currentState) {
                    case STATE_SCAN_MARK:
                        tvAction.setText("Сканируйте марку");
                        break;
                    case STATE_SCAN_EAN:
                        tvAction.setText("Сканируйте ШК товара");
                        break;
                }
                moveRecContentHolder.setItem(moveRecContent, 0, DocContentArrayAdapter.RECCONTENT_MODE);

            }
        });
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barCode = barcodeReadEvent.getBarcodeData();
        MoveRecContent moveRecContentLocal;
        switch (barCodeType) {
            case EAN13:
                // 1) Если форма находилась в режиме ожидания сканирования ШК PDF-417 (DataMatrix): вывести модальное сообщение , прервать обработку события.
                if (currentState == STATE_SCAN_MARK) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте марку с " + moveRecContent.getNomenIn().getName());
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
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод с " + moveRecContent.getNomenIn().getName());
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
                Integer markScanned = DaoMem.getDaoMem().checkMarkScanned(moveRec, barCode);
                if (markScanned != null && markScanned == BaseRecContentMark.MARK_SCANNED_AS_MARK) {
                    // Если марка найдена — модальное сообщение «», прервать обработку события
                    MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + moveRecContent.getPosition() + " товара " + moveRecContent.getNomenIn().getName());
                    return;
                }
                // выполнить проверку допустимости добавления этой марки, типы проверяемых марок зависят от состояния CheckMark в справочнике магазинов shops.json:
                //- «DataMatrix» - проверяются только марки DataMatrix (проверка PDF417 - пропускается)
                //- «DataMatrixPDF417» - проверяются марки DataMatrix и PDF417
                if (!DaoMem.getDaoMem().isAlowedBarcode(barCodeType)) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Эта марка не допустима для сканирования в этом магазине!");
                    return;
                }
                //
                // алгоритм допустимости добавления марки
                //
                // искать марку в справочнике «marks.json»
                MarkIn markIn = DaoMem.getDaoMem().findMarkByBarcode(barCode);
                if (markIn == null) {
                    // если не найдена: модальное сообщение , прерывание обработки события.
                    MessageUtils.showModalMessage(this, "Внимание!", "Марка не состоит на учете в магазине. Перемещение невозможно. Отложите эту бутылку для постановки на баланс и сканируйте другую!");
                    return;
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
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте штрихкод с " + moveRecContent.getNomenIn().getName());
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте марку с " + moveRecContent.getNomenIn().getName());
                }
        }

    }

    private void proceedOneBottle(NomenIn nomenIn) {
        // 9) проверить соответствие определенного NomenID текущей товарной позиции:
        if (!nomenIn.getId().equals(moveRecContent.getNomenIn().getId())) {
            //9.1) если не соответствует: модальное сообщение «», прерывание обработки события.
            MessageUtils.showModalMessage(this, "Внимание!", "Сканированная номенклатура "+nomenIn.getName()+" не соответствует текущей позиции на перемещение, Эту бутылку перемещать не надо. Сканируйте марку с "+moveRecContent.getNomenIn().getName()+"!");
            return;
        }
        //10) поле «Количество факт» добавить 1 шт к предыдущему значению
        moveRecContent.setQtyAccepted(moveRecContent.getQtyAccepted() + 1);
        //11) добавить марку к списку марок текущей позиции.
        moveRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(scannedMarkIn.getMark(), BaseRecContentMark.MARK_SCANNED_AS_MARK, scannedMarkIn.getMark()));
        //12) проиграть файл «bottle_one.mp3»
        MessageUtils.playSound(R.raw.bottle_one);
        //13) установить статус документа «в работе»
        moveRecContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
        moveRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
        this.currentState = STATE_SCAN_MARK;
        this.scannedMarkIn = null;
        updateData();

    }



    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
