package com.glvz.egais.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
    WebView wvResult;
    private CommandIn commandByID;
    private ProgressDialog pg;

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
        pg.show();
        // Запустить запрос к сервису
        DaoMem.getDaoMem().callToWS(commandByID, barcode, null, new CommandFinishCallback() {
            @Override
            public void finishCommand(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pg.dismiss();
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvResult.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            tvResult.setText(Html.fromHtml(result));
                        }*/
                        TextView tvCaption = (TextView) findViewById(R.id.tvCaption);
                        if ((result.startsWith("https://www.gosuslugi.ru/covid-cert"))||(result.startsWith("https://immune.mos.ru/qr?id"))) {
                            tvCaption.setVisibility(View.GONE);
                            wvResult.getSettings().setJavaScriptEnabled(true);
                            wvResult.setInitialScale(137);
                            wvResult.loadUrl(result);
                        } else {
                            tvCaption.setVisibility(View.VISIBLE);
                            wvResult.loadData(result,"text/html; charset=utf-8","UTF-8"); }
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
        wvResult = (WebView) findViewById(R.id.wvResult);
        pg = new ProgressDialog(this);
        pg.setMessage("Выполняется запрос...");
        pg.setCancelable(false);
        pg.setCanceledOnTouchOutside(false);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callService(barCode);}
            }
        );
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
