package com.glvz.egais.service.writeoff;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class WriteoffRecContentHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvNomenId;
    TextView tvName;
    TextView tvQty;
    TextView tvVolume;
    TextView tvAlc;
    TextView tvMrc;

    public WriteoffRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvNomenId = (TextView)v.findViewById(R.id.tvNomenId);
        tvVolume = (TextView)v.findViewById(R.id.tvVolume);
        tvAlc = (TextView)v.findViewById(R.id.tvAlc);
        tvMrc = (TextView)v.findViewById(R.id.tvMrc);
        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        WriteoffRecContent writeoffRecContent = (WriteoffRecContent) recContent;
        llPosition.setVisibility(View.VISIBLE);
        tvPosition.setText(recContent.getPosition().toString());
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
        if (recContent.getNomenIn() != null) {
            tvNomenId.setText(recContent.getNomenIn().getId());
            if (writeoffRecContent.getNomenIn().getCapacity() != null) {
                tvVolume.setText(StringUtils.formatQty(writeoffRecContent.getNomenIn().getCapacity()));
            } else {
                tvVolume.setText("");
            }
            if (writeoffRecContent.getNomenIn().getAlcVolume() != null) {
                tvAlc.setText(StringUtils.formatQty(writeoffRecContent.getNomenIn().getAlcVolume()));
            } else {
                tvAlc.setText("");
            }
        } else {
            tvNomenId.setText("");
            tvVolume.setText("");
            tvAlc.setText("");
        }
        if (writeoffRecContent.getMrc() != null) {
            tvMrc.setText(StringUtils.formatQty(writeoffRecContent.getMrc()));
        } else {
            tvMrc.setText("");
        }
    }
}
