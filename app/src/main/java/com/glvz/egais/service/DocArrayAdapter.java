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
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;
import java.util.List;

public class DocArrayAdapter extends ArrayAdapter<BaseRec> {

    List<BaseRec> recList;
    final Context context;
    int layoutResID;


    public DocArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRec> recList) {
        super(context, resource, recList);
        this.context = context;
        this.recList = recList;
        this.layoutResID = resource;
    }

    @Override
    public int getCount() {
        return recList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DocRecHolder holder = null;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResID, parent, false);
            holder = new DocRecHolder(row);
        } else {
            holder = (DocRecHolder) row.getTag();
        }

        BaseRec itemdata = recList.get(position);
        holder.setItem(itemdata);

        if (position % 2 == 1) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        return row;

    }

    public static class DocRecHolder {

        TextView tvNumnakl;
        TextView tvDatenakl;
        TextView tvCntRows;
        TextView tvNameagent;
        TextView tvStatus;
        TextView tvExported;
        BaseRec rec;

        public DocRecHolder(View v) {
            super();
            this.tvNumnakl = (TextView) v.findViewById(R.id.tvNumnakl);
            this.tvDatenakl = (TextView) v.findViewById(R.id.tvDateNakl);
            this.tvCntRows = (TextView) v.findViewById(R.id.tvCntRows);
            this.tvNameagent = (TextView) v.findViewById(R.id.tvNameagent);
            this.tvStatus = (TextView) v.findViewById(R.id.tvStatus);
            this.tvExported = (TextView) v.findViewById(R.id.tvExported);
            v.setTag(this);
        }

        public void setItem(BaseRec rec) {
            this.rec = rec;
            this.tvNumnakl.setText(rec.getDocNum());
            Date d = rec.getDate();
            this.tvDatenakl.setText("на " + StringUtils.formatDateDisplay(d));
            this.tvCntRows.setText("Строк: " + rec.getCntDone() + "/" + rec.getDocContentInList().size());
            if (this.rec.isExported()) {
                tvExported.setVisibility(View.VISIBLE);
            } else {
                tvExported.setVisibility(View.INVISIBLE);
            }
            switch (rec.getStatus()) {
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
                    tvStatus.setTextColor(Color.rgb(0, 200, 0));
                    break;
            }
            this.tvStatus.setText("Статус: " + rec.getStatus().getMessage());
            this.tvNameagent.setText(rec.getAgentName());
        }

    }


}
