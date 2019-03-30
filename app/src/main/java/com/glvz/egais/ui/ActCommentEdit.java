package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.glvz.egais.R;

public class ActCommentEdit extends Activity {

    public static final String COMMENT_VALUE = "comment_value";
    EditText edComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentedit);
        setResources();
        Bundle extras = getIntent().getExtras();
        String comment = extras.getString(COMMENT_VALUE);
        edComment.setText(comment);
    }

    private void setResources() {
        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(edComment.getText().toString()));
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        edComment = (EditText)findViewById(R.id.etComment);

    }


}
