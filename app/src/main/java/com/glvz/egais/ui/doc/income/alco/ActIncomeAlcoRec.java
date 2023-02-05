package com.glvz.egais.ui.doc.income.alco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.daodb.DaoDbDoc;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.income.*;
import com.glvz.egais.service.PickBottliingDateCallback;
import com.glvz.egais.service.TransferCallback;
import com.glvz.egais.service.income.alco.IncomeAlcoContentArrayAdapter;
import com.glvz.egais.service.income.IncomeRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.*;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActIncomeAlcoRec extends ActBaseDocRec implements PickBottliingDateCallback, TransferCallback {

    private IncomeRec incomeRec;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_incomealcorec);

        View container = findViewById(R.id.inclRecPrih);
        docRecHolder = new IncomeRecHolder(container);

        cbFilter = (CheckBox) findViewById(R.id.cbFilter);
        cbFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoMem.getDaoMem().writeFilterOnIncomeRec((IncomeRec) incomeRec, cbFilter.isChecked());
                ActIncomeAlcoRec.this.updateData();
            }
        });
        cbFilter.setChecked(DaoMem.getDaoMem().readFilterOnIncomeRec((IncomeRec) incomeRec));

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActIncomeAlcoRec.this, incomeRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new IncomeAlcoContentArrayAdapter(this, R.layout.rec_incomealco_position, list);
        lvContent.setAdapter(adapter);
    }

    @Override
    protected void updateData() {

        docRecHolder.setItem(incomeRec);
        // Достать список позиций по накладной
        Collection<IncomeRecContent> newList = DaoMem.getDaoMem().getIncomeRecContentList(incomeRec.getDocId());
        list.clear();
        for (IncomeRecContent incomeRecContent : newList) {
            if (!cbFilter.isChecked() || incomeRecContent.getStatus() != BaseRecContentStatus.DONE) {
                list.add(incomeRecContent);
            }
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_incomerec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_export:
                // Если накладная новая или количесвто факт по всем строкам - 0, то поставить статус отказа
                if (incomeRec.getStatus() == BaseRecStatus.NEW ||
                        DaoMem.getDaoMem().checkRecZeroQtyFact(incomeRec)) {
                    DaoMem.getDaoMem().rejectData(incomeRec);
                    cbFilter.setChecked(false);
                    DaoMem.getDaoMem().writeFilterOnIncomeRec(incomeRec, cbFilter.isChecked());
                }
                boolean success = DaoMem.getDaoMem().exportData(incomeRec);
                if (success) {
                    MessageUtils.showToastMessage("Накладная выгружена!");
                    updateData();
                    syncDoc();
                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
                }
                return true;
            case R.id.action_reject:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Отказать приемку по всей накладной? Все данные о приеме будут удалены.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(incomeRec);
                                MessageUtils.showToastMessage("По всей накладной в приемке отказано!");
                                cbFilter.setChecked(false);
                                DaoMem.getDaoMem().writeFilterOnIncomeRec(incomeRec, cbFilter.isChecked());
                                updateData();
                            }
                        });
                return true;
            case R.id.action_clear:
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Очистить все данные о приемке по всей накладной? Все данные о приеме будут удалены.",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().clearData(incomeRec);
                                MessageUtils.showToastMessage("По всей накладной данные о приемке очищены!");
                                cbFilter.setChecked(false);
                                DaoMem.getDaoMem().writeFilterOnIncomeRec(incomeRec, cbFilter.isChecked());
                                updateData();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActIncomeAlcoRecContent.class);
        in.putExtra(ActIncomeAlcoRec.REC_DOCID, docId);
        in.putExtra(ActIncomeAlcoRec.RECCONTENT_POSITION, req.getPosition().toString());
        in.putExtra(ActIncomeAlcoRec.RECCONTENT_ADDQTY, addQty);
        in.putExtra(ActIncomeAlcoRec.RECCONTENT_LASTMARK, barcode);
        in.putExtra(ActIncomeAlcoRec.RECCONTENT_ISBOXSCANNED, isBoxScanned);
        in.putExtra(ActIncomeAlcoRec.RECCONTENT_ISOPENBYSCAN, isOpenByScan);
        ctx.startActivity(in);
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {

        // Определить тип ШК
        BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barcode = barcodeReadEvent.getBarcodeData();
        Integer markScanned;
        IncomeRecContent incomeRecContent;
        // В документах с CheckMark = “DataMatrixPDF417” событие сканирования PDF417 обрабатывается аналогично сканированию DataMatrix.
        if (barCodeType == PDF417 && "DataMatrixPDF417".equals(incomeRec.getIncomeIn().getCheckMark())) {
            barCodeType = DATAMATRIX;
        }
        switch (barCodeType) {
            case EAN8:
            case EAN13:
                //Сканирование ШК номенклатуры (EAN): тут запрещено
                MessageUtils.showToastMessage("Внимание! Сканируйте марку или упаковку или выберите позицию с пивом вручную");
                MessageUtils.playSound(R.raw.tap_position);
                break;
            case PDF417:
                ActionOnScanPDF417Wrapper actionOnScanPDF417Wrapper = proceedPdf417(this, incomeRec, barcode, this);
                if (actionOnScanPDF417Wrapper != null && actionOnScanPDF417Wrapper.ircList != null) {
                    if (actionOnScanPDF417Wrapper.ircList.size() == 1) {
                        // Перейти в форму "приемка позиции"
                        pickRec(this, incomeRec.getDocId(), actionOnScanPDF417Wrapper.ircList.get(0), actionOnScanPDF417Wrapper.addQty, actionOnScanPDF417Wrapper.addQty == 0 ? null : barcode, false, true);
                    } else {
                        pickBottlingDate(this, incomeRec.getDocId(), actionOnScanPDF417Wrapper.ircList, barcode, this);
                    }
                }
                break;
            case DATAMATRIX:
                ActionOnScanDataMatrixWrapper actionOnScanDataMatrixWrapper = proceedDataMatrix(this, incomeRec, barcode);
                if (actionOnScanDataMatrixWrapper != null) {
                    // Перейти в форму "приемка позиции"
                    pickRec(this, incomeRec.getDocId(), actionOnScanDataMatrixWrapper.irc, actionOnScanDataMatrixWrapper.addQty, actionOnScanDataMatrixWrapper.addQty == 0 ? null : barcode, false, true);
                }
                break;
            case CODE128:
                // Проверить сканирован ли этот ШК коробки уже
                BaseRecContentMark scannedMark = DaoMem.getDaoMem().findIncomeRecContentScannedMarkBox(incomeRec, barcodeReadEvent.getBarcodeData());
                if (scannedMark != null) {
                    MessageUtils.showToastMessage("Марка коробки уже сканирована!");
                }

                incomeRecContent = proceedCode128(this, incomeRec, barcode);
                if (incomeRecContent != null) {
                    // Перейти в форму "приемка позиции" с установленным флагом что сканируем упаковку
                    int addQty = DaoMem.getDaoMem().calculateQtyToAdd(incomeRec, incomeRecContent, barcode);
                    pickRec(this, incomeRec.getDocId(), incomeRecContent, addQty, barcode, true, true);
                }
                break;
        }
    }

    public static IncomeRecContent proceedCode128(Activity activity, IncomeRec incomeRec, String barcode) {

        // проверить наличие разрешения на коробочную приемку по справочнику поставщиков.
        // Если разрешения нет - выдать сообщение “По поставщику [наименование] приемка коробками запрещена”.
        PostIn postIn = DaoMem.getDaoMem().getDictionary().findPostById(incomeRec.getIncomeIn().getPostID());
        if (postIn == null || postIn.getGroupBoxEnable() == 0) {
            MessageUtils.showToastMessage("По поставщику %s приемка коробками запрещена", postIn == null ? "" : postIn.getName());
            return null;
        }
        // проверить наличие марки в структуре BoxTree
        IncomeRecContent irc = DaoMem.getDaoMem().findIncomeRecContentByBoxBarcode(incomeRec, barcode);
        if (irc == null) {
            // если ШК нет - сообщение
            // LAG 2019-01-21 start (+ параметр activity в параметрах процедуры)
            //MessageUtils.showToastMessage("Штрихкод упаковки %s отсутствует в ТТН ЕГАИС. Проверьте тот ли сканировали ШК либо сканируйте марки побутылочно. Если ШК упаковки и марок не проходят - верните не принятую продукцию поставщику", barcode);
            MessageUtils.showModalMessage(activity, "Внимание!", "Штрихкод упаковки %s отсутствует в ТТН ЕГАИС. Проверьте тот ли сканировали ШК либо сканируйте марки побутылочно. Если ШК упаковки и марок не проходят - верните не принятую продукцию поставщику", barcode);
            // LAG 2019-01-21 end
            return null;
        }
        // проверять по позиции - соответствует ли количество марок количеству позиции - если нет - ругатся - “Допустимо сканирование только по-марочно”
        if (irc.getContentIn().getQty().intValue() != irc.getContentIn().getMarkInfo().length) {
            MessageUtils.showToastMessage("Допустимо сканирование только по-марочно");
            return null;
        }

        // определить позицию в ТТН ЕГАИС и наличие по ней ранее отсканированного ШК номенклатуры
        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(incomeRec);
        return irc;
    }

    @Override
    public void onCallbackPickBottlingDate(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        pickRec(ctx, wbRegId, irc, addQty, barcode, false, true);
    }


    public static void pickBottlingDate(final Context ctx, final String wbRegId, List<IncomeRecContent> incomeRecContentList, final String barcode, final PickBottliingDateCallback cb) {
        final Map<CharSequence, List<IncomeRecContent>> dates = new HashMap<>();
        for (IncomeRecContent irc : incomeRecContentList) {
            String bottlingDate = StringUtils.formatDateDisplay(StringUtils.jsonBottlingStringToDate(irc.getContentIn().getBottlingDate()));
            if (bottlingDate != null && !"".equals(bottlingDate)) {
                List<IncomeRecContent> ircOnDate = dates.get(bottlingDate);
                if (ircOnDate == null) {
                    ircOnDate = new ArrayList<>();
                    dates.put(bottlingDate, ircOnDate);
                }
                dates.get(bottlingDate).add(irc);
            }
        }
        final CharSequence[] items = new CharSequence[dates.size()];
        int i = 0;
        for (CharSequence d : dates.keySet()) {
            items[i] = d;
            i++;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                final int[] choice = {0};
                new AlertDialog.Builder(ctx)
                        .setTitle("Выберите дату")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                choice[0] = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Найти  первую
                                IncomeRecContent irc = dates.get(items[choice[0]]).get(0);
                                cb.onCallbackPickBottlingDate(ctx, wbRegId, irc, 1, barcode);
                            }
                        }).show();
            }
        });
    }

    public static class ActionOnScanPDF417Wrapper {
        List<IncomeRecContent> ircList;
        Integer addQty;

        public ActionOnScanPDF417Wrapper(List<IncomeRecContent> ircList, Integer addQty) {
            this.ircList = ircList;
            this.addQty = addQty;
        }
    }

    public static ActionOnScanPDF417Wrapper proceedPdf417(Activity activity, IncomeRec incomeRec, String barcode, TransferCallback transferCallback) {
        // Проверить что этот ШК ранее не сканировался в данной ТТН
        DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
        if (markScanned != null) {
            if (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK) {
                MessageUtils.showToastMessage("Эта марка уже сканировалась!");
            }
            // Марка была сканирована - найти по ней позицию Rec
            BaseRecContentMark baseRecContentMark = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(incomeRec, barcode);
            baseRecContentMark.setMarkScannedAsType(BaseRecContentMark.MARK_SCANNED_AS_MARK);
            baseRecContentMark.setMarkScannedReal(barcode);
            // возвращать управление - переходим в карточку позиции
            DaoDbDoc.getDaoDbDoc().saveDbDocRec(incomeRec);
            IncomeRecContent irc = DaoMem.getDaoMem().findIncomeRecContentByMarkScanned(incomeRec, barcode);
            return new ActionOnScanPDF417Wrapper(Collections.singletonList(irc), 0); // Просто возвращаем
        }

        // Проверить наличие ШК марки в ТТН ЕГАИС
        IncomeRecContent incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, barcode);
        if (incomeRecContent == null) {
            // самой марки нет вообще - поищем алкокод
            String alcocode = BarcodeObject.extractAlcode(barcode);
            // определить количество строк в ТТН ЕГАИС с таким алкокодом и принятых не полностью.
            List<IncomeRecContent> incomeRecContentList = DaoMem.getDaoMem().findIncomeRecContentListByAlcocodeNotDone(incomeRec, alcocode, true);
            if (incomeRecContentList.size() == 0 ) {
                MessageUtils.showToastMessage("Продукция с алкокодом %s отсутствует в ТТН поставщика. Принимать бутылку нельзя, верните поставщику!", alcocode);
                return null;
            }
            if (incomeRecContentList.size() == 1 ) {
                incomeRecContent = incomeRecContentList.get(0);
                //определить позицию в ТТН ЕГАИС и принятое по ней количество
                //Если [Количество по ТТН] = [Принятое количество]
                if (incomeRecContent.getQtyAccepted() != null && incomeRecContent.getQtyAccepted().equals(incomeRecContent.getContentIn().getQty()) &&
                        incomeRecContent.getContentIn().getQtyDirectInput() == 0) {
                    MessageUtils.showModalMessage(activity,"Внимание","По позиции номер: %s, алкокод: %s, (%s) уже принято полное количество %s. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику!",
                            incomeRecContent.getPosition(),
                            alcocode,
                            incomeRecContent.getContentIn().getName(),
                            incomeRecContent.getQtyAccepted()
                            );
                    return null;
                }
            } else {
                // в этом списке сделать выбор по датам
                return new ActionOnScanPDF417Wrapper(incomeRecContentList, 1);
            }

        } else {
            // если позиция принята не полностью
            if (incomeRecContent.getStatus() != BaseRecContentStatus.DONE) {
                return new ActionOnScanPDF417Wrapper(Collections.singletonList(incomeRecContent), 1);
            } else {
                //  Если полностью принято
                // проверить можно ли одну из марок, ранее принятых по этой позиции перенести на другую позицию этой ТТН с таким же алкокодом
                // (новая позиция должна быть еще принята не полностью, с приоритетом по совпадению даты розлива, а переносимая марка - не указана в текущей приходной ТТН ЕГАИС).
                DataToTransfer dataToTransfer = ActIncomeAlcoRec.findIncomeRecContentToTransfer(incomeRec, barcode, incomeRecContent);
                if (dataToTransfer != null) {
                    // Перенести подобранную марку в новую позицию и у этой новой позиции Принятое количество увеличить на 1 шт.
                    String markToTransfer = dataToTransfer.baseRecContentMark.getMarkScanned();
                    //  Текущую марку привязать к текущей позиции (без изменения в ней принятого количества).
                    dataToTransfer.baseRecContentMark.setMarkScanned(barcode);
                    dataToTransfer.baseRecContentMark.setMarkScannedReal(barcode);
                    //dataToTransfer.incomeRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(markToTransfer, BaseRecContentMark.MARK_SCANNED_AS_MARK));
                    //DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
                    //  Завершить обработку марки  (выход на следующее сканирование). Перебрасываем на форму карточки той, куда перекинули марку
                    transferCallback.doFinishTransferCallback(MainApp.getContext(), incomeRec.getDocId(), dataToTransfer.incomeRecContent, 1, markToTransfer);

                    return null;
                } else {
                    String alcocode = BarcodeObject.extractAlcode(barcode);
                    MessageUtils.showToastMessage("Запрет приемки. Лишняя бутылка. По алкокоду %s все позиции приняты полностью. Верните бутылку поставщику", alcocode);
                    return null;
                }
            }

        }

        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbDoc.getDaoDbDoc().saveDbDocRecContent(incomeRec,incomeRecContent);
        return new ActionOnScanPDF417Wrapper(Collections.singletonList(incomeRecContent), 1);

    }

    @Override
    public void doFinishTransferCallback(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        pickRec(ctx, wbRegId, irc, addQty, barcode, false, true);
    }


    public static class DataToTransfer {
        IncomeRecContent incomeRecContent;
        BaseRecContentMark baseRecContentMark;

        public DataToTransfer(IncomeRecContent incomeRecContent, BaseRecContentMark baseRecContentMark) {
            this.incomeRecContent = incomeRecContent;
            this.baseRecContentMark = baseRecContentMark;
        }
    }

    public static DataToTransfer findIncomeRecContentToTransfer(IncomeRec incomeRec, String barcode, IncomeRecContent originalIncomeRecContent) {

        BaseRecContentMark ircmToTransfer = null;
        // В оригинальной позиции найти марку, которую можно теоретически перенести - она недолжна быть нигде в ТТН
        for (BaseRecContentMark ircm : originalIncomeRecContent.getBaseRecContentMarkList()) {
            // Если марки нет вообще в этой ТТН
            IncomeRecContent ircTmp = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, ircm.getMarkScanned());
            if (ircTmp == null) {
                ircmToTransfer = ircm;
                break;
            }
        }
        if (ircmToTransfer == null)
            return null;

        String alcocode = BarcodeObject.extractAlcode(barcode);
        List<IncomeRecContent> incomeRecContentList = DaoMem.getDaoMem().findIncomeRecContentListByAlcocodeNotDone(incomeRec, alcocode, false);
        // пройтись по списку, найти запись (не оригинальную), которая еще не принята
        for (IncomeRecContent irc : incomeRecContentList) {
            if (!irc.getPosition().equals(originalIncomeRecContent.getPosition()) // Не эта позиция
                    && irc.getStatus() != BaseRecContentStatus.DONE ) {   // не принятая

                return new DataToTransfer(irc, ircmToTransfer);
            }
        }
        return null;
    }

    public static class ActionOnScanDataMatrixWrapper {
        IncomeRecContent irc;
        Integer addQty;

        public ActionOnScanDataMatrixWrapper(IncomeRecContent irc, Integer addQty) {
            this.irc = irc;
            this.addQty = addQty;
        }
    }

    public static ActionOnScanDataMatrixWrapper proceedDataMatrix(Activity activity, IncomeRec incomeRec, String barcode) {
        // Проверить что этот ШК ранее не сканировался в данной ТТН
        DaoMem.CheckMarkScannedResult markScanned = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
        if (markScanned != null) {
            if (markScanned.markScannedAsType == BaseRecContentMark.MARK_SCANNED_AS_MARK) {
                MessageUtils.showToastMessage("Эта марка уже сканировалась!");
            }
            // Марка была сканирована - найти по ней позицию Rec
            BaseRecContentMark baseRecContentMark = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(incomeRec, barcode);
            baseRecContentMark.setMarkScannedAsType(BaseRecContentMark.MARK_SCANNED_AS_MARK);
            baseRecContentMark.setMarkScannedReal(barcode);
            // переходим в карточку
            DaoDbDoc.getDaoDbDoc().saveDbDocRec(incomeRec);
            IncomeRecContent irc = DaoMem.getDaoMem().findIncomeRecContentByMarkScanned(incomeRec, barcode);
            return new ActionOnScanDataMatrixWrapper(irc,0);
        }
        // Проверить наличие ШК марки в ТТН ЕГАИС
        IncomeRecContent incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, barcode);
        if (incomeRecContent == null) {
            // LAG 2019-01-21 start (+ параметр activity в параметрах процедуры)
            //MessageUtils.showToastMessage("Прием бутылки запрещен: марка отсутствует в ТТН от поставщика. Верните бутылку поставщику, принимать ее нельзя!");
            MessageUtils.showModalMessage(activity, "Внимание!", "Прием бутылки запрещен: марка отсутствует в ТТН от поставщика. Верните бутылку поставщику, принимать ее нельзя!");
            // LAG 2019-01-21 end
            return null;
        }
        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(BaseRecStatus.INPROGRESS);
        DaoDbDoc.getDaoDbDoc().saveDbDocRecContent(incomeRec, incomeRecContent);
        return new ActionOnScanDataMatrixWrapper(incomeRecContent, 1);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }
}
