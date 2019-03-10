package com.glvz.egais.model.income;

import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentPositionType;

public class IncomeRecContent extends BaseRecContent {

    public IncomeRecContent(String position, DocContentIn docContentIn) {
        super(position, docContentIn);
    }

    @Override
    public BaseRecContentPositionType getPositionType() {
        IncomeContentIn incomeContentIn = (IncomeContentIn)this.contentIn;
        if (incomeContentIn.getCapacity() == null || incomeContentIn.getCapacity().equals("")
                || incomeContentIn.getCapacity().equals("0")) {
            return BaseRecContentPositionType.NONMARKED_LIQUID;
        }
        if (incomeContentIn.getMarked() == null || incomeContentIn.getMarked() == 0) {
            return BaseRecContentPositionType.NONMARKED;
        }
        return BaseRecContentPositionType.MARKED;
    }

    @Override
    public IncomeContentIn getContentIn() {
        return (IncomeContentIn) contentIn;
    }


}
