package com.glvz.egais.service;

import com.glvz.egais.model.IncomeRec;

public class BarCodeProceedDataMatrix extends BarcodeProceedBase {


    @Override
    public boolean proceedMarkBarCode(IncomeRec incomeRec, String mark) {

        return false;
    }

    @Override
    public boolean proceedNomenBarCode(IncomeRec incomeRec, String barcodeNomen) {
        return false;
    }
}
