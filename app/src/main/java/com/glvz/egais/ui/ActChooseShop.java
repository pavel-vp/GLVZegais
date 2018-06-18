package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;

public class ActChooseShop extends Activity {

    private TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseshop);
        setResources();
    }

    private void setResources() {

        TextView textView2 = (TextView) findViewById(R.id.textView2);

        Button buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button buttonLoadDocs = (Button) findViewById(R.id.buttonLoadDocs);
        buttonLoadDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button buttonMainMenu = (Button) findViewById(R.id.buttonMainMenu);
        buttonMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActChooseShop.this, ActMainMenu.class);
                startActivity(intent);
                ActChooseShop.this.finish();
            }
        });

        Button buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActChooseShop.this.finish();
            }
        });
    }


}
