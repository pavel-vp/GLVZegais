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

public abstract class DocArrayAdapter extends ArrayAdapter<BaseRec> {

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

    abstract protected DocRecHolder getHolderOfRow(View row, boolean needCreate);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DocRecHolder holder = null;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResID, parent, false);
            holder = getHolderOfRow(row, true);
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



}
