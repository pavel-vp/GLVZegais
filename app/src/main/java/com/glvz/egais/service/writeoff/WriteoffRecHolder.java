package com.glvz.egais.service.writeoff;

import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.service.DocRecHolder;
import com.glvz.egais.utils.StringUtils;

public class WriteoffRecHolder extends DocRecHolder {

    TextView tvComment;
    TextView tvCntRows;

    public WriteoffRecHolder(View v) {
        super(v);
        this.tvComment = (TextView) v.findViewById(R.id.tvComment);
        this.tvCntRows = (TextView) v.findViewById(R.id.tvCntRows);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        WriteoffRec writeoffRec = (WriteoffRec)rec;
        String comment = writeoffRec.getComment();
        this.tvComment.setText(comment == null ? "" : comment);
        double qty = 0;
        for (BaseRecContent recContent : rec.getRecContentList()) {
            qty = qty + recContent.getQtyAccepted();
        }
        this.tvCntRows.setText("Стр: "+rec.getRecContentList().size()+" / Шт: "+StringUtils.formatQty(qty));
    }

}
