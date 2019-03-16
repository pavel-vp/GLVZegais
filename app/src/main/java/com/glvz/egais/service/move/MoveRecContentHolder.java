package com.glvz.egais.service.move;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

public class MoveRecContentHolder extends DocRecContentHolder {

    LinearLayout llPosition;
    TextView tvPosition;
    TextView tvStatus;
    TextView tvName;
    TextView tvQty;
    TextView tvQtyAccepted;

    public MoveRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        //tvStatus = (TextView)v.findViewById(R.id.tvStatus);
        tvName = (TextView)v.findViewById(R.id.tvName);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvQtyAccepted = (TextView)v.findViewById(R.id.tvQtyAccepted);
        v.setTag(this);
    }

    @Override
    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        MoveRecContent moveRecContent = (MoveRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            //tvStatus.setText(recContent.getStatus().getMessage());
        }
        if (moveRecContent.getNomenIn() != null) {
            tvName.setText(moveRecContent.getNomenIn().getName());
        } else {
            tvName.setText("");
        }
        tvQty.setText(StringUtils.formatQty(moveRecContent.getContentIn().getQty()));
        if (recContent.getQtyAccepted() != null) {
            tvQtyAccepted.setText(StringUtils.formatQty(moveRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQtyAccepted.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }

/*        switch (recContent.getStatus()) {
            case NOT_ENTERED:
                tvStatus.setTextColor(Color.BLACK);
                break;
            case IN_PROGRESS:
                tvStatus.setTextColor(Color.BLUE);
                break;
            case REJECTED:
                tvStatus.setTextColor(Color.RED);
                break;
            case DONE:
                tvStatus.setTextColor(Color.GREEN);
                break;
        }*/

    }
}
