package com.glvz.egais.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.EAN13;

public class ActIncomeRecContentChangeNomen extends Activity implements BarcodeReader.BarcodeListener {

    TextView tvNameEgais;
    TextView tvCapacityEgais;
    TextView tvAlcVolumeEgais;
    TextView tvBottlingDateEgais;

    TextView tvName1c;
    TextView tvCapacity1c;

    TextView tvName1cNew;
    TextView tvCapacity1cNew;

    IncomeRec incomeRec;
    IncomeRecContent incomeRecContent;

    NomenIn newNomenIn;
    String newBarcode;
    Button btnOk;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomereccontentchangenomen);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeRec.INCOMEREC_WBREGID);
        incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeRec.INCOMERECCONTENT_POSITION);
        incomeRecContent = DaoMem.getDaoMem().getIncomeRecContentByPosition(incomeRec, position);

    }

    private void setResources() {

        tvNameEgais = (TextView) findViewById(R.id.tvNameEgais);
        tvCapacityEgais = (TextView) findViewById(R.id.tvCapacityEgais);
        tvAlcVolumeEgais = (TextView) findViewById(R.id.tvAlcVolumeEgais);
        tvBottlingDateEgais = (TextView) findViewById(R.id.tvBottlingDateEgais);

        tvName1c = (TextView) findViewById(R.id.tvName1c);
        tvCapacity1c = (TextView) findViewById(R.id.tvCapacity1c);

        tvName1cNew = (TextView) findViewById(R.id.tvName1cNew);
        tvCapacity1cNew = (TextView) findViewById(R.id.tvCapacity1cNew);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActIncomeRecContentChangeNomen.this.finish();
            }
        });

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MessageUtils.ShowModalAndConfirm(ActIncomeRecContentChangeNomen.this, "ВНИМАНИЕ!", "Сопоставить с товаром?\n " +
                                "ЕГАИС:\n" +
                                "Наименование: %s\n" +
                                "Емкость: %s\n" +
                                "Крепость: %s\n" +
                                "1C:\n" +
                                "Наименование: %s\n" +
                                "Емкость: %s\n" +
                                "Крепость: %s",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                incomeRecContent.setNomenIn(newNomenIn, newBarcode);
                                Intent intent = new Intent();
                                intent.putExtra(ActIncomeRecContent.NEWBARCODE, newBarcode);
                                setResult(RESULT_OK, intent);
                                ActIncomeRecContentChangeNomen.this.finish();
                            }
                        },
                        incomeRecContent.getIncomeContentIn().getName(),
                        StringUtils.formatQty(incomeRecContent.getIncomeContentIn().getCapacity()),
                        StringUtils.formatQty(incomeRecContent.getIncomeContentIn().getAlcVolume()),
                        newNomenIn.getName(),
                        StringUtils.formatQty(newNomenIn.getCapacity()),
                        StringUtils.formatQty(newNomenIn.getAlcVolume())
                );
            }
        });
    }


    private void updateData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (newNomenIn != null) {
                    btnOk.setEnabled(true);
                    tvName1cNew.setText( newNomenIn.getName());
                    tvCapacity1cNew.setText( String.valueOf( newNomenIn.getCapacity() ) );
                } else {
                    btnOk.setEnabled(false);
                    tvName1cNew.setText( null );
                    tvCapacity1cNew.setText( null );
                }

                tvNameEgais.setText( incomeRecContent.getIncomeContentIn().getName() );
                tvCapacityEgais.setText( String.valueOf( incomeRecContent.getIncomeContentIn().getCapacity() ) );
                tvAlcVolumeEgais.setText( String.valueOf( incomeRecContent.getIncomeContentIn().getAlcVolume() ) );
                tvBottlingDateEgais.setText( incomeRecContent.getIncomeContentIn().getBottlingDate() );

                if (incomeRecContent.getNomenIn() != null) {
                    tvName1c.setText(incomeRecContent.getNomenIn().getName());
                    tvCapacity1c.setText(String.valueOf(incomeRecContent.getNomenIn().getCapacity()));
                } else {
                    tvName1c.setText( null );
                    tvCapacity1c.setText( null );
                }


            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.setCurrentListener(this);
        updateData();
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        // Найти товар по этому ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        if (barCodeType == EAN13) {
            //Сканирование ШК номенклатуры (EAN):
            // Проверить наличие ШК в справочнике номенклатуры 1С.
            final NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcode(barcodeReadEvent.getBarcodeData());
            //Если в номенклатуре нет такого ШК - запрет приемки: звуковой сигнал и сообщение “Штрихкод [указать номер] отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику”. Запретить ввод значения в поле “Принимаемое количество”
            if (nomenIn == null) {
                MessageUtils.showModalMessage(this, "ВНИМАНИЕ!","Штрихкод "+barcodeReadEvent.getBarcodeData()+" отсутствует в номенклатуре 1С.");
                MessageUtils.playSound(R.raw.no_ean);
                newNomenIn = null;
                this.newBarcode = null;
            } else {
                newNomenIn = nomenIn;
                this.newBarcode = barcodeReadEvent.getBarcodeData();
            }
            updateData();
        } else {
            MessageUtils.showToastMessage("Необходимо сканировать ШК товара!");
        }

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
