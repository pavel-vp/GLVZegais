package com.glvz.egais.service.writeoff;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.service.DocRecHolder;

public class WriteoffRecHolder extends DocRecHolder {

    TextView tvComment;
    public WriteoffRecHolder(View v) {
        super(v);
        this.tvComment = (TextView) v.findViewById(R.id.tvComment);

    }
    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        WriteoffRec writeoffRec = (WriteoffRec)rec;
        this.tvComment.setText(writeoffRec.getComment());
    }

}
