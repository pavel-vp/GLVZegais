package com.glvz.egais.service.move;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.DocRecHolder;

public class MoveRecHolder extends DocRecHolder {

    TextView tvNameagent;

    public MoveRecHolder(View v) {
        super(v);
        this.tvNameagent = (TextView) v.findViewById(R.id.tvNameagent);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        this.tvNameagent.setText(rec.getAgentName());
    }

}
