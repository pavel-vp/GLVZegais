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
    TextView tvQtyFound;
    TextView tvQtyFoundNew;
    TextView tvNomenId;
    TextView tvVolume;
    TextView tvAlc;

    public CheckMarkRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvQtyFound = (TextView)v.findViewById(R.id.tvQtyFound);
        tvQtyFoundNew = (TextView)v.findViewById(R.id.tvQtyFoundNew);
        tvNomenId = (TextView)v.findViewById(R.id.tvNomenId);
        tvVolume = (TextView)v.findViewById(R.id.tvVolume);
        tvAlc = (TextView)v.findViewById(R.id.tvAlc);

        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        CheckMarkRecContent checkMarkRecContent = (CheckMarkRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            //tvStatus.setText(recContent.getStatus().getMessage());
        }
        if (checkMarkRecContent.getNomenIn() != null) {
            tvName.setText(checkMarkRecContent.getNomenIn().getName());
            tvNomenId.setText(checkMarkRecContent.getNomenIn().getId());
            if (checkMarkRecContent.getNomenIn().getCapacity() != null) {
                tvVolume.setText(StringUtils.formatQty(checkMarkRecContent.getNomenIn().getCapacity() ));
            } else {
                tvVolume.setText("");
            }
            if (checkMarkRecContent.getNomenIn().getAlcVolume() != null) {
                tvAlc.setText(StringUtils.formatQty(checkMarkRecContent.getNomenIn().getAlcVolume() ));
            } else {
                tvAlc.setText("");
            }

        } else {
            tvName.setText("");
            tvVolume.setText("");
            tvAlc.setText("");
        }

        tvQty.setText(StringUtils.formatQty(checkMarkRecContent.getContentIn().getQty()));

        if (checkMarkRecContent.getQtyAccepted() != null) {
            tvQtyFound.setText(StringUtils.formatQty(checkMarkRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQtyFound.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }
        if (checkMarkRecContent.getQtyAcceptedNew() != null) {
            tvQtyFoundNew.setText(StringUtils.formatQty(checkMarkRecContent.getQtyAcceptedNew()));
        } else {
            tvQtyFoundNew.setText("");
        }

    }
}
