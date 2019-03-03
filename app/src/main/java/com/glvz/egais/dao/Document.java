package com.glvz.egais.dao;

import com.glvz.egais.integration.model.IncomeContentIn;
import com.glvz.egais.integration.model.IncomeIn;

public interface Document {
    IncomeIn findIncomeInById(String s);

    IncomeContentIn findIncomeContentByMarkDM(String wbRegId, String markDM);

}
