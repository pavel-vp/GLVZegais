package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.CommandIn;
import com.glvz.egais.service.CommandFinishCallback;
import com.glvz.egais.ui.doc.income.ActIncomeList;
import com.glvz.egais.utils.BarcodeObject;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActCommandExec extends Activity implements BarcodeReader.BarcodeListener {
    public static final String ID = "id";
    TextView tvResult;
    private CommandIn commandByID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commandexec);
        Bundle extras = getIntent().getExtras();
        String id = extras.getString(ID);
        commandByID = DaoMem.getDaoMem().getCommandByID(id);
        createResources();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isWaitForBarcode()) {
            BarcodeObject.setCurrentListener(this);
        } else {
            callService(null);
        }
    }

    private void callService(String barcode) {
        // Запустить запрос к сервису
        DaoMem.getDaoMem().callToWS(commandByID, barcode, new CommandFinishCallback() {
            @Override
            public void finishCommand(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText(result);
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isWaitForBarcode()) {
            BarcodeObject.setCurrentListener(null);
        }
    }

    private void createResources() {
        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActCommandExec.this.finish();
            }
        });
        TextView caption = (TextView) findViewById(R.id.tvCaption);
        caption.setText(commandByID.getCaption());
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvResult.setText("");

        TextView tvWaitForScan = (TextView) findViewById(R.id.tvWaitForScan);
        if (isWaitForBarcode()) {
            tvWaitForScan.setVisibility(View.VISIBLE);
        } else {
            tvWaitForScan.setVisibility(View.GONE);
        }
    }

    private boolean isWaitForBarcode() {
        for (String param : commandByID.getParams()) {
            if (CommandIn.PARAM_BARCODE.equals(param)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        final String barCode = barcodeReadEvent.getBarcodeData();
        callService(barCode);
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
