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
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.model.IncomeRecContentMark;
import com.glvz.egais.model.IncomeRecStatus;
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
        switch (barCodeType) {
            case EAN13:
                //Сканирование ШК номенклатуры (EAN):
                break;
            case PDF417:
                // Статус данной ТТН перевести в состояние “Идет приемка”
                incomeRec.setStatus(IncomeRecStatus.INPROGRESS);
                DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
                // Проверить что этот ШК ранее не сканировался в данной ТТН
                markScanned = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
                if (markScanned != null && markScanned == IncomeRecContentMark.MARK_SCANNED_AS_MARK) {
                    MessageUtils.showModalMessage("Эта марка уже сканировалась!");
                    break;
                }
                break;
            case DATAMATRIX:
                IncomeRecContent incomeRecContent = proceedDataMatrix(incomeRec, barcode);
                if (incomeRecContent != null) {
                    // Перейти в форму "приемка позиции"
                    pickRec(incomeRecContent, 1, barcode);
                }

                break;
            case CODE128:
                break;

        }
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
