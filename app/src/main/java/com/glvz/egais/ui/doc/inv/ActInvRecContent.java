package com.glvz.egais.ui.doc.inv;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.model.inv.InvRecContentMark;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.PickMRCCallback;
import com.glvz.egais.service.inv.InvRecContentHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.List;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActInvRecContent extends Activity implements BarcodeReader.BarcodeListener, PickMRCCallback {

    public final static int STATE_SCAN_ANY = 1;
    public final static int STATE_SCAN_EAN = 2;

    private InvRecContentHolder invRecContentHolder;
    private TextView tvAction;

    private Button btnAdd;
    private Button btnClear;
    private Button btnNone;
    private EditText edQtyAdd;

    private InvRec invRec;
    private InvRecContent invRecContent;
    private int currentState = STATE_SCAN_ANY;
    private MarkIn scannedMarkIn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invreccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String docId = extras.getString(ActBaseDocRec.REC_DOCID);
        invRec = DaoMem.getDaoMem().getMapInvRec().get(docId);
        String position = extras.getString(ActBaseDocRec.RECCONTENT_POSITION);
        invRecContent = (InvRecContent) DaoMem.getDaoMem().getRecContentByPosition(invRec, position);

        updateData();
        String msg = extras.getString(ActBaseDocRec.RECCONTENT_MESSAGE);
        if (msg != null) {
            MessageUtils.showModalMessage(this, "Внимание!", msg);
        }
    }

    public static InvRecContent fillInvRecContent(InvRec invRec, NomenIn nomenIn, Double mrc) {
        InvRecContent invRecContent = null;
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
            invRecContent = new InvRecContent(String.valueOf(maxPos+1));
            invRecContent.setNomenIn(nomenIn, null);
            invRecContent.setId1c(nomenIn.getId());
            invRecContent.setStatus(BaseRecContentStatus.DONE);
            invRecContent.setManualMrc(mrc);
            invRec.getRecContentList().add(invRecContent);
            DaoMem.getDaoMem().saveDbInvRecContent(invRec, invRecContent);
        } else {
            invRecContent = irc;
        }

        return invRecContent;
    }
    public static Integer findInContentByMark(InvRec invRec, String mark) {
        Integer pos = 0;
        for (InvRecContent recContent : invRec.getInvRecContentList()) {
            pos++;
            for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
                if (mark.equals(baseRecContentMark.getMarkScanned())) {
                    return pos;
                }
            }
        }
        return null;
    }

    public static InvRecContent findOrAddNomen(InvRec invRec, NomenIn nomenIn, MarkIn mark, String barCode) {
        InvRecContent resultRecContent = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (InvRecContent recContent : invRec.getInvRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId())) {
                resultRecContent = recContent;
                break;
            }
            position++;
        }
        if (resultRecContent == null) {
            position++;
            resultRecContent = new InvRecContent(String.valueOf(position));
            resultRecContent.setNomenIn(nomenIn, null);
            invRec.getRecContentList().add(resultRecContent);
        }
        //10) поле «Количество факт» добавить 1 шт к предыдущему значению
        resultRecContent.setQtyAccepted((resultRecContent.getQtyAccepted() == null ? 0 : resultRecContent.getQtyAccepted()) + 1);
        resultRecContent.getBaseRecContentMarkList().add(new InvRecContentMark(mark.getMark(), BaseRecContentMark.MARK_SCANNED_AS_BOX, mark.getMark(), barCode));

        //13) установить статус документа «в работе»
        resultRecContent.setStatus(BaseRecContentStatus.DONE);
        invRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoMem.getDaoMem().saveDbInvRecContent(invRec, resultRecContent);

        return resultRecContent;
    }

    private void fillActWithNomenIdPosition(NomenIn nomenIn, Double mrc) {
        invRecContent = fillInvRecContent(invRec, nomenIn, mrc);
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
        View container = findViewById(R.id.inclRecInvContent);
        invRecContentHolder = new InvRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        edQtyAdd = (EditText) findViewById(R.id.etQtyAdd);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edQtyAdd.getText() != null) {
                    Double qtyAcceptedPrev = invRecContent.getQtyAccepted();
                    boolean soundPlayed = false;
                    try {
                        //1) сохраняет введенное пользователем количество: [Количество факт] = [Количество факт] + [Количество добавить]
                        double qty = 0;
                        try {
                            qty = Double.valueOf(edQtyAdd.getText().toString());
                        } catch (Exception e) {
                            Log.e("ActInvRecContent","Error while parsing the qty", e);
                        }
                        if (qty != 0 ) {
                            invRecContent.setQtyAccepted((invRecContent.getQtyAccepted() == null ? 0 : invRecContent.getQtyAccepted()) + qty);
                            //2) у позиции установить статус «Обработана»
                            invRecContent.setStatus(BaseRecContentStatus.DONE);
                            invRec.setStatus(BaseRecStatus.INPROGRESS);
                            DaoMem.getDaoMem().saveDbInvRecContent(invRec, invRecContent);

                            scannedMarkIn = null;
                            currentState = STATE_SCAN_ANY;
                            edQtyAdd.setText("");
                            if (qty == 1) {
                                MessageUtils.playSound(R.raw.bottle_one);
                            } else {
                                MessageUtils.playSound(R.raw.bottle_many);
                            }
                            soundPlayed = true;
                        }
                    } finally {
                        if (soundPlayed) {
                            try {
                                // проверить что новое факт кол-во записанное - не равно предыдущему
                                InvRecContent invRecContentNew = (InvRecContent) DaoMem.getDaoMem().getRecContentByPosition(invRec, invRecContent.getPosition());
                                if (invRecContentNew == null || invRecContentNew.getQtyAccepted() == null ||
                                        invRecContentNew.getQtyAccepted() - (qtyAcceptedPrev == null ? 0 : qtyAcceptedPrev) == 0) {

                                    MessageUtils.playSound(R.raw.alarm);
                                    MessageUtils.showModalMessage(ActInvRecContent.this, "Внимание!", "Произошла ошибка с сохранением количества по данной позиции! Немедленно обратитесь в ИТ-отдел!");
                                }
                            } catch (Exception e) {
                                Log.e("ActInvRecContent","Error in Catching", e);
                            }

                        }
                    }
                    updateData();
                }
            }
        });

        btnNone = (Button) findViewById(R.id.btnNone);
        btnNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1) Установить «Количество факт» = 0
                invRecContent.setQtyAccepted((double) 0);
                //2) Установить статус позиции «Обработана»
                invRecContent.setStatus(BaseRecContentStatus.DONE);
                DaoMem.getDaoMem().writeLocalDataRecContent_ClearAllMarks(invRec.getDocId(), invRecContent);
                invRecContent.getBaseRecContentMarkList().clear();
                DaoMem.getDaoMem().saveDbInvRecContent(invRec, invRecContent);

                scannedMarkIn = null;
                currentState = STATE_SCAN_ANY;
                updateData();
            }
        });

        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.ShowModalAndConfirm(ActInvRecContent.this, "Внимание!", "Подтвердите очистку текущего товара. Будет удалено «Количество факт» и все сканированные марки",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //   очистить «Количество факт»
                                invRecContent.setQtyAccepted((double) 0);
                                //установить статус «Не обработана»
                                invRecContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
                                //удалить все марки
                                DaoMem.getDaoMem().writeLocalDataRecContent_ClearAllMarks(invRec.getDocId(), invRecContent);
                                invRecContent.getBaseRecContentMarkList().clear();
                                DaoMem.getDaoMem().saveDbInvRecContent(invRec, invRecContent);
                                scannedMarkIn = null;
                                currentState = STATE_SCAN_ANY;
                                updateData();
                            }
                        });

            }
        });

    }

    private void updateData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Текст надписи зависит от текущего статуса
                switch (currentState) {
                    case STATE_SCAN_ANY:
                        tvAction.setText("Сканируйте марку или ШК");
                        break;
                    case STATE_SCAN_EAN:
                        tvAction.setText("Сканируйте ШК товара");
                        break;
                }
                invRecContentHolder.setItem(invRecContent, 0, DocContentArrayAdapter.RECCONTENT_MODE);
                // Недоступна у товаров "NomenType": 1 (маркированный алкоголь)
                if (invRecContent.getNomenIn() == null ||
                        (invRecContent.getNomenIn().getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK && !((InvIn)invRec.getDocIn()).ableDirectInput() )) {
                    btnAdd.setEnabled(false);
                    edQtyAdd.setEnabled(false);
                } else {
                    btnAdd.setEnabled(true);
                    edQtyAdd.setEnabled(true);
                }
                // Кнопка доступна только если у позиции статус «На обработана»
                if (invRecContent.getStatus() == BaseRecContentStatus.NOT_ENTERED) {
                    btnNone.setEnabled(true);
                } else {
                    btnNone.setEnabled(false);
                }

            }
        });
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barCode = barcodeReadEvent.getBarcodeData();
        switch (barCodeType) {
            case EAN8:
            case EAN13:
                NomenIn nomenIn = null;
                if (currentState == STATE_SCAN_EAN) {
                    // ожидание сканирование EAN и определение по нему номенклатуры из «nomen.json» (только среди позиций номенклатуры с "NomenType": 1)
                    nomenIn = DaoMem.getDaoMem().findNomenInAlcoByBarCode(barCode);
                    if (nomenIn == null) {
                        MessageUtils.playSound(R.raw.alarm);
                        MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + barCode + ", не найден. Обратитесь к категорийному менеджеру. Бутылка не будет учтена в фактическом количестве");
                        this.currentState = STATE_SCAN_ANY;
                        this.scannedMarkIn = null;
                        updateData();
                        return;
                    }
                    // если найден,
                    fillActWithNomenIdPosition(nomenIn, null);
                    proceedOneBottle(nomenIn);
                    return;
                }
                // искать товар в nomen.json
                nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(barCode);
                //  Если номенклатура по ШК не найдена — модальное сообщение «», прерывание обработки события
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + barCode + ", не найден. Обратитесь к категорийному менеджеру");
                    return;
                }
                // если найден, то в соответствии с NomenType:
                switch (nomenIn.getNomenType()) {
                    case NomenIn.NOMENTYPE_ALCO_MARK:
                        if (!((InvIn)invRec.getDocIn()).ableDirectInput()) {
                            MessageUtils.showModalMessage(this, "Внимание!", "Маркированный алкоголь. Необходимо сканировать марку");
                        } else {
                            // по NomenID искать товарную позицию документа и открыть ее карточку (если такой не было: добавить и открыть)
                            fillActWithNomenIdPosition(nomenIn, null);
                        }
                        break;
                    case NomenIn.NOMENTYPE_ALCO_OTHER:
                    case NomenIn.NOMENTYPE_ALCO_NOMARK:
                        // по NomenID искать товарную позицию документа и открыть ее карточку (если такой не было: добавить и открыть)
                        fillActWithNomenIdPosition(nomenIn, null);
                        break;
                    case NomenIn.NOMENTYPE_ALCO_TOBACCO:
                        // вывести список МРЦ (из записи nomen.json) для выбора пользователем
                        // после выбора пользователем МРЦ, по NomenID и МРЦ искать товарную позицию документа и открыть ее карточку (если такой не было: добавить и открыть)
                        ActInvRec.chooseMRC(this, nomenIn, nomenIn.getMcArr(), this);
                        break;
                }
                break;
            case PDF417:
            case DATAMATRIX:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод");
                    return;
                }
                if (((InvIn)invRec.getDocIn()).ableDirectInput()) {
                    MessageUtils.playSound(R.raw.scan_ean_inv);
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
                // искать марку среди ранее сканированных во всех позициях документа
                DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(invRec, barCode);
                if (markScanned != null) {
                    // Если марка найдена — открыть товарную позицию
                    fillActWithNomenIdPosition(markScanned.recContent.getNomenIn(), null);
                    MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                    return;
                }
                // выполнить проверку допустимости добавления этой марки, типы проверяемых марок зависят от состояния CheckMark в справочнике магазинов shops.json:
                //- «DataMatrix» - проверяются только марки DataMatrix (проверка PDF417 - пропускается)
                //- «DataMatrixPDF417» - проверяются марки DataMatrix и PDF417
                MarkIn markIn = null;
                if (DaoMem.getDaoMem().isNeedToCheckMark(invRec.getInvIn().getCheckMark(), barCodeType)) {
                    //
                    // алгоритм допустимости добавления марки
                    //
                    // искать марку в справочнике «marks.json»
                    markIn = DaoMem.getDaoMem().findMarkByBarcode(barCode);
                    if (markIn == null) {
                        // если не найдена: модальное сообщение
                        MessageUtils.showModalMessage(this, "Внимание!", "Марка не состоит на учете в магазине. Отложите эту бутылку для постановки на баланс. Бутылка будет учтена в фактическом наличии, но выставлять ее на продажу нельзя.\n\nТеперь сканируйте штрихкод с этой же бутылки для сопоставления с номенклатурой.");
                        // создаем фейковую марку
                        markIn = new MarkIn();
                        markIn.setMark(barCode);
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
                    // у марок старого типа (PDF417) отключить идентификацию номенклатуры по справочнику alccodes.json
                    if (barCodeType != PDF417) {
                        // искать алкокод в справочнике «alccodes.json». Если найден -  сохранить значение NomenID из записи с алкокодом
                        AlcCodeIn alcCodeIn = DaoMem.getDaoMem().findAlcCode(markIn.getAlcCode());
                        if (alcCodeIn != null) {
                            markIn.setNomenId(alcCodeIn.getNomenId());
                        }
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
                NomenIn nomenIn2 = DaoMem.getDaoMem().findNomenInAlcoByNomenId(markIn.getNomenId());
                if (nomenIn2 == null) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Товар по марке " + barCode + ", не найден. Обратитесь к категорийному менеджеру");
                    return;
                }
                fillActWithNomenIdPosition(nomenIn2, null);
                proceedOneBottle(nomenIn2);
                break;
            case CODE128:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод, с той же бутылки с которой только что сканировали марку");
                    return;
                }
                if (((InvIn)invRec.getDocIn()).ableDirectInput()) {
                    MessageUtils.playSound(R.raw.scan_ean_inv);
                    return;
                }
                // в справочнике marks.json найти все марки с ШК коробки, соответствующей сканированной
                List<MarkIn> marksInBox = DaoMem.getDaoMem().findMarksByBoxBarcode(barCode);
                if (marksInBox.size() == 0) {
                    // если ни одной марки не найдено вывести модальное сообщение: «По ШК коробки #BoxBarcode# марки не найдены». Прервать обработку.
                    MessageUtils.showModalMessage(this, "Внимание!", "По ШК коробки "+barCode+" марки не найдены");
                    break;
                }
                // Обнулить переменные «Числится марок в текущей коробке», «Количество, добавленное по текущей коробке»
                int marksInCurrentBox = 0;
                int qtyAddedCurrentBox = 0;
                Integer position = null;
                NomenIn foundNomenIn = null;
                InvRecContent row = null;
                // для каждой найденной марки выполнить обработку
                for (MarkIn mark : marksInBox) {
                    // Увеличить на 1 переменную «Числится марок в текущей коробке»
                    marksInCurrentBox++;
                    // 4.2 из найденной записи марки извлечь номенклатуру, выполнить поиск в товарной части документа.
                    // Уникальный ключ для поиска записей в документе - «NomenID»
                    // Если записи товара в документе не найдено — добавить новую.
                    foundNomenIn = DaoMem.getDaoMem().findNomenInByNomenId(mark.getNomenId());
                    if (foundNomenIn == null) {
                        continue;
                    }
                    // проверить наличие текущей марки среди ранее сканированных в документе. При наличии — пропустить обработку марки
                    position = ActInvRecContent.findInContentByMark(invRec, mark.getMark());
                    if (position != null) {
                        continue;
                    }
                    row = ActInvRecContent.findOrAddNomen(invRec, foundNomenIn, mark, barCode);
                    position = Integer.parseInt(row.getPosition());
                    // увеличить на 1 переменную «Количество, добавленное по текущей коробке»
                    qtyAddedCurrentBox++;
                }
                this.currentState = STATE_SCAN_ANY;
                this.scannedMarkIn = null;
                if (position != null && foundNomenIn != null) {
                    fillActWithNomenIdPosition(foundNomenIn, null);
                    MessageUtils.showModalMessage(this, "Внимание!", "В коробке " + barCode + " числится номенклатура " + foundNomenIn.getName() + " (" + foundNomenIn.getId() + ") с учетным количеством " + marksInCurrentBox + " марок." +
                            " Количество марок, добавленное в документ " + qtyAddedCurrentBox + " шт.");
                    updateData();
                }
                if (foundNomenIn != null) {
                    // 6 Информировать пользователя, о результате сканирования ШК коробки:
                    //6.1 в зависимости от значения переменной «Количество, добавленное по текущей коробке»:
                    //- 0: звуковые файлы не проигрывать
                    //- 1: проиграть звуковой файл «bottle_one.mp3»
                    if (qtyAddedCurrentBox == 1) {
                        MessageUtils.playSound(R.raw.bottle_one);
                    }
                    //- более 1: проиграть звуковой файл «bottle_many.mp3»
                    if (qtyAddedCurrentBox > 1) {
                        MessageUtils.playSound(R.raw.bottle_many);
                    }
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

    public static void proceedOneBottle(Activity activity, InvRec invRec, InvRecContent invRecContent, NomenIn nomenIn, MarkIn scannedMarkIn) {
        Double qtyAcceptedPrev = invRecContent.getQtyAccepted();
        boolean soundPlayed = false;
        try {
            invRecContent.setNomenIn(nomenIn, null);
            //10) поле «Количество факт» добавить addQty шт к предыдущему значению
            invRecContent.setQtyAccepted((invRecContent.getQtyAccepted() == null ? 0 : invRecContent.getQtyAccepted()) + 1);
            //11) добавить марку к списку марок текущей позиции.
            if (scannedMarkIn != null) {
                invRecContent.getBaseRecContentMarkList().add(new InvRecContentMark(scannedMarkIn.getMark(), BaseRecContentMark.MARK_SCANNED_AS_MARK, scannedMarkIn.getMark(), null));
            }
            //13) установить статус документа «в работе»
            invRecContent.setStatus(BaseRecContentStatus.DONE);
            invRec.setStatus(BaseRecStatus.INPROGRESS);
            DaoMem.getDaoMem().saveDbInvRecContent(invRec, invRecContent);
            //12) проиграть файл «bottle_one.mp3» - проигрываем файл в самом конце после успешного сохранения записи
            MessageUtils.playSound(R.raw.bottle_one);
            soundPlayed = true;
        } finally {
            if (soundPlayed) {
                try {
                    // проверить что новое факт кол-во записанное - не равно предыдущему
                    InvRecContent invRecContentNew = (InvRecContent) DaoMem.getDaoMem().getRecContentByPosition(invRec, invRecContent.getPosition());
                    if (invRecContentNew == null || invRecContentNew.getQtyAccepted() == null ||
                            invRecContentNew.getQtyAccepted() - (qtyAcceptedPrev == null ? 0 : qtyAcceptedPrev) == 0) {

                        MessageUtils.playSound(R.raw.alarm);
                        MessageUtils.showModalMessage(activity, "Внимание!", "Произошла ошибка с сохранением количества по данной позиции! Немедленно обратитесь в ИТ-отдел!");
                    }
                } catch (Exception e) {
                    Log.e("ActInvRecContent","Error in Catching", e);
                }

            }

        }

    }

    private void proceedOneBottle(NomenIn nomenIn) {
        proceedOneBottle(this, invRec, invRecContent, nomenIn, scannedMarkIn);
        this.currentState = STATE_SCAN_ANY;
        this.scannedMarkIn = null;
        updateData();
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onSelectMRCCallback(NomenIn nomenIn, Double mrc) {
        // переход к карточке этой строки
        fillActWithNomenIdPosition(nomenIn, mrc);
    }

    @Override
    public void onCancelMRCCallback() {
        this.currentState = STATE_SCAN_ANY;
        this.scannedMarkIn = null;
        updateData();
        finish();
    }
}
