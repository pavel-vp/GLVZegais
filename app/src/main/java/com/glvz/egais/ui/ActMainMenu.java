package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.utils.BarcodeObject;
import com.honeywell.aidc.AidcManager;

import java.io.File;

public class ActMainMenu extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
        BarcodeObject.create(this);

        setResources();

        File path = new File(Environment.getExternalStorageDirectory(), getResources().getString(R.string.path_exchange));

        DaoMem.getDaoMem().init(path.getAbsolutePath(), "00-000083");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BarcodeObject.delete();
    }

    private void setResources() {

        Button buttonIncome = (Button) findViewById(R.id.buttonIncome);
        buttonIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActMainMenu.this, ActIncomeList.class);
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

    }



}
