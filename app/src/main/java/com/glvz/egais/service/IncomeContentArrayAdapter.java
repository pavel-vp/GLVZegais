package com.glvz.egais.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;
import java.util.List;

public class IncomeContentArrayAdapter extends ArrayAdapter<IncomeRecContent> {

    List<IncomeRecContent> incomeRecContents;
    final Context context;
    int layoutResID;

    public IncomeContentArrayAdapter(@NonNull Context context, int resource, @NonNull List<IncomeRecContent> incomeRecContents) {
        super(context, resource);
        this.context = context;
        this.layoutResID = resource;
        this.incomeRecContents = incomeRecContents;
    }

    @Override
    public int getCount() {
        return incomeRecContents.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DocRecContentHolder holder = null;
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResID, parent, false);
            holder = new DocRecContentHolder(row);
        }
        else
        {
            holder = (DocRecContentHolder)row.getTag();
        }

        IncomeRecContent itemdata = incomeRecContents.get(position);
        holder.setItem(itemdata, 0);

        if (position % 2 == 1) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        return row;

    }

    public static class DocRecContentHolder{

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

        IncomeRecContent incomeRecContent;

        public DocRecContentHolder(View v) {
            super();
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

        public void setItem(IncomeRecContent incomeRecContent, int addMark) {
            this.incomeRecContent = incomeRecContent;
            tvPosition.setText(incomeRecContent.getPosition().toString());
            tvStatus.setText(incomeRecContent.getStatus().getMessage());
            tvNameEgais.setText(incomeRecContent.getIncomeContentIn().getName());
            tvCapacityEgais.setText(StringUtils.formatQty(incomeRecContent.getIncomeContentIn().getCapacity()));
            tvAlcVolumeEgais.setText(incomeRecContent.getIncomeContentIn().getAlcVolume());
            Date d = StringUtils.jsonBottlingStringToDate(incomeRecContent.getIncomeContentIn().getBottlingDate());
            tvBottlingDateEgais.setText(StringUtils.formatDateDisplay(d));
            if (incomeRecContent.getNomenIn() != null) {
                tvName1c.setText(incomeRecContent.getNomenIn().getName());
                tvCapacity1c.setText(StringUtils.formatQty(incomeRecContent.getNomenIn().getCapacity()));
            } else {
                tvName1c.setText("");
                tvCapacity1c.setText("");
            }
            tvQty.setText(StringUtils.formatQty(incomeRecContent.getIncomeContentIn().getQty()));
            if (incomeRecContent.getQtyAccepted() != null) {
                tvQtyAccepted.setText(StringUtils.formatQty(incomeRecContent.getQtyAccepted() + addMark ));
            } else {
                tvQtyAccepted.setText(addMark ==0 ? "" : String.valueOf(addMark));
            }

            switch (incomeRecContent.getStatus()) {
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


}
