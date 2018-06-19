package com.glvz.egais.service;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.IncomeRecContent;

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
        holder.setItem(itemdata);

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

        IncomeRecContent incomeRec;

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

        public void setItem(IncomeRecContent incomeRec) {
            this.incomeRec = incomeRec;
            tvPosition.setText(incomeRec.getPosition().toString());
            tvStatus.setText(incomeRec.getStatus().getMessage());
            tvNameEgais.setText(incomeRec.getIncomeContentIn().getName());
            tvCapacityEgais.setText(incomeRec.getIncomeContentIn().getCapacity());
            tvAlcVolumeEgais.setText(incomeRec.getIncomeContentIn().getAlcVolume());
            tvBottlingDateEgais.setText(incomeRec.getIncomeContentIn().getBottlingDate());
            if (incomeRec.getNomenIn() != null) {
                tvName1c.setText(incomeRec.getNomenIn().getName());
                tvCapacity1c.setText(String.valueOf(incomeRec.getNomenIn().getCapacity()));
            } else {
                tvName1c.setText("");
                tvCapacity1c.setText("");
            }
            tvQty.setText(String.valueOf(incomeRec.getIncomeContentIn().getQty()));
            if (incomeRec.getQtyAccepted() != null) {
                tvQtyAccepted.setText(String.valueOf(incomeRec.getQtyAccepted()));
            } else {
                tvQtyAccepted.setText("");
            }

        }

    }


}
