package com.glvz.egais.service.price;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.price.PriceRecContent;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class PriceRecContentHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvNomenId;
    TextView tvName;
    TextView tvVolume;
    TextView tvAlc;

    public PriceRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvNomenId = (TextView)v.findViewById(R.id.tvNomenId);
        tvVolume = (TextView)v.findViewById(R.id.tvVolume);
        tvAlc = (TextView)v.findViewById(R.id.tvAlc);
        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        PriceRecContent priceRecContent = (PriceRecContent) recContent;
        llPosition.setVisibility(View.VISIBLE);
        tvPosition.setText(recContent.getPosition().toString());
        if (priceRecContent.getNomenIn() != null) {
            tvName.setText(priceRecContent.getNomenIn().getName());
        } else {
            tvName.setText("");
        }
        if (recContent.getNomenIn() != null) {
            tvNomenId.setText(recContent.getNomenIn().getId());
            if (priceRecContent.getNomenIn().getCapacity() != null) {
                tvVolume.setText(StringUtils.formatQty(priceRecContent.getNomenIn().getCapacity()));
            } else {
                tvVolume.setText("");
            }
            if (priceRecContent.getNomenIn().getAlcVolume() != null) {
                tvAlc.setText(StringUtils.formatQty(priceRecContent.getNomenIn().getAlcVolume()));
            } else {
                tvAlc.setText("");
            }
        } else {
            tvNomenId.setText("");
            tvVolume.setText("");
            tvAlc.setText("");
        }
    }
}
