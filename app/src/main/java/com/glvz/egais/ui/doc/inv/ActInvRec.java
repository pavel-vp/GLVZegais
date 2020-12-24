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
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.model.inv.InvRecContentMark;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.model.writeoff.WriteoffRecContentMark;
import com.glvz.egais.service.PickMRCCallback;
import com.glvz.egais.service.inv.InvContentArrayAdapter;
import com.glvz.egais.service.inv.InvRecHolder;
import com.glvz.egais.ui.ActEnterNomenId;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.*;

import static com.glvz.egais.ui.doc.inv.ActInvRecContent.STATE_SCAN_ANY;
import static com.glvz.egais.ui.doc.inv.ActInvRecContent.STATE_SCAN_EAN;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActInvRec extends ActBaseDocRec implements PickMRCCallback{

    private static final int ENTERNOMENID_RETCODE = 1;
    private InvRec invRec;
    private TextView tvCaption;
    private int currentState = STATE_SCAN_ANY;
    private MarkIn scannedMarkIn = null;
    private String message = null;

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
        updateCaption();
    }

    private void updateCaption() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Текст надписи зависит от текущего статуса
                switch (currentState) {
                    case STATE_SCAN_ANY:
                        tvCaption.setText("Сканируйте марку или ШК");
                        break;
                    case STATE_SCAN_EAN:
                        tvCaption.setText("Сканируйте ШК товара");
                        break;
                }
            }
        });
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
                MessageUtils.showToastMessage("Документ выгружен!");
                updateData();
                syncDoc();
            } else {
                MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCaption();
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // Открыть форму строки
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActInvRecContent.class);
        in.putExtra(ActInvRec.REC_DOCID, docId);
        in.putExtra(ActInvRec.RECCONTENT_POSITION, req.getPosition().toString());
        in.putExtra(ActInvRec.RECCONTENT_MESSAGE, message);
        ctx.startActivity(in);
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        message = null;
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
                        updateCaption();
                        return;
                    }
                    // если найден,
                    InvRecContent irc = ActInvRecContent.fillInvRecContent(invRec, nomenIn, null);
                    proceedOneBottle(irc, nomenIn);
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
                            InvRecContent irc = ActInvRecContent.fillInvRecContent(invRec, nomenIn, null);
                            pickRec(this, invRec.getDocId(), irc, 0, null, false, false);
                        }
                        break;
                    case NomenIn.NOMENTYPE_ALCO_OTHER:
                    case NomenIn.NOMENTYPE_ALCO_NOMARK:
                        // по NomenID искать товарную позицию документа и открыть ее карточку (если такой не было: добавить и открыть)
                        InvRecContent irc = ActInvRecContent.fillInvRecContent(invRec, nomenIn, null);
                        pickRec(this, invRec.getDocId(), irc, 0, null, false, false);
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
                    InvRecContent irc = ActInvRecContent.fillInvRecContent(invRec, markScanned.recContent.getNomenIn(), null);
                    message = "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName();
                    pickRec(this, invRec.getDocId(), irc, 0, null, false, false);
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
                    updateCaption();
                    return;
                }
                NomenIn nomenIn2 = DaoMem.getDaoMem().findNomenInAlcoByNomenId(markIn.getNomenId());
                if (nomenIn2 == null) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Товар по марке " + barCode + ", не найден. Обратитесь к категорийному менеджеру");
                    return;
                }
                InvRecContent irc = ActInvRecContent.fillInvRecContent(invRec, nomenIn2, null);
                proceedOneBottle(irc, nomenIn2);
                break;
            case CODE128:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод, с той же бутылки с которой только что сканировали марку");
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
                    row = ActInvRecContent.fillInvRecContent(invRec, foundNomenIn, null);
                    message = "В коробке " + barCode + " числится номенклатура " + foundNomenIn.getName() + " (" + foundNomenIn.getId() + ") с учетным количеством " + marksInCurrentBox + " марок." +
                            " Количество марок, добавленное в документ " + qtyAddedCurrentBox + " шт.";
                    pickRec(this, invRec.getDocId(), row, 0, null, false, false);
                    message=null;
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

    public void onActivityResult(int requestCode, int resultCode, Intent i){
        if(requestCode == ENTERNOMENID_RETCODE ){
            if(resultCode==RESULT_OK){
                String nomenId =  String.format("%011d", Integer.valueOf(String.valueOf(i.getData())));
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
                        })
                        .setNegativeButton("Отмена" , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // По нажатию - ввод количества прекратить, вернуться в форму документа для ожидания сканирования нового товара
                                cb.onCancelMRCCallback();
                            }
                        })
                        .show();
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

    @Override
    public void onCancelMRCCallback() {
        this.currentState = STATE_SCAN_ANY;
        this.scannedMarkIn = null;
        updateCaption();
    }

    private void proceedOneBottle(InvRecContent invRecContent, NomenIn nomenIn) {
        ActInvRecContent.proceedOneBottle(this, invRec, invRecContent, nomenIn, scannedMarkIn);
        this.currentState = STATE_SCAN_ANY;
        this.scannedMarkIn = null;
        pickRec(this, invRec.getDocId(), invRecContent, 0, null, false, false);
    }

}

