package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.*;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.service.PickBottliingDateCallback;
import com.glvz.egais.service.TransferCallback;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.List;

public class ActIncomeRecContent extends Activity implements BarcodeReader.BarcodeListener, PickBottliingDateCallback, TransferCallback {

    private String lastMark;
    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;
    private IncomeContentArrayAdapter.DocRecContentHolder docRecContentHolder;

    TextView tvAction;
    EditText etQtyAccepted;
    Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomereccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeRec.INCOMEREC_WBREGID);
        IncomeRec ir = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeRec.INCOMERECCONTENT_POSITION);
        IncomeRecContent irc = DaoMem.getDaoMem().getIncomeRecContentByPosition(ir, Integer.valueOf(position));
        String barcode = extras.getString(ActIncomeRec.INCOMERECCONTENT_LASTMARK);
        int addQty = extras.getInt(ActIncomeRec.INCOMERECCONTENT_ADDQTY);

        prepareActWithData(wbRegId, irc, addQty, barcode);


    }

    private void prepareActWithData(String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        this.incomeRecContent = irc;
        this.lastMark = barcode;

        boolean checkMark = checkQtyOnLastMark();
        if (checkMark && this.incomeRecContent.getNomenIn() != null && addQty != 0) {
            // Если товар сопоставлен - сохраняем сразу
            proceedAddQtyInternal(addQty);
        }
        updateDisplayData();
    }

    private boolean checkQtyOnLastMark() {
        if (this.lastMark != null) {
            // Проверить: [количество по ТТН] > [Принятое количество]
            if (incomeRecContent.getQtyAccepted() !=null &&
                    incomeRecContent.getIncomeContentIn().getQty().compareTo(incomeRecContent.getQtyAccepted()) <= 0) {
                String alcocode = BarcodeObject.extractAlcode(this.lastMark);
                MessageUtils.showModalMessage("По позиции номер: %d, алкокод: %s, (%s) уже принято полное количество %s. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику!",
                        incomeRecContent.getPosition(),
                        alcocode,
                        incomeRecContent.getIncomeContentIn().getName(),
                        incomeRecContent.getQtyAccepted()
                );
                this.lastMark = null;
                return false;
            }
        }
        return true;
    }

    // обработчик добавления марки и/или количества
    private void proceedAddQtyInternal(double addQty) {
        if (addQty != 0) {
            incomeRecContent.setQtyAccepted(incomeRecContent.getQtyAccepted() == null ? addQty : incomeRecContent.getQtyAccepted() + addQty);
        }
        if (this.lastMark != null) {
            this.incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(this.lastMark, IncomeRecContentMark.MARK_SCANNED_AS_MARK));
        }
        if (incomeRecContent.getQtyAccepted() == null) {
            incomeRecContent.setStatus(IncomeRecContentStatus.NOT_ENTERED);
        } else {
            if (incomeRecContent.getQtyAccepted().equals(Double.valueOf(0))) {
                incomeRecContent.setStatus(IncomeRecContentStatus.REJECTED);
            } else {
                if (incomeRecContent.getQtyAccepted().compareTo(incomeRecContent.getIncomeContentIn().getQty()) == 0 && incomeRecContent.getNomenIn() != null) {
                    incomeRecContent.setStatus(IncomeRecContentStatus.DONE);
                } else {
                    incomeRecContent.setStatus(IncomeRecContentStatus.IN_PROGRESS);
                }
            }
        }
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        this.lastMark = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.linkToListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.unLinkFromListener(this);
    }

    private void setResources() {
        View container = findViewById(R.id.inclRecPrihContent);
        docRecContentHolder = new IncomeContentArrayAdapter.DocRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вычисление: [Количество принятое (надпись)] = [Количество принятое (надпись)] + [Принимаемое количество].
                // Перед вычислением производится проверка: Количество принятое - не может быть больше количества по ТТН.
                // Если превышает - сообщение “Принятое количество не может быть больше количества по ТТН”, вычисление не выполняется.
                try {
                    double currQty = incomeRecContent.getQtyAccepted() == null ? 0 : incomeRecContent.getQtyAccepted();
                    double addQty = Double.valueOf(etQtyAccepted.getText().toString());
                    if ((currQty + addQty) > incomeRecContent.getIncomeContentIn().getQty()) {
                        MessageUtils.showModalMessage("Принятое количество не может быть больше количества по ТТН");
                    } else {
                        proceedAddQtyInternal(addQty);
                        // После успешного вычисления - возврат в форму “Приход ЕГАИС”
                        ActIncomeRecContent.this.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    etQtyAccepted.setText("");
                    MessageUtils.showModalMessage("Неверное количество!");
                }
            }
        });
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incomeRecContent.setQtyAccepted(null);
                incomeRecContent.getIncomeRecContentMarkList().clear();
                lastMark = null;
                proceedAddQtyInternal(0);
                updateDisplayData();
            }
        });
        etQtyAccepted = (EditText) findViewById(R.id.etQtyAccepted);
    }

    private void updateDisplayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.MARKED) {
                    etQtyAccepted.setEnabled(false);
                } else {
                    etQtyAccepted.setEnabled(true);
                }
                //Для немаркированной продукции кнопка доступна только если ранее была определена номенклатура 1С (по ШК) или если позиция является разливным пивом (Емкость (ЕГАИС) = 0).
                // Для маркированной продукции - кнопка не доступна.
                if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED_LIQUID ||
                        (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED &&
                                incomeRecContent.getNomenIn() != null )
                        ) {
                    btnAdd.setEnabled(true);
                } else {
                    btnAdd.setEnabled(false);
                }
                // Текст надписи зависит от типа позиции:
                switch (incomeRecContent.getPositionType()) {
                    case NONMARKED_LIQUID:
                        tvAction.setText("Введите Принимаемое количество в декалитрах и нажмите Добавить");
                        break;
                    case NONMARKED:
                        tvAction.setText("Сканируйте ШК с бутылки, введите Принимаемое количество и нажмите Добавить");
                        break;
                    case MARKED:
                        tvAction.setText("Сканируйте ШК с бутылки, и марки со всех бутылок только этой позиции");
                }

                docRecContentHolder.setItem(incomeRecContent, lastMark != null);
            }
        });
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        int addQty = this.lastMark != null ? 1 : 0;
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        IncomeRecContent incomeRecContentLocal;
        switch (barCodeType) {
            case EAN13:
                //Сканирование ШК номенклатуры (EAN):
                // Проверить наличие ШК в справочнике номенклатуры 1С.
                NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcode(barcodeReadEvent.getBarcodeData());
                //Если в номенклатуре нет такого ШК - запрет приемки: звуковой сигнал и сообщение “Штрихкод [указать номер] отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику”. Запретить ввод значения в поле “Принимаемое количество”
                if (nomenIn == null) {
                    MessageUtils.showModalMessage("Штрихкод "+barcodeReadEvent.getBarcodeData()+" отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику");
                    incomeRecContent.setNomenIn(null);
                    this.lastMark= null;
                    proceedAddQtyInternal(0);
                } else {
                    //Если ШК товара найден в номенклатуре 1С - заполнить все надписи формы из номенклатуры 1С (код, наименование, ….)
                    incomeRecContent.setNomenIn(nomenIn);
                    proceedAddQtyInternal(addQty);
                }
                updateDisplayData();
                break;
            case PDF417:
                // Сканирование Pdf417 в карточке позиции
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showModalMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                List<IncomeRecContent> incomeRecContentListLocal = ActIncomeRec.proceedPdf417(incomeRec, barcodeReadEvent.getBarcodeData(), this);
                if (incomeRecContentListLocal != null) {

                    if (incomeRecContentListLocal.size() == 1) {
                        proceedWithPdf417AndBarcode(incomeRecContentListLocal.get(0), barcodeReadEvent.getBarcodeData());
                    } else {
                        // тоже выбор дат....
                        ActIncomeRec.pickBottlingDate(this, incomeRec.getWbRegId(), incomeRecContentListLocal, barcodeReadEvent.getBarcodeData(), this);
                    }
                }
                break;
            case DATAMATRIX:
                // Сканирование DataMatrix в карточке позиции
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showModalMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                incomeRecContentLocal = ActIncomeRec.proceedDataMatrix(incomeRec, barcodeReadEvent.getBarcodeData());
                if (incomeRecContentLocal != null) {
                    this.incomeRecContent = incomeRecContentLocal;
                    this.lastMark = barcodeReadEvent.getBarcodeData();
                    boolean resCheck = checkQtyOnLastMark();
                    if (resCheck && this.incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQtyInternal(1);
                    }

                    updateDisplayData();
                }

                break;
            case CODE128:
                break;

        }

    }

    private void proceedWithPdf417AndBarcode(IncomeRecContent icr, String barcode) {
        this.incomeRecContent = icr;
        this.lastMark = barcode;
        boolean resCheck = checkQtyOnLastMark();
        if (resCheck && this.incomeRecContent.getNomenIn() != null) {
            // Если товар сопоставлен - сохраняем сразу
            proceedAddQtyInternal(1);
        }

        updateDisplayData();
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onCallbackPickBottlingDate(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        proceedWithPdf417AndBarcode(irc, barcode);
    }

    @Override
    public void doFinishTransferCallback(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        prepareActWithData(wbRegId, irc, addQty, barcode);
    }
}
