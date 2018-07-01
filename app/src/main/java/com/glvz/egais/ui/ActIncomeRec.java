package com.glvz.egais.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.*;
import com.glvz.egais.service.IncomeArrayAdapter;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.service.PickBottliingDateCallback;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.*;

public class ActIncomeRec extends Activity implements BarcodeReader.BarcodeListener, PickBottliingDateCallback {

    public final static String INCOMEREC_WBREGID = "WBREGID";
    public final static String INCOMERECCONTENT_POSITION = "POSITION";
    public static final String INCOMERECCONTENT_ADDQTY = "ADDQTY";
    public static final String INCOMERECCONTENT_LASTMARK = "LASTMARK";

    private IncomeRec incomeRec;

    private IncomeArrayAdapter.DocRecHolder docRecHolder;
    private CheckBox cbFilter;
    private ListView lvContent;
    private List<IncomeRecContent> list = new ArrayList<>();
    IncomeContentArrayAdapter adapter;
    private static final Handler handler = new Handler(MainApp.getContext().getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomerec);

        Bundle extras = getIntent().getExtras();
        String key = extras.getString(INCOMEREC_WBREGID);
        incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(key);

        setResources();
        updateData();
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
                DaoMem.getDaoMem().exportData(incomeRec);
                MessageUtils.showModalMessage("Накладная выгружена!");
                return true;
            case R.id.action_reject:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setResources() {

        View container = findViewById(R.id.inclRecPrih);
        docRecHolder = new IncomeArrayAdapter.DocRecHolder(container);

        cbFilter = (CheckBox) findViewById(R.id.cbFilter);
        cbFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActIncomeRec.this.updateData();
            }
        });

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActIncomeRec.this, incomeRec.getWbRegId(), list.get(position), 0, null);
            }
        });
        adapter = new IncomeContentArrayAdapter(this, R.layout.rec_prih_position, list);
        lvContent.setAdapter(adapter);

    }

    private void updateData() {
        docRecHolder.setItem(incomeRec);
        // Достать список позиций по накладной
        Collection<IncomeRecContent> newList = DaoMem.getDaoMem().getIncomeRecContentList(incomeRec.getWbRegId());
        list.clear();
        for (IncomeRecContent incomeRecContent : newList) {
            if (!cbFilter.isChecked() || incomeRecContent.getStatus() != IncomeRecContentStatus.DONE) {
                list.add(incomeRecContent);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static void pickRec(Context ctx, String wbRegId, IncomeRecContent req, Integer addQty, String barcode) {
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActIncomeRecContent.class);
        in.putExtra(ActIncomeRec.INCOMEREC_WBREGID, wbRegId);
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_POSITION, req.getPosition().toString());
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_ADDQTY, addQty);
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_LASTMARK, barcode);
        ctx.startActivity(in);
    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.linkToListener(this);
        updateData();
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.unLinkFromListener(this);
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {

        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barcode = barcodeReadEvent.getBarcodeData();
        Integer markScanned;
        IncomeRecContent incomeRecContent;
        switch (barCodeType) {
            case EAN13:
                //Сканирование ШК номенклатуры (EAN): тут запрещено
                break;
            case PDF417:
                List<IncomeRecContent> incomeRecContentList = proceedPdf417(incomeRec, barcode);
                if (incomeRecContentList != null) {
                    if (incomeRecContentList.size() == 1) {
                        // Перейти в форму "приемка позиции"
                        pickRec(this, incomeRec.getWbRegId(), incomeRecContentList.get(0), 1, barcode);
                    } else {
                        pickBottlingDate(this, incomeRec.getWbRegId(), incomeRecContentList, barcode, this);
                    }
                }
                break;
            case DATAMATRIX:
                incomeRecContent = proceedDataMatrix(incomeRec, barcode);
                if (incomeRecContent != null) {
                    // Перейти в форму "приемка позиции"
                    pickRec(this, incomeRec.getWbRegId(), incomeRecContent, 1, barcode);
                }
                break;
            case CODE128:
                break;
                // TODO:

        }
    }

    @Override
    public void onCallbackPickBottlingDate(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        pickRec(ctx, wbRegId, irc, addQty, barcode);
    }


    public static void pickBottlingDate(final Context ctx, final String wbRegId, List<IncomeRecContent> incomeRecContentList, final String barcode, final PickBottliingDateCallback cb) {
        final Map<CharSequence, List<IncomeRecContent>> dates = new HashMap<>();
        for (IncomeRecContent irc : incomeRecContentList) {
            if (irc.getIncomeContentIn().getBottlingDate() != null && !"".equals(irc.getIncomeContentIn().getBottlingDate())) {
                List<IncomeRecContent> ircOnDate = dates.get(irc.getIncomeContentIn().getBottlingDate());
                if (ircOnDate == null) {
                    ircOnDate = new ArrayList<>();
                    dates.put(irc.getIncomeContentIn().getBottlingDate(), ircOnDate);
                }
                dates.get(irc.getIncomeContentIn().getBottlingDate()).add(irc);
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

    public static List<IncomeRecContent> proceedPdf417(IncomeRec incomeRec, String barcode) {
        // Проверить что этот ШК ранее не сканировался в данной ТТН
        Integer markScanned = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
        if (markScanned != null) {
            if (markScanned == IncomeRecContentMark.MARK_SCANNED_AS_MARK) {
                MessageUtils.showModalMessage("Эта марка уже сканировалась!");
                return null;
            }
            // Марка была сканирована - найти по ней позицию Rec
            IncomeRecContentMark incomeRecContentMark = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(incomeRec, barcode);
            incomeRecContentMark.setMarkScannedAsType(IncomeRecContentMark.MARK_SCANNED_AS_MARK);
            // TODO: возвращать управление или нет?
            //DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
            //return null;

        }

        // Проверить наличие ШК марки в ТТН ЕГАИС
        IncomeRecContent incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, barcode);
        if (incomeRecContent == null) {
            // самой марки нет вообще - поищем алкокод
            String alcocode = BarcodeObject.extractAlcode(barcode);
            // определить количество строк в ТТН ЕГАИС с таким алкокодом и принятых не полностью.
            List<IncomeRecContent> incomeRecContentList = DaoMem.getDaoMem().findIncomeRecContentListByAlcocode(incomeRec, alcocode);
            if (incomeRecContentList.size() == 0 ) {
                MessageUtils.showModalMessage("Продукция с алкокодом [показать] отсутствует в ТТН поставщика. Принимать бутылку нельзя, верните поставщику!");
                return null;
            }
            if (incomeRecContentList.size() == 1 ) {
                incomeRecContent = incomeRecContentList.get(0);
                //определить позицию в ТТН ЕГАИС и принятое по ней количество
                //Если [Количество по ТТН] = [Принятое количество]
                if (incomeRecContent.getQtyAccepted() != null && incomeRecContent.getQtyAccepted().equals(incomeRecContent.getIncomeContentIn().getQty())) {
                    MessageUtils.showModalMessage("По позиции [показать номер, алкокод, наименование ЕГАИС] уже принято полное количество [показать]. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику!");
                    return null;
                }
            } else {
                // в этом списке сделать выбор по датам
                return incomeRecContentList;
            }

        } else {
            // если позиция принята не полностью
            if (incomeRecContent.getStatus() != IncomeRecContentStatus.DONE) {
                return Collections.singletonList(incomeRecContent);
            } else {
                //  Если полностью принято
                // проверить можно ли одну из марок, ранее принятых по этой позиции перенести на другую позицию этой ТТН с таким же алкокодом
                // (новая позиция должна быть еще принята не полностью, с приоритетом по совпадению даты розлива, а переносимая марка - не указана в текущей приходной ТТН ЕГАИС).
                DataToTransfer dataToTransfer = ActIncomeRec.findIncomeRecContentToTransfer(incomeRec, barcode, incomeRecContent);
                if (dataToTransfer != null) {
                    // Перенести подобранную марку в новую позицию и у этой новой позиции Принятое количество увеличить на 1 шт.
                    String markToTransfer = dataToTransfer.incomeRecContentMark.getMarkScanned();
                    //  Текущую марку привязать к текущей позиции (без изменения в ней принятого количества).
                    dataToTransfer.incomeRecContentMark.setMarkScanned(barcode);
                    dataToTransfer.incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(markToTransfer, IncomeRecContentMark.MARK_SCANNED_AS_MARK));
                    DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
                    //  Завершить обработку марки  (выход на следующее сканирование).
                    return null;
                } else {
                    MessageUtils.showModalMessage("Запрет приемки. Лишняя бутылка. По алкокоду [показать] все позиции приняты полностью. Верните бутылку поставщику");
                    return null;
                }
            }

        }

        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(IncomeRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        return Collections.singletonList(incomeRecContent);

    }

    public static class DataToTransfer {
        IncomeRecContent incomeRecContent;
        IncomeRecContentMark incomeRecContentMark;

        public DataToTransfer(IncomeRecContent incomeRecContent, IncomeRecContentMark incomeRecContentMark) {
            this.incomeRecContent = incomeRecContent;
            this.incomeRecContentMark = incomeRecContentMark;
        }
    }

    public static DataToTransfer findIncomeRecContentToTransfer(IncomeRec incomeRec, String barcode, IncomeRecContent originalIncomeRecContent) {

        IncomeRecContentMark ircmToTransfer = null;
        // В оригинальной позиции найти марку, которую можно теоретически перенести - она недолжна быть нигде в ТТН
        for (IncomeRecContentMark ircm : originalIncomeRecContent.getIncomeRecContentMarkList()) {
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
        List<IncomeRecContent> incomeRecContentList = DaoMem.getDaoMem().findIncomeRecContentListByAlcocode(incomeRec, alcocode);
        // пройтись по списку, найти запись (не оригинальную), которая еще не принята
        for (IncomeRecContent irc : incomeRecContentList) {
            if (!irc.getPosition().equals(originalIncomeRecContent.getPosition()) // Не эта позиция
                    && irc.getStatus() != IncomeRecContentStatus.DONE ) {   // не принятая

                return new DataToTransfer(irc, ircmToTransfer);
            }
        }
        return null;
    }

    public static IncomeRecContent proceedDataMatrix(IncomeRec incomeRec, String barcode) {
        // Проверить что этот ШК ранее не сканировался в данной ТТН
        Integer markScanned = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
        if (markScanned != null && markScanned == IncomeRecContentMark.MARK_SCANNED_AS_MARK) {
            MessageUtils.showModalMessage("Эта марка уже сканировалась!");
            return null;
        }
        // Проверить наличие ШК марки в ТТН ЕГАИС
        IncomeRecContent incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, barcode);
        if (incomeRecContent == null) {
            MessageUtils.showModalMessage("Прием бутылки запрещен: марка отсутствует в ТТН от поставщика. Верните бутылку поставщику, принимать ее нельзя!");
            return null;
        }
        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(IncomeRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        return incomeRecContent;
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }
}
