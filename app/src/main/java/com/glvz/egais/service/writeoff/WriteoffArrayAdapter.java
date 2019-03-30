package com.glvz.egais.service.writeoff;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.service.DocArrayAdapter;
import com.glvz.egais.service.DocRecHolder;

import java.util.List;

public class WriteoffArrayAdapter extends DocArrayAdapter {
    public WriteoffArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRec> recList) {
        super(context, resource, recList);
    }

    @Override
    protected DocRecHolder getHolderOfRow(View row, boolean needCreate) {
        return new WriteoffRecHolder(row);
    }
}
