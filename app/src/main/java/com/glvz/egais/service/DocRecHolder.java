package com.glvz.egais.service;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;

public abstract class DocRecHolder {
    protected TextView tvNumnakl;
    protected TextView tvDatenakl;
    protected TextView tvCntRows;
    protected TextView tvStatus;
    protected TextView tvExported;
    protected BaseRec rec;

    public DocRecHolder(View v) {
        super();
        this.tvNumnakl = (TextView) v.findViewById(R.id.tvNumnakl);
        this.tvDatenakl = (TextView) v.findViewById(R.id.tvDateNakl);
        this.tvCntRows = (TextView) v.findViewById(R.id.tvCntRows);
        this.tvStatus = (TextView) v.findViewById(R.id.tvStatus);
        this.tvExported = (TextView) v.findViewById(R.id.tvExported);
        v.setTag(this);
    }

    public void setItem(BaseRec rec) {
        this.rec = rec;
        this.tvNumnakl.setText(rec.getDocNum());
        Date d = rec.getDate();
        this.tvDatenakl.setText("на " + StringUtils.formatDateDisplay(d));
        this.tvCntRows.setText("Строк: " + rec.getCntDone() + "/" + rec.getDocContentInList().size());
        if (this.rec.isExported()) {
            tvExported.setVisibility(View.VISIBLE);
        } else {
            tvExported.setVisibility(View.INVISIBLE);
        }
        switch (rec.getStatus()) {
            case NEW:
                tvStatus.setTextColor(Color.BLACK);
                break;
            case INPROGRESS:
                tvStatus.setTextColor(Color.BLUE);
                break;
            case REJECTED:
                tvStatus.setTextColor(Color.RED);
                break;
            case DONE:
                tvStatus.setTextColor(Color.rgb(0, 200, 0));
                break;
        }
        this.tvStatus.setText("Статус: " + rec.getStatusDesc());
    }

}
