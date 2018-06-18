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
import com.glvz.egais.model.IncomeRec;

import java.util.List;

public class IncomeArrayAdapter extends ArrayAdapter<IncomeRec> {

    List<IncomeRec> incomeRecList;
    final Context context;
    int layoutResID;


    public IncomeArrayAdapter(@NonNull Context context, int resource, @NonNull List<IncomeRec> incomeRecList) {
        super(context, resource, incomeRecList);
        this.context = context;
        this.incomeRecList = incomeRecList;
        this.layoutResID = resource;
    }

    @Override
    public int getCount() {
        return incomeRecList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DocRecHolder holder = null;
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResID, parent, false);
            holder = new DocRecHolder(row);
        }
        else
        {
            holder = (DocRecHolder)row.getTag();
        }

        IncomeRec itemdata = incomeRecList.get(position);
        holder.setItem(itemdata);

        return row;

    }

    public static class DocRecHolder{

        TextView tvNumnakl;
        TextView tvDatenakl;
        TextView tvCntRows;
        TextView tvNamepost;
        TextView tvStatus;
        IncomeRec incomeRec;

        public DocRecHolder(View v) {
            super();
            this.tvNumnakl = (TextView)v.findViewById(R.id.tvNumnakl);
            this.tvDatenakl = (TextView)v.findViewById(R.id.tvDateNakl);
            this.tvCntRows=(TextView)v.findViewById(R.id.tvCntRows);
            this.tvNamepost=(TextView)v.findViewById(R.id.tvNamepost);
            this.tvStatus=(TextView)v.findViewById(R.id.tvStatus);
            v.setTag(this);
        }

        public void setItem(IncomeRec incomeRec) {
            this.incomeRec = incomeRec;
            this.tvNumnakl.setText(incomeRec.getIncomeIn().getNumber());
            this.tvDatenakl.setText("на " + incomeRec.getIncomeIn().getDate());
            this.tvCntRows.setText("Строк: " + incomeRec.getCntDone() + "/" + incomeRec.getIncomeIn().getContent().length);
            switch (incomeRec.getStatus()) {
                case NEW:
                    tvStatus.setTextColor(Color.BLACK);
                    break;
                case INPROGRESS:
                    tvStatus.setTextColor(Color.YELLOW);
                    break;
                case REJECTED:
                    tvStatus.setTextColor(Color.RED);
                    break;
                case DONE:
                    tvStatus.setTextColor(Color.GREEN);
                    break;
            }
            this.tvStatus.setText("Статус: " + incomeRec.getStatus().getMessage());
            this.tvNamepost.setText(incomeRec.getIncomeIn().getPostName());
        }

    }



}
