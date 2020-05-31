package com.glvz.egais.service.income.ciga;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.DocRecHolder;

public class IncomeCigaRecHolder extends DocRecHolder {
    TextView tvNameagent;

    public IncomeCigaRecHolder(View v) {
        super(v);
        this.tvNameagent = (TextView) v.findViewById(R.id.tvNameagent);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        this.tvNameagent.setText(rec.getAgentName());
    }
}