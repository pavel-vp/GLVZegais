package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.CommandIn;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.ui.doc.inv.ActInvList;
import com.glvz.egais.ui.doc.inv.ActInvRec;

import java.util.List;

public class ActCommandList extends Activity {


    private static final String PARENT = "parent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);
        Bundle extras = getIntent().getExtras();
        String parent = extras.getString(ActCommandList.PARENT);

        createButtons(DaoMem.getDaoMem().getListCommands(parent));
    }

    private void createButtons(List<CommandIn> listCommands) {

        LinearLayout ll = (LinearLayout)findViewById(R.id.mainLL);

        for (final CommandIn commandIn : listCommands) {
            Button myButton = new Button(this);
            myButton.setText(commandIn.getName());
            if (commandIn.getUrl() != null && !"".equals(commandIn.getUrl())) {
                // вызываем команду
                myButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent in = new Intent();
                        in.setClass(ActCommandList.this, ActCommandExec.class);
                        in.putExtra(ActCommandExec.ID, commandIn.getId());
                        startActivity(in);
                    }
                });
            } else {
                // вызываем подменю
                myButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent in = new Intent();
                        in.setClass(ActCommandList.this, ActCommandList.class);
                        in.putExtra(ActCommandList.PARENT, commandIn.getId());
                        startActivity(in);
                    }
                });
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
        }


    }


}
