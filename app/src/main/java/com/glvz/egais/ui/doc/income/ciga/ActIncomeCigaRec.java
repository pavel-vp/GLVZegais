package com.glvz.egais.ui.doc.income.ciga;

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
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.model.income.IncomeRecContent;
import com.glvz.egais.service.income.ciga.IncomeCigaContentArrayAdapter;
import com.glvz.egais.service.income.ciga.IncomeCigaRecHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

public class ActIncomeCigaRec extends ActBaseDocRec {

    private IncomeRec incomeRec;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_incomecigarec);

        View container = findViewById(R.id.inclRecPrih);
        docRecHolder = new IncomeCigaRecHolder(container);

        cbFilter = (CheckBox) findViewById(R.id.cbFilter);
        cbFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoMem.getDaoMem().writeFilterOnIncomeRec((IncomeRec) incomeRec, cbFilter.isChecked());
                ActIncomeCigaRec.this.updateData();
            }
        });
        cbFilter.setChecked(DaoMem.getDaoMem().readFilterOnIncomeRec((IncomeRec) incomeRec));

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActIncomeCigaRec.this, incomeRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new IncomeCigaContentArrayAdapter(this, R.layout.rec_incomeciga_position, list);
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
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Отказать приемку по все накладной? Все данные о приеме будут удалены.",
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // Перейти в форму одной строки позиции
        Intent in = new Intent();
        in.setClass(ctx, ActIncomeCigaRecContent.class);
        in.putExtra(ActIncomeCigaRec.REC_DOCID, docId);
        in.putExtra(ActIncomeCigaRec.RECCONTENT_POSITION, req.getPosition().toString());
        in.putExtra(ActIncomeCigaRec.RECCONTENT_ADDQTY, addQty);
        in.putExtra(ActIncomeCigaRec.RECCONTENT_LASTMARK, barcode);
        in.putExtra(ActIncomeCigaRec.RECCONTENT_ISBOXSCANNED, isBoxScanned);
        in.putExtra(ActIncomeCigaRec.RECCONTENT_ISOPENBYSCAN, isOpenByScan);
        ctx.startActivity(in);

    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {

        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        String mark = BarcodeObject.extractSigaMark(barcodeReadEvent.getBarcodeData());
        final String barcode = mark.replace("\u001D", "");
        Integer markScanned;
        IncomeRecContent incomeRecContent;
        // TODO: implement logic
        switch (barCodeType) {
            case GS1_DATAMATRIX_CIGA:
            {
                DaoMem.CheckMarkScannedResult markScannedResult = DaoMem.getDaoMem().checkMarkScanned(incomeRec, barcode);
                if (markScannedResult == null) {
                    incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, barcode);
                    if (incomeRecContent == null) {
                        MessageUtils.showModalMessage(this, "Внимание!", "Прием запрещен: марка отсутствует в ТТН от поставщика. Верните поставщику, принимать нельзя!");
                        break;
                    }

                    // Статус данной ТТН перевести в состояние “Идет приемка”
                    incomeRec.setStatus(BaseRecStatus.INPROGRESS);
                    DaoMem.getDaoMem().writeLocalDataBaseRec(incomeRec);
                    pickRec(this, incomeRec.getDocId(), incomeRecContent, 1, barcode, false, true);

                } else {
                    MessageUtils.showModalMessage(this, "Внимание!", "Марка уже сканирована!");
                    break;
                }
                break;
            }
            case EAN13:
                break;
            case PDF417:
                break;
            case DATAMATRIX:
                break;
            case CODE128:
                break;
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }


}
