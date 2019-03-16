package com.glvz.egais.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.income.IncomeRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.Date;
import java.util.List;

public abstract class DocContentArrayAdapter extends ArrayAdapter<BaseRecContent> {

    public static final int RECLIST_MODE = 1;
    public static final int RECCONTENT_MODE = 2;

    private List<BaseRecContent> recContents;
    private final Context context;
    private int layoutResID;

    public DocContentArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRecContent> recContents) {
        super(context, resource);
        this.context = context;
        this.layoutResID = resource;
        this.recContents = recContents;
    }

    @Override
    public int getCount() {
        return recContents.size();
    }

    abstract protected DocRecContentHolder getHolderOfRow(View row, boolean needCreate);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DocRecContentHolder holder = null;
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResID, parent, false);
            holder = getHolderOfRow(row,true);
        }
        else
        {
            holder = getHolderOfRow(row, false);
        }

        BaseRecContent itemdata = recContents.get(position);
        holder.setItem(itemdata, 0, RECLIST_MODE);

        if (position % 2 == 1) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.WHITE);
        }

        return row;

    }



}
