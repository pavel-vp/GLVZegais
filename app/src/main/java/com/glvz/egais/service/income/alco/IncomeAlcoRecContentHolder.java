package com.glvz.egais.service.income.alco;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.income.IncomeRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;

public class IncomeAlcoRecContentHolder extends DocRecContentHolder {
    LinearLayout llPosition;
    LinearLayout llEgaisPart;
    TextView tvPosition;
    TextView tvStatus;
    TextView tvNameEgais;
    TextView tvCapacityEgais;
    TextView tvAlcVolumeEgais;
    TextView tvBottlingDateEgais;
    TextView tvName1c;
    TextView tvCapacity1c;
    TextView tvQty;
    TextView tvQtyAccepted;

    public IncomeAlcoRecContentHolder(View v) {
        super(v);
        llPosition = (LinearLayout)v.findViewById(R.id.llPosition);
        llEgaisPart = (LinearLayout)v.findViewById(R.id.llEgaisPart);
        tvPosition = (TextView)v.findViewById(R.id.tvPosition);
        tvStatus = (TextView)v.findViewById(R.id.tvStatus);
        tvNameEgais = (TextView)v.findViewById(R.id.tvNameEgais);
        tvCapacityEgais = (TextView)v.findViewById(R.id.tvCapacityEgais);
        tvAlcVolumeEgais = (TextView)v.findViewById(R.id.tvAlcVolumeEgais);
        tvBottlingDateEgais = (TextView)v.findViewById(R.id.tvBottlingDateEgais);
        tvName1c = (TextView)v.findViewById(R.id.tvName1c);
        tvCapacity1c = (TextView)v.findViewById(R.id.tvCapacity1c);
        tvQty = (TextView)v.findViewById(R.id.tvQty);
        tvQtyAccepted = (TextView)v.findViewById(R.id.tvQtyAccepted);
        v.setTag(this);
    }

    public void setItem(BaseRecContent recContent, int addMark, int mode) {
        IncomeRecContent incomeRecContent = (IncomeRecContent) recContent;
        if (mode == DocContentArrayAdapter.RECLIST_MODE) {
            llPosition.setVisibility(View.GONE);
        } else {
            llEgaisPart.setBackgroundColor(Color.LTGRAY);
            llPosition.setVisibility(View.VISIBLE);
            tvPosition.setText(recContent.getPosition().toString());
            tvStatus.setText(recContent.getStatus().getMessage());
        }
        tvNameEgais.setText(incomeRecContent.getContentIn().getName());
        tvCapacityEgais.setText(StringUtils.formatQty(incomeRecContent.getContentIn().getCapacity()));
        tvAlcVolumeEgais.setText(StringUtils.formatQty(incomeRecContent.getContentIn().getAlcVolume()));
        Date d = StringUtils.jsonBottlingStringToDate(incomeRecContent.getContentIn().getBottlingDate());
        tvBottlingDateEgais.setText(StringUtils.formatDateDisplay(d));
        if (incomeRecContent.getNomenIn() != null) {
            tvName1c.setText(incomeRecContent.getNomenIn().getName());
            tvCapacity1c.setText(StringUtils.formatQty(incomeRecContent.getNomenIn().getCapacity()));
        } else {
            tvName1c.setText("");
            tvCapacity1c.setText("");
        }
        tvQty.setText(StringUtils.formatQty(incomeRecContent.getContentIn().getQty()));
        if (recContent.getQtyAccepted() != null) {
            tvQtyAccepted.setText(StringUtils.formatQty(incomeRecContent.getQtyAccepted() + addMark ));
        } else {
            tvQtyAccepted.setText(addMark ==0 ? "" : String.valueOf(addMark));
        }

        switch (recContent.getStatus()) {
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
        }


    }

}
