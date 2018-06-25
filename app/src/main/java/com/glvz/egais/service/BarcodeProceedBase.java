package com.glvz.egais.service;

import com.glvz.egais.model.IncomeRec;

public abstract class BarcodeProceedBase {

    public abstract boolean proceedMarkBarCode(IncomeRec incomeRec, String mark);
    public abstract boolean proceedNomenBarCode(IncomeRec incomeRec, String barcodeNomen);

}
