package com.glvz.egais.service.checkmark;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.DocRecContentHolder;

import java.util.List;

public class CheckMarkContentArrayAdapter extends DocContentArrayAdapter {
    public CheckMarkContentArrayAdapter(@NonNull Context context, int resource, @NonNull List<BaseRecContent> recContents) {
        super(context, resource, recContents);
    }

    @Override
    protected DocRecContentHolder getHolderOfRow(View row, boolean needCreate) {
        if (needCreate) {
            return new CheckMarkRecContentHolder(row);
        } else {
            return (CheckMarkRecContentHolder)row.getTag();
        }
    }
}
