package com.glvz.egais.dao;

import com.glvz.egais.integration.model.IncomeContentIn;
import com.glvz.egais.integration.model.IncomeIn;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContentMark;

public interface Document {
    IncomeIn findIncomeInById(String s);

    IncomeContentIn findIncomeContentByMarkDM(String wbRegId, String markDM);

}
