package com.glvz.egais.service.income;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;

import java.util.List;

public class IncomeContentArrayAdapter extends DocContentArrayAdapter {

    public IncomeContentArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRecContent> recContents) {
        super(context, resource, recContents);
    }

    @Override
    protected DocRecContentHolder getHolderOfRow(View row, boolean needCreate) {
        if (needCreate) {
            return new IncomeRecContentHolder(row);
        } else {
            return (IncomeRecContentHolder)row.getTag();
        }
    }
}