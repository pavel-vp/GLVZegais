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
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.model.IncomeRecContentPositionType;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActIncomeRecContent extends Activity implements BarcodeReader.BarcodeListener {

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
        incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeRec.INCOMERECCONTENT_POSITION);
        incomeRecContent = DaoMem.getDaoMem().getIncomeRecContentByPosition(incomeRec, Integer.valueOf(position));


        setResources();
        updateDisplayData();
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

            }
        });

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

                docRecContentHolder.setItem(incomeRecContent);
            }
        });
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        switch (barCodeType) {
            case EAN13:
                //Сканирование ШК номенклатуры (EAN):
                // Проверить наличие ШК в справочнике номенклатуры 1С.
                NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcode(barcodeReadEvent.getBarcodeData());
                //Если в номенклатуре нет такого ШК - запрет приемки: звуковой сигнал и сообщение “Штрихкод [указать номер] отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику”. Запретить ввод значения в поле “Принимаемое количество”
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "Штрихкод "+barcodeReadEvent.getBarcodeData()+" отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику");
                    incomeRecContent.setNomenIn(null);
                } else {
                    //Если ШК товара найден в номенклатуре 1С - заполнить все надписи формы из номенклатуры 1С (код, наименование, ….)
                    incomeRecContent.setNomenIn(nomenIn);
                }
                updateDisplayData();
                DaoMem.getDaoMem().writeLocalDataIncomeRecContent(incomeRec.getWbRegId(), incomeRecContent);
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

    }
}
