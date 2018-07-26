package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;

public class ActWelcome extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setResources();
    }

    private void setResources() {
        Button button = (Button) findViewById(R.id.buttonProceed);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActWelcome.this, ActChooseShop.class);
                startActivity(intent);
                ActWelcome.this.finish();
            }
        });
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        tvVersion.setText("Версия: " + DaoMem.getDaoMem().getVersion());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent();
        intent.setClass(ActWelcome.this, ActChooseShop.class);
        startActivity(intent);
        DaoMem.getDaoMem().checkIsNeedToUpdate(this);
        ActWelcome.this.finish();
    }
}
