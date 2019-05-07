package com.glvz.egais.service.checkmark;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.checkmark.CheckMarkRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class CheckMarkRecContentHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvName;
    TextView tvQty;

    public CheckMarkRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        CheckMarkRecContent writeoffRecContent = (CheckMarkRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            //tvStatus.setText(recContent.getStatus().getMessage());
        }
        if (writeoffRecContent.getNomenIn() != null) {
            tvName.setText(writeoffRecContent.getNomenIn().getName());
        } else {
            tvName.setText("");
        }
        if (writeoffRecContent.getQtyAccepted() != null) {
            tvQty.setText(StringUtils.formatQty(writeoffRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQty.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }

    }
}