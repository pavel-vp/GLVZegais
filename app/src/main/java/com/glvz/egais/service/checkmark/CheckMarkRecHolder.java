package com.glvz.egais.service.checkmark;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.service.DocRecHolder;

public class CheckMarkRecHolder extends DocRecHolder {

    public CheckMarkRecHolder(View v) {
        super(v);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        CheckMarkIn checkMarkIn = (CheckMarkIn)(rec.getDocIn());

    }

}
