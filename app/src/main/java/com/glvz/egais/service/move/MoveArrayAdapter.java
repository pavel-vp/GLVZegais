package com.glvz.egais.service.move;

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
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;
import java.util.List;

public class MoveArrayAdapter extends ArrayAdapter<MoveRec> {

    List<MoveRec> moveRecList;
    final Context context;
    int layoutResID;


    public MoveArrayAdapter(@NonNull Context context, int resource, @NonNull List<MoveRec> moveRecList) {
        super(context, resource, moveRecList);
        this.context = context;
        this.moveRecList = moveRecList;
        this.layoutResID = resource;
    }

    @Override
    public int getCount() {
        return moveRecList.size();
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

        MoveRec itemdata = moveRecList.get(position);
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
        TextView tvNamepoluch;
        TextView tvStatus;
        TextView tvExported;
        MoveRec moveRec;

        public DocRecHolder(View v) {
            super();
            this.tvNumnakl = (TextView)v.findViewById(R.id.tvNumnakl);
            this.tvDatenakl = (TextView)v.findViewById(R.id.tvDateNakl);
            this.tvCntRows=(TextView)v.findViewById(R.id.tvCntRows);
            this.tvNamepoluch=(TextView)v.findViewById(R.id.tvNamepoluch);
            this.tvStatus=(TextView)v.findViewById(R.id.tvStatus);
            this.tvExported = (TextView)v.findViewById(R.id.tvExported);
            v.setTag(this);
        }

        public void setItem(MoveRec moveRec) {
            this.moveRec = moveRec;
            this.tvNumnakl.setText(moveRec.getMoveIn().getNumber());
            Date d = StringUtils.jsonStringToDate(moveRec.getMoveIn().getDate());
            this.tvDatenakl.setText("на " + StringUtils.formatDateDisplay( d ) );
            this.tvCntRows.setText("Строк: " + moveRec.getCntDone() + "/" + moveRec.getMoveIn().getContent().length);
            if (this.moveRec.isExported()) {
                tvExported.setVisibility(View.VISIBLE);
            } else {
                tvExported.setVisibility(View.INVISIBLE);
            }
            switch (moveRec.getStatus()) {
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
            this.tvStatus.setText("Статус: " + moveRec.getStatus().getMessage());
            this.tvNamepoluch.setText(moveRec.getMoveIn().getPoluchName());
        }

    }


}
