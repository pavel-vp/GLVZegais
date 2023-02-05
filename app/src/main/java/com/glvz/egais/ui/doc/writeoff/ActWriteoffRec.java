package com.glvz.egais.ui.doc.writeoff;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.daodb.DaoDbInv;
import com.glvz.egais.daodb.DaoDbWriteOff;
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.model.writeoff.WriteoffRecContentMark;
import com.glvz.egais.service.writeoff.WriteoffContentArrayAdapter;
import com.glvz.egais.service.writeoff.WriteoffRecHolder;
import com.glvz.egais.ui.ActCommentEdit;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.DoubleValueOnEnterCallback;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;
import java.util.List;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.GS1_DATAMATRIX_CIGA;
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
        registerForContextMenu(lvContent);

        adapter = new WriteoffContentArrayAdapter(this, R.layout.rec_writeoff_position, list);
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
                docRecHolder.setItem(writeoffRec);
                // Достать список позиций по накладной
                Collection<WriteoffRecContent> newList = DaoMem.getDaoMem().getWriteoffRecContentList(writeoffRec.getDocId());
                list.clear();
                list.addAll(newList);
                adapter.notifyDataSetChanged();
                if (position != null) {
                    lvContent.smoothScrollToPosition(position);
                }
                switch (currentState) {
                    case STATE_SCAN_MARK:
                        tvCaption.setText("Сканируйте марку");
                        break;
                    case STATE_SCAN_EAN:
                        tvCaption.setText("Сканируйте штрихкод");
                        break;
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent i){
        if(requestCode == COMMENT_RETCODE ){
            if(resultCode==RESULT_OK){
                writeoffRec.setComment(i.getData().toString());
                DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRec(DaoMem.getDaoMem().getShopId(), writeoffRec);
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
                                currentState = STATE_SCAN_MARK;
                                MessageUtils.showToastMessage("Данные по накладной удалены!");
                                updateData();
                            }
                        });
                return true;
            case R.id.action_remove:
                // удалять документ из списка и его out-файл (если есть).
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление документа",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().deleteData(writeoffRec);
                                MessageUtils.showToastMessage("Документ удален!");
                                ActWriteoffRec.this.finish();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvContent) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_writeoffrec_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.delete:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление строки документа. Внимание: строка будет удалена целиком - все бутылки. Если бутылки подготовлены к отгрузке - уберите их из коробки.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WriteoffRecContent recContent = writeoffRec.removeWriteoffRecContent(info.position+1);

                                DaoDbWriteOff.getDaoDbWriteOff().removeWriteoffRecContent(DaoMem.getDaoMem().getShopId(), writeoffRec, recContent);
                                MessageUtils.showToastMessage("Строка документа удалена!");
                                updateDataWithScroll(info.position >= writeoffRec.getWriteoffRecContentList().size() ? writeoffRec.getWriteoffRecContentList().size() - 1 : info.position);
                            }
                        });
                return true;
            default:
                return super.onContextItemSelected(item);
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
        String barCode = barcodeReadEvent.getBarcodeData();
        switch (barCodeType) {
            case EAN13:
            case EAN8:
                // 1) Если форма находилась в режиме ожидания сканирования ШК PDF-417 (DataMatrix):
                // сейчас можно будет сканировать и добавлять произвольный товар - поэтому можно
                if (currentState == STATE_SCAN_MARK) {
                    //1) по ШК найти товар в nomen.json
                    final NomenIn nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(barCode);
                    // 2) если не найден вывести модальное сообщение “Товар по ШК #BarCode# не найден”, завершить обработку события.
                    if (nomenIn == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + barCode + ", не найден.");
                        return;
                    }
                    // 3) если у найденного товара NomenType = 1 вывести модальное сообщение “Для маркированного алкоголя сканируйте марку или ШК коробки”, завершить обработку события.
                    if (nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Для маркированного алкоголя сканируйте марку или ШК коробки");
                        return;
                    }
                    // 4) если у найденного товара NomenType = 3 вывести модальное сообщение “Для маркированных сигарет сканируйте марку с пачки или блока”, завершить обработку события.
                    if (nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_TOBACCO) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Для маркированных сигарет сканируйте марку с пачки или блока");
                        return;
                    }
                    // 5) для всех остальных товаров (NomenType in (0, 2) продукты, пиво):
                    // 5.1) вывести модальный запрос на ввод добавляемого количества:
                    MessageUtils.ShowModalToEntedDoubleValue(this, "Введите количество", "Введите добавляемое количество",
                            new DoubleValueOnEnterCallback() {
                                @Override
                                public void handle(double value) {
                                    // 5.2) после ввода пользователем в товарной части поискать строку с таким товаром:
                                    // если найдена - добавить количество в нее, если не найдена - добавить строку с введенным количеством.
                                    // (прим. поиск товарной строки выполнять только по коду товара, у сигарет МРЦ не учитывать).
                                    // В соответствии с добавленным количеством проиграть озвучивание «bottle_many.mp3» или «bottle_one.mp3»
                                    proceedOneBottle(nomenIn, value, 0d);
                                }
                            });
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
                    proceedOneBottle(nomenIn, 1, 0d);
                    return;
                }
                break;
            case PDF417:
            case DATAMATRIX:
            case GS1_DATAMATRIX_CIGA:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод, с того же товара с которой только что сканировали марку");
                    return;
                }
                barCode = tryToTransformMark(barCodeType, barCode);
                // выполнить проверку корректности ШК по длине:  PDF-417 должна быть 68 символов,  DataMatrix – 150
                if (barCodeType == PDF417 && barCode.length() != 68) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 68, фактически " + barCode.length());
                    return;
                }
                MarkIn markIn = null;
                if (barCode.length() == 150) { // для марок длиной 150 символов (алкоголь)
                    // Проверить наличие этой марки среди ранее сохраненных марок всех товарных позиций этого задания.
                    // Проверить что этот ШК ранее не сканировался в данной ТТН
                    DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(writeoffRec, barCode);
                    if (markScanned != null && (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK || markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_BOX)) {
                        // Если марка найдена — модальное сообщение «», прервать обработку события
                        MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                        return;
                    }
                    // - для Всех типов док - выполнить проверку допустимости добавления этой марки, типы проверяемых марок зависят от состояния CheckMark в справочнике магазинов shops.json:
                    //   - «DataMatrix» - проверяются только марки DataMatrix (проверка PDF417 - пропускается)
                    //   - «DataMatrixPDF417» - проверяются марки DataMatrix и PDF417
                    if (DaoMem.getDaoMem().isNeedToCheckMarkForWriteoff(barCodeType)) {
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
                        int position = 0;
                        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
                            boolean found = false;
                            for (BaseRecContentMark mark : recContent.getBaseRecContentMarkList()) {
                                if (mark.getMarkScanned().equals(markIn.getMark())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                break;
                            }
                            position++;
                        }
                        updateDataWithScroll(position);
                        return;
                    }
                    NomenIn nomenIn = DaoMem.getDaoMem().findNomenInAlcoByNomenId(markIn.getNomenId());
                    proceedOneBottle(nomenIn, 1, 0d);

                } else if (barCode.length() == 21 ) { // для марок 21 символ (пачка сигарет):
                    // Проверить наличие этой марки среди ранее сохраненных марок всех товарных позиций этого задания.
                    // Проверить что этот ШК ранее не сканировался в данной ТТН
                    DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(writeoffRec, barCode);
                    if (markScanned != null && (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK || markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_BOX)) {
                        // Если марка найдена — модальное сообщение «», прервать обработку события
                        MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                        return;
                    }
                    // искать марку в справочнике «marks.json»
                    markIn = DaoMem.getDaoMem().findMarkByBarcode(barCode);
                    if (markIn == null) {
                        // если не найдена: модальное сообщение , прерывание обработки события.
                        MessageUtils.showModalMessage(this, "Внимание!", "Марка не состоит на учете. Обработка невозможна. Отложите для дальнейшего разбора и сканируйте другую!");
                        return;
                    }
                    this.scannedMarkIn = markIn;

                    // если NomenID не определен
                    if (StringUtils.isEmptyOrNull(markIn.getNomenId())) {
                        // 8.1) подсказку изменить на «Сканируйте штрихкод»
                        this.currentState = STATE_SCAN_EAN;
                        // 8.2) Звуковое сообщение «Сканируйте штрихкод»
                        MessageUtils.playSound(R.raw.scan_ean);
                        int position = 0;
                        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
                            boolean found = false;
                            for (BaseRecContentMark mark : recContent.getBaseRecContentMarkList()) {
                                if (mark.getMarkScanned().equals(markIn.getMark())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                break;
                            }
                            position++;
                        }
                        updateDataWithScroll(position);
                        return;
                    }
                    NomenIn nomenIn = DaoMem.getDaoMem().findNomenInByNomenId(markIn.getNomenId());
                    if (nomenIn == null) {
                        // если не найдена: модальное сообщение , прерывание обработки события.
                        MessageUtils.showModalMessage(this, "Внимание!", "Товар не состоит на учете. Обработка невозможна. Отложите для дальнейшего разбора и сканируйте другую!");
                        return;
                    }
                    proceedOneBottle(nomenIn, 1, this.scannedMarkIn.getMrc());

                } else if (barCode.length() == 25) { // для марок 25 символов
                    // для всех марок в marks.json по фильтру в реквизите Box – выполнить добавление в документ с накоплением счетчиков обработанных, пропущенных и добавленных марок
                    // в справочнике marks.json найти все марки с ШК коробки, соответствующей сканированной
                    List<MarkIn> marksInBox = DaoMem.getDaoMem().findMarksByBoxBarcode(barCode);
                    if (marksInBox.size() == 0) {
                        // если ни одной марки не найдено вывести модальное сообщение: «По ШК коробки #BoxBarcode# марки не найдены». Прервать обработку.
                        MessageUtils.showModalMessage(this, "Внимание!", "По ШК " + barCode + " Блок сигарет не найден, сканируйте каждую пачку");
                        break;
                    }
                    // Обнулить переменные «Числится марок в текущей коробке», «Количество, добавленное по текущей коробке»
                    int marksInCurrentBox = 0;
                    int qtyAddedCurrentBox = 0;
                    Integer position = null;
                    // Найти Еан в Марке
                    String ean = BarcodeObject.extractEanFromGS1DM(barCode);
                    if (ean.length() != 8 && ean.length() != 13) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Не найден ШК из марки!");
                        return;
                    }
                    NomenIn foundNomenIn = DaoMem.getDaoMem().findNomenInByBarCode(ean);
                    if (foundNomenIn == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Не найден товар по ШК из марки -" + ean);
                        return;
                    }

                    // для каждой найденной марки выполнить обработку
                    for (MarkIn mark : marksInBox) {
                        // Увеличить на 1 переменную «Числится марок в текущей коробке»
                        marksInCurrentBox++;
                        // 4.2 из найденной записи марки извлечь номенклатуру, выполнить поиск в товарной части документа.
                        // Уникальный ключ для поиска записей в документе - «NomenID»+"Mrc"
                        // Если записи товара в документе не найдено — добавить новую.
                        // проверить наличие текущей марки среди ранее сканированных в документе. При наличии — пропустить обработку марки
                        position = findInContentByMark(mark.getMark());
                        if (position != null) {
                            continue;
                        }
                        WriteoffRecContent row = findOrAddNomen(foundNomenIn, mark, barCode, mark.getMrc());
                        position = Integer.parseInt(row.getPosition());
                        // увеличить на 1 переменную «Количество, добавленное по текущей коробке»
                        qtyAddedCurrentBox++;
                    }
                    if (position != null) {
                        updateDataWithScroll(position);
                    }
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
                    //6.2 вывести модальное сообщение:
                    //В коробке #ШККоробки# числится номенклатура #НаименованиеНоменклатуры# (#NomenID#) с учетным количеством #Числится марок в текущей коробке# марок.
                    //Количество марок, добавленное в документ #Количество, добавленное по текущей коробке# шт.
                    MessageUtils.showModalMessage(this, "Внимание!", "В коробке " + barCode + " числится номенклатура " + foundNomenIn.getName() + " (" + foundNomenIn.getId() + ") с учетным количеством " + marksInCurrentBox + " марок." +
                            " Количество марок, добавленное в документ " + qtyAddedCurrentBox + " шт.");
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки, фактичесая длина марки " + barCode.length());
                }
                break;
