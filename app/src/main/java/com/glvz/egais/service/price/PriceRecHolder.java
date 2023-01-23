package com.glvz.egais.service.price;

import android.view.View;
import android.widget.TextView;

import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.price.PriceRec;
import com.glvz.egais.service.DocRecHolder;
import com.glvz.egais.utils.StringUtils;

public class PriceRecHolder extends DocRecHolder {

    TextView tvComment;
    TextView tvCntRows;

    public PriceRecHolder(View v) {
        super(v);
        this.tvComment = (TextView) v.findViewById(R.id.tvComment);
        this.tvCntRows = (TextView) v.findViewById(R.id.tvCntRows);
    }

    @Override
    public void setItem(BaseRec rec) {
        super.setItem(rec);
        PriceRec priceRec = (PriceRec)rec;
        this.tvComment.setText(priceRec.getComment());
        double qty = 0;
        for (BaseRecContent recContent : rec.getRecContentList()) {
            qty = qty + 1;
        }
        this.tvCntRows.setText("Стр: "+rec.getRecContentList().size()+" / Шт: "+ StringUtils.formatQty(qty));
    }

}
