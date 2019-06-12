package com.glvz.egais.service.findmark;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.findmark.FindMarkRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class FindMarkRecContentHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvName;
    TextView tvQty;
    TextView tvQtyFound;
    TextView tvNomenId;
    TextView tvVolume;
    TextView tvAlc;

    public FindMarkRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvQtyFound = (TextView)v.findViewById(R.id.tvQtyFound);
        tvNomenId = (TextView)v.findViewById(R.id.tvNomenId);
        tvVolume = (TextView)v.findViewById(R.id.tvVolume);
        tvAlc = (TextView)v.findViewById(R.id.tvAlc);

        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        FindMarkRecContent findMarkRecContent = (FindMarkRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            //tvStatus.setText(recContent.getStatus().getMessage());
        }
        if (findMarkRecContent.getNomenIn() != null) {
            tvName.setText(findMarkRecContent.getNomenIn().getName());
        } else {
            tvName.setText("");
        }
        tvNomenId.setText(recContent.getNomenIn().getId());
        if (findMarkRecContent.getNomenIn().getCapacity() != null) {
            tvVolume.setText(StringUtils.formatQty(findMarkRecContent.getNomenIn().getCapacity() ));
        } else {
            tvVolume.setText("");
        }
        if (findMarkRecContent.getNomenIn().getAlcVolume() != null) {
            tvAlc.setText(StringUtils.formatQty(findMarkRecContent.getNomenIn().getAlcVolume() ));
        } else {
            tvAlc.setText("");
        }

        tvQty.setText(StringUtils.formatQty(findMarkRecContent.getContentIn().getQty()));

        if (findMarkRecContent.getQtyAccepted() != null) {
            tvQtyFound.setText(StringUtils.formatQty(findMarkRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQtyFound.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }
    }
}
