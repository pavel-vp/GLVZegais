package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.UserIn;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActWelcome extends Activity implements BarcodeReader.BarcodeListener, BarcodeObject.CallbackAfterCreateBarcodeReader {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarcodeObject.create(this, this);
        setContentView(R.layout.activity_welcome);
        setResources();
    }

    private void setResources() {
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText("Версия: " + DaoMem.getDaoMem().getVersion());
    }

    private void proceed(UserIn userIn) {
        DaoMem.getDaoMem().setUserIn(userIn);
        Intent intent = new Intent();
        intent.setClass(ActWelcome.this, ActChooseShop.class);
        startActivity(intent);
        ActWelcome.this.finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        DaoMem.getDaoMem().checkIsNeedToUpdate(this);
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

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        final String barcode = barcodeReadEvent.getBarcodeData();
        // Попробовать найти сотрудника с таким ШК
        UserIn userIn = DaoMem.getDaoMem().getDictionary().findUserByBarcode(barcode);
        if (userIn == null) {
            MessageUtils.showToastMessage("Пользователь с таким штрих-кодом не найден!");
        } else {
            proceed(userIn);
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        System.out.println(barcodeFailureEvent);

    }

    @Override
    public void afterCreate() {
        BarcodeObject.linkToListener(this);
    }
}
