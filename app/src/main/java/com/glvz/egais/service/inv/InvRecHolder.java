package com.glvz.egais.service.inv;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.service.DocRecHolder;

public class InvRecHolder extends DocRecHolder {
    TextView tvComment;

    public InvRecHolder(View v) {
        super(v);
        this.tvComment = (TextView) v.findViewById(R.id.tvComment);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        InvRec invRec = (InvRec)rec;
        this.tvComment.setText(((InvIn)(invRec.getDocIn())).getComment());
        this.tvCntRows.setText("Строк: " + rec.getRecContentList().size());

    }
}

