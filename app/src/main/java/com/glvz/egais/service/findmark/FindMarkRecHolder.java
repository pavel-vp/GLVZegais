package com.glvz.egais.service.findmark;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.DocRecHolder;

public class FindMarkRecHolder  extends DocRecHolder {
    TextView tvComment;

    public FindMarkRecHolder(View v) {
        super(v);
        TextView tvNameagent = (TextView)v.findViewById(R.id.tvNameagent);
        tvNameagent.setVisibility(View.INVISIBLE);
        tvComment = (TextView)v.findViewById(R.id.tvComment);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        FindMarkIn findMarkIn = (FindMarkIn)(rec.getDocIn());
        tvComment.setText(findMarkIn.getComment());
    }

}
