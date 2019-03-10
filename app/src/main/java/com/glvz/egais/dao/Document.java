package com.glvz.egais.dao;

import com.glvz.egais.integration.model.doc.income.IncomeContentIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;

public interface Document {
    IncomeIn findIncomeInById(String s);

    IncomeContentIn findIncomeContentByMarkDM(String wbRegId, String markDM);

}
