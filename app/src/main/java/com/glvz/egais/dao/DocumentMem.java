package com.glvz.egais.dao;

import com.glvz.egais.integration.model.IncomeContentIn;
import com.glvz.egais.integration.model.IncomeContentMarkIn;
import com.glvz.egais.integration.model.IncomeIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentMem implements Document {
    Map<String, IncomeIn> mapIncomeIn = new HashMap<>();

    public DocumentMem(List<IncomeIn> listIncomeIn) {
        for (IncomeIn incomeIn : listIncomeIn) {
            mapIncomeIn.put(incomeIn.getWbRegId(), incomeIn);
        }
    }


    @Override
    public IncomeIn findIncomeInById(String s) {
        return mapIncomeIn.get(s);
    }

    @Override
    public IncomeContentIn findIncomeContentByMarkDM(String wbRegId, String markDM) {
        IncomeIn incomeIn = mapIncomeIn.get(wbRegId);
        for (IncomeContentIn contentIn : incomeIn.getContent()) {
            if (contentIn.getMarkInfo() != null) {
                for (IncomeContentMarkIn contentMarkIn : contentIn.getMarkInfo()) {
                    if (contentMarkIn.getMark().equals(markDM)) {
                        return contentIn;
                    }
                }
            }
        }
        return null;
    }
}
