package com.glvz.egais.service;

import android.content.Context;
import com.glvz.egais.model.IncomeRecContent;

public interface TransferCallback {
    void doFinishTransferCallback(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode);
}
