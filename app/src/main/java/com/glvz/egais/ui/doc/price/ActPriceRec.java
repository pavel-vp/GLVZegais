package com.glvz.egais.ui.doc.price;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.daodb.DaoDbPrice;
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.CommandIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.price.PriceRec;
import com.glvz.egais.model.price.PriceRecContent;
import com.glvz.egais.service.CommandFinishCallback;
import com.glvz.egais.service.price.PriceContentArrayAdapter;
import com.glvz.egais.service.price.PriceRecHolder;
import com.glvz.egais.service.writeoff.WriteoffRecHolder;
import com.glvz.egais.ui.ActCommentEdit;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;
import java.util.List;

public class ActPriceRec extends ActBaseDocRec {

    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    private final static int COMMENT_RETCODE = 1;

    private int currentState = STATE_SCAN_MARK;
    private MarkIn scannedMarkIn = null;
    private PriceRecContent priceRecContentLocal;

    private PriceRec priceRec;
    Button btnChangeComment;
    TextView tvCaption;
    private ProgressDialog pg;
    private CommandIn commandByID;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.priceRec = DaoMem.getDaoMem().getMapPriceRec().get(key);
        commandByID = DaoMem.getDaoMem().getCommandByID("PriceLabel");
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_pricerec);

        View container = findViewById(R.id.inclRecPrice);
        docRecHolder = new PriceRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        btnChangeComment = (Button)findViewById(R.id.btnChangeComment);
        btnChangeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открыть форму с редактироанием комментария
                Intent i = new Intent(ActPriceRec.this, ActCommentEdit.class);
                i.putExtra(ActCommentEdit.COMMENT_VALUE, priceRec.getComment());
                startActivityForResult(i, COMMENT_RETCODE);
            }
        });

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActPriceRec.this, priceRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        registerForContextMenu(lvContent);

        adapter = new PriceContentArrayAdapter(this, R.layout.rec_price_position, list);
        lvContent.setAdapter(adapter);
        tvCaption = (TextView)findViewById(R.id.tvCaption);
        pg = new ProgressDialog(this);
        pg.setMessage("Выполняется запрос...");
        pg.setCancelable(false);
        pg.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void updateData() {
        updateDataWithScroll(null);
    }

    private void updateDataWithScroll(final Integer position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                docRecHolder.setItem(priceRec);
                // Достать список позиций по накладной
                Collection<PriceRecContent> newList = DaoMem.getDaoMem().getPriceRecContentList(priceRec.getDocId());
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
                priceRec.setComment(i.getData().toString());
                DaoDbPrice.getDaoDbPrice().saveDbPriceRec(DaoMem.getDaoMem().getShopId(), priceRec);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pricerec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_print:
                print();
                return true;
            case R.id.action_export:
                exportDoc();
                return true;
            case R.id.action_clear:
                // - запрос на подтверждение очистки «Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу» Да/Нет
                //- удаляются данные о фактически собранном количестве и маркам по всему документу
                //- статус задания устанавливается в «новый»
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите очистку. Информация будет удалена по всему документу",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(priceRec);
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
                                DaoMem.getDaoMem().deleteData(priceRec);
                                MessageUtils.showToastMessage("Документ удален!");
                                ActPriceRec.this.finish();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void print() {
        if (commandByID != null && commandByID.getUrl() != null) {
            pg.show();
            String nomens = priceRec.buildNomenList();
            // Запустить запрос к сервису
            DaoMem.getDaoMem().callToWS(commandByID, null, nomens, new CommandFinishCallback() {
                @Override
                public void finishCommand(final String result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pg.dismiss();
                            MessageUtils.showModalMessage(ActPriceRec.this, "Внимание!", result);
                        }
                    });
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.lvContent) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_pricerec_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.delete:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление строки документа. Внимание: строка будет удалена целиком.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PriceRecContent recContent = priceRec.removePriceRecContent(info.position+1);

                                DaoDbPrice.getDaoDbPrice().removePriceRecContent(DaoMem.getDaoMem().getShopId(), priceRec, recContent);
                                MessageUtils.showToastMessage("Строка документа удалена!");
                                updateDataWithScroll(info.position >= priceRec.getPriceRecContentList().size() ? priceRec.getPriceRecContentList().size() - 1 : info.position);
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
        boolean success = DaoMem.getDaoMem().exportData(priceRec);
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
            case EAN8: {
                //1) по ШК найти товар в nomen.json
                final NomenIn nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(barCode);
                // 2) если не найден вывести модальное сообщение “Товар по ШК #BarCode# не найден”, завершить обработку события.
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Товар по штрихкоду " + barCode + ", не найден.");
                    return;
                }
                proceedOneBottle(nomenIn, 1);
                return;
            }
            case PDF417:
            case DATAMATRIX:
                // выполнить проверку корректности ШК по длине:  PDF-417 должна быть 68 символов,  DataMatrix – 150
                if (barCodeType == PDF417 && barCode.length() != 68) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 68, фактически " + barCode.length());
                    return;
                }
                if (barCodeType == DATAMATRIX && barCode.length() != 150) {
                    MessageUtils.showModalMessage(this, "Внимание!", "Неверная длина сканированного ШК, повторите сканирование марки (должна быть 150, фактически " + barCode.length());
                    return;
                }
                // - для Всех типов док - выполнить проверку допустимости добавления этой марки, типы проверяемых марок зависят от состояния CheckMark в справочнике магазинов shops.json:
                //   - «DataMatrix» - проверяются только марки DataMatrix (проверка PDF417 - пропускается)
                //   - «DataMatrixPDF417» - проверяются марки DataMatrix и PDF417
                MarkIn markIn = null;
                // искать марку в справочнике «marks.json»
                markIn = DaoMem.getDaoMem().findMarkByBarcode(barCode);
                if (markIn == null) {
                    // если не найдена: модальное сообщение , прерывание обработки события.
                    MessageUtils.showModalMessage(this, "Внимание!", "Марка не состоит на учете в магазине!");
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
                    MessageUtils.showModalMessage(this, "Внимание!", "Не удалось определить товар по марке!");
                    return;
                }
                {
                    NomenIn nomenIn = DaoMem.getDaoMem().findNomenInAlcoByNomenId(markIn.getNomenId());
                    proceedOneBottle(nomenIn, 1);
                }
                break;
            case CODE128:
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
                    // Уникальный ключ для поиска записей в документе - «NomenID»
                    // Если записи товара в документе не найдено — добавить новую.
                    foundNomenIn = DaoMem.getDaoMem().findNomenInByNomenId(mark.getNomenId());
                    if (foundNomenIn == null) {
                        continue;
                    }
                    PriceRecContent row = findOrAddNomen(foundNomenIn, mark, barCode);
                    position = Integer.parseInt(row.getPosition());
                    // увеличить на 1 переменную «Количество, добавленное по текущей коробке»
                    qtyAddedCurrentBox++;
                }
                if (position != null) {
                    updateDataWithScroll(position);
                }
                if (foundNomenIn != null) {
                        MessageUtils.playSound(R.raw.bottle_one);
                }
                break;
        }

    }

    private PriceRecContent findOrAddNomen(NomenIn nomenIn, MarkIn mark, String barCode) {
        PriceRecContent resultRecContent = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (PriceRecContent recContent : priceRec.getPriceRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId())) {
                resultRecContent = recContent;
                break;
            }
            position++;
        }
        if (resultRecContent == null) {
            position++;
            resultRecContent = new PriceRecContent(String.valueOf(position), null);
            resultRecContent.setNomenIn(nomenIn, null);
            priceRec.getRecContentList().add(resultRecContent);
        }

        //13) установить статус документа «в работе»
        resultRecContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
        priceRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbPrice.getDaoDbPrice().saveDbPriceRecContent(DaoMem.getDaoMem().getShopId(), priceRec, resultRecContent);
        this.scannedMarkIn = null;
        return resultRecContent;
    }

    private void proceedOneBottle(NomenIn nomenIn, double value) {
        priceRecContentLocal = null;
        // 9) найти в документе позицию с NomenID (если такой нет — добавить) и у этой позиции
        int position = 0;
        for (PriceRecContent recContent : priceRec.getPriceRecContentList()) {
            if (recContent.getNomenIn().getId().equals(nomenIn.getId())) {
                priceRecContentLocal = recContent;
                break;
            }
            position++;
        }
        if (priceRecContentLocal == null) {
            position++;
            priceRecContentLocal = new PriceRecContent(String.valueOf(position), null);
            priceRecContentLocal.setNomenIn(nomenIn, null);
            priceRec.getRecContentList().add(priceRecContentLocal);
        }
        MessageUtils.playSound(R.raw.bottle_one);
        //13) установить статус документа «в работе»
        priceRecContentLocal.setStatus(BaseRecContentStatus.IN_PROGRESS);
        priceRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbPrice.getDaoDbPrice().saveDbPriceRecContent(DaoMem.getDaoMem().getShopId(), priceRec, priceRecContentLocal);
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

