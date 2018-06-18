package com.glvz.egais.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActIncomeRecContent extends Activity implements BarcodeReader.BarcodeListener {

    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;

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

    }

    private void setResources() {
        View container = findViewById(R.id.inclRecPrihContent);
        IncomeContentArrayAdapter.DocRecContentHolder docRecContentHolder = new IncomeContentArrayAdapter.DocRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        Button btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: обработчик нажатия ДОбавить
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
        // Текст надписи зависит от типа позиции:
        //  если позиция немаркированная и разливная (Емкость (ЕГАИС) = 0), надпись = “Введите Принимаемое количество в декалитрах и нажмите Добавить”
        //  если позиция немаркированная и штучная (Емкость (ЕГАИС) != 0), надпись = “Сканируйте ШК с бутылки, введите Принимаемое количество и нажмите Добавить”
        //  если позиция маркированная - “Сканируйте ШК с бутылки, и марки со всех бутылок только этой позиции”.

    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
