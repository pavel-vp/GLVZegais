package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.glvz.egais.R;

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
    }
}
