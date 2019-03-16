package com.glvz.egais.service;

import android.view.View;
import com.glvz.egais.model.BaseRecContent;

public abstract class DocRecContentHolder{

    public DocRecContentHolder(View v) {
        super();
    }

    abstract public void setItem(BaseRecContent recContent, int addMark, int mode);

}
