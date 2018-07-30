package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.utils.BarcodeObject;

public class ActMainMenu extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        setResources();

        DaoMem.getDaoMem().initDocuments();
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
