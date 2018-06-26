package com.glvz.egais.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.IncomeIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.*;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActIncomeRecContent extends Activity implements BarcodeReader.BarcodeListener {

    private String lastMark;
    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;
    private IncomeContentArrayAdapter.DocRecContentHolder docRecContentHolder;

    TextView tvAction;
    EditText etQtyAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomereccontent);

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeRec.INCOMEREC_WBREGID);
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeRec.INCOMERECCONTENT_POSITION);
        this.incomeRecContent = DaoMem.getDaoMem().getIncomeRecContentByPosition(incomeRec, Integer.valueOf(position));
        this.lastMark = extras.getString(ActIncomeRec.INCOMERECCONTENT_LASTMARK);

        setResources();
        checkQtyOnLastMark();
        updateDisplayData();
    }

    private void checkQtyOnLastMark() {
        if (this.lastMark != null) {
            // Проверить: [количество по ТТН] > [Принятое количество]
            if (incomeRecContent.getQtyAccepted() !=null &&
                    incomeRecContent.getIncomeContentIn().getQty().compareTo(incomeRecContent.getQtyAccepted()) <= 0) {
                MessageUtils.showModalMessage( "По позиции [показать номер, алкокод, наименование ЕГАИС] уже принято полное количество [показать]. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику");
            }
        }
    }

    // обработчик добавления марки и/или количества
    private void proceedAddQty(int addQty) {
        if (addQty != 0) {
            incomeRecContent.setQtyAccepted(incomeRecContent.getQtyAccepted() == null ? addQty : incomeRecContent.getQtyAccepted() + addQty);
        }
        if (this.lastMark != null) {
            this.incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(this.lastMark, IncomeRecContentMark.MARK_SCANNED_AS_MARK));
        }
        if (incomeRecContent.getQtyAccepted() == null) {
            incomeRecContent.setStatus(IncomeRecContentStatus.NOT_ENTERED);
        } else {
            if (incomeRecContent.getQtyAccepted().compareTo(incomeRecContent.getIncomeContentIn().getQty()) == 0) {
                incomeRecContent.setStatus(IncomeRecContentStatus.DONE);
            } else {
                incomeRecContent.setStatus(IncomeRecContentStatus.IN_PROGRESS);
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
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        //Для немаркированной продукции кнопка доступна только если ранее была определена номенклатура 1С (по ШК) или если позиция является разливным пивом (Емкость (ЕГАИС) = 0).
        // Для маркированной продукции - кнопка не доступна.
        if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED_LIQUID ||
                (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED &&
                        incomeRecContent.getId1c() != null &&
                        incomeRecContent.getNomenIn() != null )
                ) {
            btnAdd.setEnabled(true);
        } else {
            btnAdd.setEnabled(false);
        }
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: обработчик нажатия ДОбавить
                // Вычисление: [Количество принятое (надпись)] = [Количество принятое (надпись)] + [Принимаемое количество].
                // Перед вычислением производится проверка: Количество принятое - не может быть больше количества по ТТН.
                // Если превышает - сообщение “Принятое количество не может быть больше количества по ТТН”, вычисление не выполняется.
                // После успешного вычисления - возврат в форму “Приход ЕГАИС”
            }
        });
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: обработчик нажатия Очистить
                incomeRecContent.setQtyAccepted(null);
                incomeRecContent.getIncomeRecContentMarkList().clear();
                lastMark = null;
                proceedAddQty(0);
                updateDisplayData();
            }
        });
        etQtyAccepted = (EditText) findViewById(R.id.etQtyAccepted);
        if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.MARKED) {
            etQtyAccepted.setEnabled(false);
        } else {
            etQtyAccepted.setEnabled(true);
        }
    }

    private void updateDisplayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        int addQty = this.lastMark == null ? 0 : 1;
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
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
                    proceedAddQty(0);
                } else {
                    //Если ШК товара найден в номенклатуре 1С - заполнить все надписи формы из номенклатуры 1С (код, наименование, ….)
                    incomeRecContent.setNomenIn(nomenIn);
                    proceedAddQty(addQty);
                }
                updateDisplayData();
                break;
            case PDF417:
                break;
            case DATAMATRIX:
                // Сканирование DataMatrix в карточке позиции
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showModalMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                IncomeRecContent incomeRecContent = ActIncomeRec.proceedDataMatrix(incomeRec, barcodeReadEvent.getBarcodeData());
                if (incomeRecContent != null) {
                    this.incomeRecContent = incomeRecContent;
                    this.lastMark = barcodeReadEvent.getBarcodeData();
                    checkQtyOnLastMark();
                    if (incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQty(1);
                    }

                    updateDisplayData();
                }

                break;
            case CODE128:
                break;

        }

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