/*            case GS1_DATAMATRIX_CIGA:
                // маркированная вода: вырезуть из марки EAN, по нему определить номенклатуру, добавить в документ (пока без проверки марки)
                if (barCode.length() == 38) {
                    markScanned = DaoMem.getDaoMem().checkMarkScanned(writeoffRec, barCode);
                    if (markScanned != null && (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK || markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_BOX)) {
                        // Если марка найдена — модальное сообщение «», прервать обработку события
                        MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                        return;
                    }
                    final String EANbarCode = barCode.substring(3, 16);
                    nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(EANbarCode);
                    if (nomenIn == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + EANbarCode + ", не найден.");
                        return;
                    }
                    markIn = new MarkIn();
                    markIn.setMark(barCode);
                    this.scannedMarkIn = markIn;
                    proceedOneBottle(nomenIn, 1);
                    break;
                }
                // маркированная молочная продукция поштучная: : вырезуть из марки EAN, по нему определить номенклатуру, добавить в документ (пока без проверки марки)
                if (barCode.length() == 31) {
                    markScanned = DaoMem.getDaoMem().checkMarkScanned(writeoffRec, barCode);
                    if (markScanned != null && (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK || markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_BOX)) {
                        // Если марка найдена — модальное сообщение «», прервать обработку события
                        MessageUtils.showModalMessage(this, "Внимание!", "Эта марка ранее уже была отсканирована в этом задании в позиции " + markScanned.recContent.getPosition() + " товара " + markScanned.recContent.getNomenIn().getName());
                        return;
                    }
                    final String EANbarCode = barCode.substring(3, 16);
                    nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(EANbarCode);
                    if (nomenIn == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + EANbarCode + ", не найден.");
                        return;
                    }
                    markIn = new MarkIn();
                    markIn.setMark(barCode);
                    this.scannedMarkIn = markIn;
                    proceedOneBottle(nomenIn, 1);
                    break;
                }*/
            case CODE128:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Сканируйте штрихкод, с того же товара с которого только что сканировали марку");
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
                // для каждой найденной марки выполнить обработку
                for (MarkIn mark : marksInBox) {
                    // Увеличить на 1 переменную «Числится марок в текущей коробке»
                    marksInCurrentBox++;
                    // 4.2 из найденной записи марки извлечь номенклатуру, выполнить поиск в товарной части документа.
                    // Уникальный ключ для поиска записей в документе - «NomenID»+"Mrc"
                    // Если записи товара в документе не найдено — добавить новую.
                    foundNomenIn = DaoMem.getDaoMem().findNomenInByNomenId(mark.getNomenId());
                    if (foundNomenIn == null) {
                        continue;
                    }
                    // проверить наличие текущей марки среди ранее сканированных в документе. При наличии — пропустить обработку марки
                    position = findInContentByMark(mark.getMark());
                    if (position != null) {
                        continue;
                    }
                    WriteoffRecContent row = findOrAddNomen(foundNomenIn, mark, barCode, mark.getMrc());
                    position = Integer.parseInt(row.getPosition());
                    // увеличить на 1 переменную «Количество, добавленное по текущей коробке»
                    qtyAddedCurrentBox++;
                }
                if (position != null) {
                    updateDataWithScroll(position);
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
                    //6.2 вывести модальное сообщение:
                    //В коробке #ШККоробки# числится номенклатура #НаименованиеНоменклатуры# (#NomenID#) с учетным количеством #Числится марок в текущей коробке# марок.
                    //Количество марок, добавленное в документ #Количество, добавленное по текущей коробке# шт.
                    MessageUtils.showModalMessage(this, "Внимание!", "В коробке " + barCode + " числится номенклатура " + foundNomenIn.getName() + " (" + foundNomenIn.getId() + ") с учетным количеством " + marksInCurrentBox + " марок." +
                            " Количество марок, добавленное в документ " + qtyAddedCurrentBox + " шт.");
                }
                break;
            default:
                if (currentState == STATE_SCAN_EAN) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте штрихкод");
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неврный тип штрихкода. Сканируйте марку" );
                }
        }

    }

    private String tryToTransformMark(BarcodeObject.BarCodeType barCodeType, String barCode) {
        //Алгоритм предварительного преобразования ШК:
        if (barCodeType == DATAMATRIX) {
            // –	150 символов - оставить без изменений (алкоголь, новая марка)
            if (barCode.length() == 150) return barCode;
            //–	29 символов - взять первые 21 символ (пачка табачной продукции)
            if (barCode.length() == 29) {
                return barCode.substring(0, 21);
            }
        }
        if (barCodeType == GS1_DATAMATRIX_CIGA) {
            //–	удалить символы “(“ и “)”
            String tmp = barCode.replaceAll("\\(", "").replaceAll("\\)", "");
            // –	43 и более и соответствует шаблону
            String pattern = "^01\\d{14}21.{7}[\\x1D]8005\\d+[\\x1D].*";
            if (tmp.length() >= 43 && tmp.matches(pattern)) {
                return tmp.substring(0,25);
            }
        }
        return barCode;
    }

    private Integer findInContentByMark(String mark) {
        Integer pos = 0;
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            pos++;
            for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
                if (mark.equals(baseRecContentMark.getMarkScanned())) {
                    return pos;
                }
            }
        }
        return null;
    }

    private WriteoffRecContent findOrAddNomen(NomenIn nomenIn, MarkIn mark, String barCode, Double mrc) {
        WriteoffRecContent resultRecContent = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId()) &&
                    ((recContent.getMrc() == null && mrc == null) || (recContent.getMrc() == null && mrc == 0) || (recContent.getMrc() == 0 && mrc == null) || (Double.compare(recContent.getMrc(), mrc) == 0))) {
                resultRecContent = recContent;
                break;
            }
            position++;
        }
        if (resultRecContent == null) {
            position++;
            resultRecContent = new WriteoffRecContent(String.valueOf(position), null);
            resultRecContent.setNomenIn(nomenIn, null);
            resultRecContent.setMrc(mrc == null ? 0 : mrc);
            writeoffRec.getRecContentList().add(resultRecContent);
        }
        //10) поле «Количество факт» добавить 1 шт к предыдущему значению
        resultRecContent.setQtyAccepted((resultRecContent.getQtyAccepted() == null ? 0 : resultRecContent.getQtyAccepted()) + 1);
        resultRecContent.getBaseRecContentMarkList().add(new WriteoffRecContentMark(mark.getMark(), BaseRecContentMark.MARK_SCANNED_AS_BOX, mark.getMark(), barCode));

        //13) установить статус документа «в работе»
        resultRecContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
        writeoffRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRecContent(DaoMem.getDaoMem().getShopId(), writeoffRec, resultRecContent);
        this.currentState = STATE_SCAN_MARK;
        this.scannedMarkIn = null;
        return resultRecContent;
    }

    private void proceedOneBottle(NomenIn nomenIn, double value, Double mrc) {
        writeoffRecContentLocal = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId()) &&
                    ((recContent.getMrc() == null && mrc == null) || (recContent.getMrc() == null && mrc == 0) || (recContent.getMrc() == 0 && mrc == null) || (Double.compare(recContent.getMrc(), mrc) == 0))) {
                writeoffRecContentLocal = recContent;
                break;
            }
            position++;
        }
        if (writeoffRecContentLocal == null) {
            position++;
            writeoffRecContentLocal = new WriteoffRecContent(String.valueOf(position), null);
            writeoffRecContentLocal.setNomenIn(nomenIn, null);
            writeoffRecContentLocal.setMrc(mrc == null ? 0 : mrc);
            writeoffRec.getRecContentList().add(writeoffRecContentLocal);
        }
        //10) поле «Количество факт» добавить 1 шт к предыдущему значению
        writeoffRecContentLocal.setQtyAccepted((writeoffRecContentLocal.getQtyAccepted() == null ? value : writeoffRecContentLocal.getQtyAccepted() + value));
        //11) добавить марку к списку марок текущей позиции.
        if (scannedMarkIn != null) {
            writeoffRecContentLocal.getBaseRecContentMarkList().add(new WriteoffRecContentMark(scannedMarkIn.getMark(), BaseRecContentMark.MARK_SCANNED_AS_MARK, scannedMarkIn.getMark(), ""));
        }
        //12) проиграть файл «bottle_one.mp3»
        if (value == 1) {
            MessageUtils.playSound(R.raw.bottle_one);
        } else {
            MessageUtils.playSound(R.raw.bottle_many);
        }
        //13) установить статус документа «в работе»
        writeoffRecContentLocal.setStatus(BaseRecContentStatus.IN_PROGRESS);
        writeoffRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRecContent(DaoMem.getDaoMem().getShopId(), writeoffRec, writeoffRecContentLocal);
        this.currentState = STATE_SCAN_MARK;
        this.scannedMarkIn = null;
        updateDataWithScroll(position);
        // 9.0) желательно как-то выделить эту позицию для пользователя
    }


    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }


    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // no action
    }
}
