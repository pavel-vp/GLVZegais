package com.glvz.egais.service;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.model.IncomeRecContentMark;
import com.glvz.egais.utils.BarcodeObject;

import java.util.List;

public class BarcodeProceedPdf417 extends BarcodeProceedBase {

    private IncomeRecContent incomeRecContentProceeded;
    private String lastMark;

    @Override
    public boolean proceedMarkBarCode(IncomeRec incomeRec, String mark) {
        this.incomeRecContentProceeded = null;
        this.lastMark = null;
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        // Проверить что этот ШК ранее не сканировался в данной ТТН

        // алкокод
        String alcCode = BarcodeObject.extractAlcode(mark);
        // Найти этот алкокод в накладной
        List<IncomeRecContent> incomeRecContentList = DaoMem.getDaoMem().findIncomeRecContentListByAlcocode(incomeRec, alcCode);
        if (incomeRecContentList.size() == 0)
            throw new IllegalStateException("Не нашли алкокод накладной!");

        // Найти эту марку в накладной
        IncomeRecContent incomeRecContent = DaoMem.getDaoMem().findIncomeRecContentByMark(incomeRec, mark);
        if (incomeRecContent != null) {
            this.incomeRecContentProceeded = incomeRecContent;
            this.lastMark = mark;
            // Если марка была указана в ТТН ЕГАИС:
            // Проверить полностью ли принята эта позиция
            if (incomeRecContent.getIncomeContentIn().getQty().equals(incomeRecContent.getQtyAccepted())) {
                // если позиция принята полностью:
                //  проверить можно ли одну из марок, ранее принятых по этой позиции перенести на другую позицию этой ТТН с таким же алкокодом
                //  (новая позиция должна быть еще принята не полностью, с приоритетом по совпадению даты розлива,
                //      а переносимая марка - не указана в текущей позиции приходной ТТН ЕГАИС)
            } else {
                // если позиция принята не полностью
                // Проверить наличие на позиции ШК номенклатуры
                if (incomeRecContent.getNomenIn() != null) {
                    // ШК Есть
                    saveRecProceeded(incomeRec, incomeRecContent.getNomenIn(), incomeRecContent.getBarcode());
                } else {
                    // пока не сохраняем
                }
                return true;
            }
            
        }

        return false;
    }

    @Override
    public boolean proceedNomenBarCode(IncomeRec incomeRec, String barcodeNomen) {
        // Обработка сканирования баркода товара (после марки)
        if (this.incomeRecContentProceeded != null) {
            NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcode(barcodeNomen);
            if (nomenIn == null)
                throw new IllegalStateException("Товар с таким ШК не найден!");
            saveRecProceeded(incomeRec, nomenIn, barcodeNomen);
        } else {
            throw new IllegalStateException("Сначала нужно сканировать марку!");
        }
        return true;
    }

    private void saveRecProceeded(IncomeRec incomeRec, NomenIn nomenIn, String barcodeNomen) {
        // добавить марку в список принятых по позиции
        this.incomeRecContentProceeded.getIncomeRecContentMarkList().add(new IncomeRecContentMark(this.lastMark, IncomeRecContentMark.MARK_SCANNED_AS_MARK, this.lastMark));
        //добавить ШК номенклатуры в позицию
        this.incomeRecContentProceeded.setNomenIn(nomenIn, barcodeNomen);
        this.incomeRecContentProceeded.setId1c(nomenIn.getId());
        //добавить в Принятое количество 1 шт.
        this.incomeRecContentProceeded.setQtyAccepted(this.incomeRecContentProceeded.getQtyAccepted() + 1);
        //Завершить обработку марки (выход на следующее сканирование)
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        this.incomeRecContentProceeded = null;
        this.lastMark = null;
    }
}
