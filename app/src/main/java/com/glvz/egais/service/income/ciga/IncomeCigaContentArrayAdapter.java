package com.glvz.egais.service.income.ciga;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;

import java.util.List;

public class IncomeCigaContentArrayAdapter extends DocContentArrayAdapter {

    public IncomeCigaContentArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRecContent> recContents) {
        super(context, resource, recContents);
    }

    @Override
    protected DocRecContentHolder getHolderOfRow(View row, boolean needCreate) {
        if (needCreate) {
            return new IncomeCigaRecContentHolder(row);
        } else {
            return (IncomeCigaRecContentHolder)row.getTag();
        }
    }
}

