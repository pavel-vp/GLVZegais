package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.*;
import com.glvz.egais.service.IncomeArrayAdapter;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActIncomeRec extends Activity implements BarcodeReader.BarcodeListener  {

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
                pickRec(list.get(position), 0, null);
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
        list.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    private void pickRec(IncomeRecContent req, Integer addQty, String barcode) {
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ActIncomeRec.this, ActIncomeRecContent.class);
        in.putExtra(ActIncomeRec.INCOMEREC_WBREGID, incomeRec.getWbRegId());
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_POSITION, req.getPosition().toString());
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_ADDQTY, addQty);
        in.putExtra(ActIncomeRec.INCOMERECCONTENT_LASTMARK, barcode);
        startActivity(in);
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
                incomeRecContent = proceedPdf417(incomeRec, barcode);
                if (incomeRecContent != null) {
                    // Перейти в форму "приемка позиции"
                    pickRec(incomeRecContent, 1, barcode);
                }
                break;
            case DATAMATRIX:
                incomeRecContent = proceedDataMatrix(incomeRec, barcode);
                if (incomeRecContent != null) {
                    // Перейти в форму "приемка позиции"
                    pickRec(incomeRecContent, 1, barcode);
                }
                break;
            case CODE128:
                break;
                // TODO:

        }
    }

    public static IncomeRecContent proceedPdf417(IncomeRec incomeRec, String barcode) {
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
            DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
            return null;
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
            }

        } else {
            // если позиция принята не полностью
            if (incomeRecContent.getStatus() != IncomeRecContentStatus.DONE) {

            } else {
                //  Если полностью принято
            }

        }

        // Статус данной ТТН перевести в состояние “Идет приемка”
        incomeRec.setStatus(IncomeRecStatus.INPROGRESS);
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        return incomeRecContent;

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
