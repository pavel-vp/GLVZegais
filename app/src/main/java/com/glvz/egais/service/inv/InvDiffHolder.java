package com.glvz.egais.service.inv;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class InvDiffHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvName;
    TextView tvQty;
    TextView tvQtyAccepted;
    TextView tvStatusRow;
    TextView tvNomenId;
    TextView tvVolume;
    TextView tvAlc;
    TextView tvMrc;
    TextView tvIzl;
    TextView tvNed;

    public InvDiffHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvQtyAccepted = (TextView)v.findViewById(R.id.tvQtyAccepted);
        tvStatusRow = (TextView)v.findViewById(R.id.tvStatusRow);
        tvNomenId = (TextView)v.findViewById(R.id.tvNomenId);
        tvVolume = (TextView)v.findViewById(R.id.tvVolume);
        tvAlc = (TextView)v.findViewById(R.id.tvAlc);
        tvMrc = (TextView)v.findViewById(R.id.tvMrc);
        tvIzl = (TextView)v.findViewById(R.id.tvIzl);
        tvNed = (TextView)v.findViewById(R.id.tvNed);
        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        InvRecContent invRecContent = (InvRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            tvStatusRow.setText(recContent.getStatus().getMessage());
        }
        if (invRecContent.getNomenIn() != null) {
            tvName.setText(invRecContent.getNomenIn().getName());
        } else {
            tvName.setText("");
        }
        if (invRecContent.getContentIn() != null) {
            tvQty.setText(StringUtils.formatQty(invRecContent.getContentIn().getQty()));
        } else {
            tvQty.setText("");
        }

        if (recContent.getQtyAccepted() != null) {
            tvQtyAccepted.setText(StringUtils.formatQty(invRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQtyAccepted.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }
        tvNomenId.setText(recContent.getId1c());
        if (recContent.getNomenIn() != null && recContent.getNomenIn().getCapacity() != null) {
            tvVolume.setText(StringUtils.formatQty(recContent.getNomenIn().getCapacity() ));
        } else {
            tvVolume.setText("");
        }
        if (recContent.getNomenIn() != null && recContent.getNomenIn().getAlcVolume() != null) {
            tvAlc.setText(StringUtils.formatQty(recContent.getNomenIn().getAlcVolume() ));
        } else {
            tvAlc.setText("");
        }
        if (invRecContent.getContentIn() != null && invRecContent.getContentIn().getMrc() != null) {
            tvMrc.setText(StringUtils.formatQty(invRecContent.getContentIn().getMrc() ));
        } else {
            if (invRecContent.getContentIn() == null && invRecContent.getManualMrc() != null) {
                tvMrc.setText(StringUtils.formatQty(invRecContent.getManualMrc() ));
            } else {
                tvMrc.setText("");
            }
        }
        double izl = 0;
        double ned = 0;
        if (invRecContent.getContentIn() != null) {
            ned = invRecContent.getContentIn().getQty().doubleValue();
            if (invRecContent.getQtyAccepted() != null) {
                ned = ned - invRecContent.getQtyAccepted().doubleValue();
                izl = invRecContent.getQtyAccepted().doubleValue();
            }
            izl = izl - invRecContent.getContentIn().getQty().doubleValue();
            if (izl < 0) {
                izl = 0;
            }
        } else {
            if (invRecContent.getQtyAccepted() != null) {
                izl = invRecContent.getQtyAccepted().doubleValue();
            }
        }
        tvIzl.setText(StringUtils.formatQtyNull(izl));
        tvNed.setText(StringUtils.formatQtyNull(ned));


    }
}
