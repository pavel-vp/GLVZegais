package com.glvz.egais.service;

import android.content.Context;
import com.glvz.egais.model.IncomeRecContent;

public interface PickBottliingDateCallback {
    void onCallbackPickBottlingDate(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode);
}
