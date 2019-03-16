package com.glvz.egais.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.wifi.SyncWiFiFtp;
import com.glvz.egais.ui.doc.income.ActIncomeList;
import com.glvz.egais.ui.doc.move.ActMoveList;
import com.glvz.egais.utils.MessageUtils;

public class ActMainMenu extends Activity {
    ProgressDialog pg ;
    private static final int REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        setResources();

    }


    @Override
    protected void onStart() {
        super.onStart();
        DaoMem.getDaoMem().initDocuments();
        DaoMem.getDaoMem().checkIsNeedToUpdate(this);
        DaoMem.getDaoMem().initDictionary();

    }

    private void setResources() {

        pg = new ProgressDialog(this);
        pg.setMessage("Синхронизация по WiFi...");
        Button buttonIncome = (Button) findViewById(R.id.buttonIncome);
        buttonIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActMainMenu.this, ActIncomeList.class);
                startActivity(intent);
            }
        });
        Button buttonMove = (Button) findViewById(R.id.buttonMove);
        buttonMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActMainMenu.this, ActMoveList.class);
                startActivity(intent);
            }
        });

        Button buttonMainMenu = (Button) findViewById(R.id.buttonBack);
        buttonMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActMainMenu.this.finish();
            }
        });
        Button buttonLoadData = (Button) findViewById(R.id.buttonLoadData);
        buttonLoadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionToLoadData();
            }
        });

    }

    private void checkPermissionToLoadData() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                MessageUtils.showToastMessage("Необходимо дать разрешение!");
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_READ_PHONE_STATE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            loadData();
        }
    }

    private void loadData() {
        pg.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DaoMem.getDaoMem().syncWiFiFtpShopDocs();
                    handleResult(SyncWiFiFtp.SYNC_SUCCESS);
                } catch (RuntimeException e) {
                    Log.e(getLocalClassName(), "error at wifi" ,e);
                    handleResult(SyncWiFiFtp.SYNC_NO_WIFI);
                } catch (Exception e) {
                    Log.e(getLocalClassName(), "error at wifi", e);
                    handleResult(SyncWiFiFtp.SYNC_ERROR);
                }
                DaoMem.getDaoMem().initDocuments();
            }
        }).start();
    }

    private void handleResult(final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pg.dismiss();
                switch (mode){
                    case SyncWiFiFtp.SYNC_SUCCESS: MessageUtils.showToastMessage("Синхрониация по WiFi выполнена");
                            break;
                    case SyncWiFiFtp.SYNC_NO_WIFI: break;
                    case SyncWiFiFtp.SYNC_ERROR: MessageUtils.showModalMessage(ActMainMenu.this, "Ошибка", "Выполните обмен через USB-кабель (WiFi-подключение отсутствует)");
                        break;
                }
            }
        });
    }


}
