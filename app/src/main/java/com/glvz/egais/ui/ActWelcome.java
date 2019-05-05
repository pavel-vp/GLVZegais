package com.glvz.egais.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import com.glvz.egais.BuildConfig;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.UserIn;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActWelcome extends Activity implements BarcodeReader.BarcodeListener {

    ProgressDialog pg ;

    private static final int GLVZ_PERMISSIONS_REQUEST = 1;

    TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarcodeObject.create(this);
        setContentView(R.layout.activity_welcome);
        setResources();
        System.out.println( DaoMem.getDaoMem().getUserIn());
    }

    private void setResources() {
        pg = new ProgressDialog(this);
        pg.setMessage("Синхронизация по WiFi...");
        pg.setCancelable(false);
        tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText("Версия: " + BuildConfig.VERSION_CODE);
    }

    private void proceed(UserIn userIn) {
        DaoMem.getDaoMem().setUserIn(userIn);
        Intent intent = new Intent();
        intent.setClass(ActWelcome.this, ActChooseShop.class);
        startActivity(intent);
        //ActWelcome.this.finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        tryGrantPermition();

    }

    private void tryGrantPermition() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                MessageUtils.showToastMessage("Необходимо дать разрешение!");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GLVZ_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            DaoMem.getDaoMem().checkIsNeedToUpdate(this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GLVZ_PERMISSIONS_REQUEST) {
            DaoMem.getDaoMem().checkIsNeedToUpdate(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        BarcodeObject.linkToListener(this);
        BarcodeObject.setCurrentListener(this);
        loadShared();
    }

    @Override
    public void onPause() {
//        BarcodeObject.unLinkFromListener(this);
        BarcodeObject.setCurrentListener(null);
        super.onPause();
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

    private void loadShared() {
        WifiManager wifi = (WifiManager)(getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        if (wifi != null && wifi.isWifiEnabled()) {
            pg.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DaoMem.getDaoMem().initDictionary();
                    try {
                        DaoMem.getDaoMem().syncWiFiFtpShared();
                    } catch (Exception e) {
                        Log.e(getLocalClassName(), "error at wifi", e);
                    }
                    pg.dismiss();
                    DaoMem.getDaoMem().initDictionary();
                }
            }).start();
        }
    }

}
