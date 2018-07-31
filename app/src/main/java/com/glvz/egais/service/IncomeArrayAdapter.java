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
import com.glvz.egais.utils.StringUtils;

import java.util.Date;
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

        if (position % 2 == 1) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        return row;

    }

    public static class DocRecHolder{

        TextView tvNumnakl;
        TextView tvDatenakl;
        TextView tvCntRows;
        TextView tvNamepost;
        TextView tvStatus;
        TextView tvExported;
        IncomeRec incomeRec;

        public DocRecHolder(View v) {
            super();
            this.tvNumnakl = (TextView)v.findViewById(R.id.tvNumnakl);
            this.tvDatenakl = (TextView)v.findViewById(R.id.tvDateNakl);
            this.tvCntRows=(TextView)v.findViewById(R.id.tvCntRows);
            this.tvNamepost=(TextView)v.findViewById(R.id.tvNamepost);
            this.tvStatus=(TextView)v.findViewById(R.id.tvStatus);
            this.tvExported = (TextView)v.findViewById(R.id.tvExported);
            v.setTag(this);
        }

        public void setItem(IncomeRec incomeRec) {
            this.incomeRec = incomeRec;
            this.tvNumnakl.setText(incomeRec.getIncomeIn().getNumber());
            Date d = StringUtils.jsonStringToDate(incomeRec.getIncomeIn().getDate());
            this.tvDatenakl.setText("на " + StringUtils.formatDateDisplay( d ) );
            this.tvCntRows.setText("Строк: " + incomeRec.getCntDone() + "/" + incomeRec.getIncomeIn().getContent().length);
            if (this.incomeRec.isExported()) {
                tvExported.setVisibility(View.VISIBLE);
            } else {
                tvExported.setVisibility(View.INVISIBLE);
            }
            switch (incomeRec.getStatus()) {
                case NEW:
                    tvStatus.setTextColor(Color.BLACK);
                    break;
                case INPROGRESS:
                    tvStatus.setTextColor(Color.BLUE);
                    break;
                case REJECTED:
                    tvStatus.setTextColor(Color.RED);
                    break;
                case DONE:
                    tvStatus.setTextColor(Color.rgb(0,200,0));
                    break;
            }
            this.tvStatus.setText("Статус: " + incomeRec.getStatus().getMessage());
            this.tvNamepost.setText(incomeRec.getIncomeIn().getPostName());
        }

    }



}
