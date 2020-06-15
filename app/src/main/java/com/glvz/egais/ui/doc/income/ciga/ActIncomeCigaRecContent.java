package com.glvz.egais.ui.doc.income.ciga;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentMarkIn;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentPositionType;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.model.income.IncomeRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.income.ciga.IncomeCigaRecContentHolder;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

public class ActIncomeCigaRecContent extends Activity implements BarcodeReader.BarcodeListener {

    private String lastMark;
    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;
    private IncomeCigaRecContentHolder incomeRecContentHolder;

    TextView tvAction;
    EditText etQtyAccepted;
    LinearLayout llAccepted;
    Button btnAdd;
    Button btnManualChange;
    private boolean isBoxScanned = false;
    private boolean isOpenByScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomecigareccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeCigaRec.REC_DOCID);
        IncomeRec ir = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeCigaRec.RECCONTENT_POSITION);
        IncomeRecContent irc = (IncomeRecContent) DaoMem.getDaoMem().getRecContentByPosition(ir, position);
        String barcode = extras.getString(ActIncomeCigaRec.RECCONTENT_LASTMARK);
        int addQty = extras.getInt(ActIncomeCigaRec.RECCONTENT_ADDQTY);
        this.isBoxScanned = extras.getBoolean(ActIncomeCigaRec.RECCONTENT_ISBOXSCANNED);
        this.isOpenByScan = extras.getBoolean(ActIncomeCigaRec.RECCONTENT_ISOPENBYSCAN);

        prepareActWithData(wbRegId, irc, addQty, barcode);
    }

    private void prepareActWithData(String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        this.incomeRecContent = irc;
        this.lastMark = barcode;
        if (addQty > 0) {
            proceedAddQtyInternal(addQty);
        }
        updateDisplayData();
    }

    // обработчик добавления марки и/или количества
    private void proceedAddQtyInternal(double addQty) {
        int multiplier = 1;
        if (this.lastMark != null) {
            // Найти Еан в Марке
            String ean = BarcodeObject.extractEanFromGS1DM(this.lastMark);
            if (ean == null || (ean.length() != 8 && ean.length() != 13)) {
                MessageUtils.showModalMessage(this, "Внимание!", "Прием запрещен: марке неверная!");
                return;
            }

            NomenIn nomenIn = DaoMem.getDaoMem().findNomenInByBarCode(ean);
            if (nomenIn == null) {
                MessageUtils.showModalMessage(this, "Внимание!", "Прием запрещен: в справочнике товаров отсутствует этот ШК = " + ean);
                return;
            }
            incomeRecContent.setNomenIn(nomenIn, ean);
            // достать мултиплаер
            IncomeContentMarkIn incomeContentMarkIn = DaoMem.getDaoMem().findIncomeContentMarkByMark(incomeRec, this.lastMark);
            if (incomeContentMarkIn != null && incomeContentMarkIn.getMultiplier() != null) {
                multiplier = Integer.parseInt(incomeContentMarkIn.getMultiplier());
            }
            this.incomeRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(this.lastMark, BaseRecContentMark.MARK_SCANNED_AS_MARK, this.lastMark));

        } else {
            incomeRecContent.setNomenIn(null, null);
        }
        addQty= addQty * multiplier;

        if (addQty != 0) {
            incomeRecContent.setQtyAccepted(incomeRecContent.getQtyAccepted() == null ? addQty : incomeRecContent.getQtyAccepted() + addQty);
        }
        if (incomeRecContent.getQtyAccepted() == null) {
            incomeRecContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
        } else {
            if (incomeRecContent.getQtyAccepted().equals(Double.valueOf(0))) {
                incomeRecContent.setStatus(BaseRecContentStatus.REJECTED);
            } else {
                if (incomeRecContent.getQtyAccepted().compareTo(incomeRecContent.getContentIn().getQty()) == 0 && incomeRecContent.getNomenIn() != null) {
                    incomeRecContent.setStatus(BaseRecContentStatus.DONE);
                } else {
                    incomeRecContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
                }
            }
        }
        DaoMem.getDaoMem().writeLocalDataBaseRec(incomeRec);
        if (addQty == 1) {
            MessageUtils.playSound(R.raw.bottle_one);
        }
        if (addQty > 1) {
            MessageUtils.playSound(R.raw.bottle_many);
        }
        this.lastMark = null;
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
        View container = findViewById(R.id.inclRecIncomeCigaContent);
        incomeRecContentHolder = new IncomeCigaRecContentHolder(container);

        tvAction = (TextView) findViewById(R.id.tvAction);
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.ShowModalAndConfirm(ActIncomeCigaRecContent.this, "Внимание!", "Очистить данные по позиции?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                incomeRecContent.setQtyAccepted(null);
                                incomeRecContent.getBaseRecContentMarkList().clear();
                                lastMark = null;
                                isBoxScanned = false;
                                proceedAddQtyInternal(0);
                                updateDisplayData();
                            }
                        });

            }
        });
        etQtyAccepted = (EditText) findViewById(R.id.etQtyAccepted);
        llAccepted = (LinearLayout) findViewById(R.id.llAccepted);

    }

    private void updateDisplayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    etQtyAccepted.setEnabled(false);
                    llAccepted.setVisibility(View.GONE);
                tvAction.setText("Сканируйте марки этой позиции");
                int countToAddInFuture = 0;
                if (ActIncomeCigaRecContent.this.lastMark != null) {
                    // Посчитать количество добавляемое, в случае успеха сканирования ШК ЕАН (предварительное добавленное колво)
                    countToAddInFuture = DaoMem.getDaoMem().calculateQtyToAdd(ActIncomeCigaRecContent.this.incomeRec, ActIncomeCigaRecContent.this.incomeRecContent, ActIncomeCigaRecContent.this.lastMark);
                }
                incomeRecContentHolder.setItem(incomeRecContent, countToAddInFuture, DocContentArrayAdapter.RECCONTENT_MODE);
            }
        });
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barcode = barcodeReadEvent.getBarcodeData().replace("\u001D", "");
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

    private void pickRec(ActIncomeCigaRecContent actIncomeCigaRecContent, String docId, IncomeRecContent irc, int addQty, String barcode, boolean b, boolean b1) {
        prepareActWithData(docId, irc, addQty, barcode);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }
}
