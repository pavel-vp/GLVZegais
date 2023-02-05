package com.glvz.egais.utils;

import com.glvz.egais.integration.model.doc.findmark.FindMarkContentIn;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.model.findmark.FindMarkRecContent;

public class SearchUtil {

    public static class CheckMarkScannedResultForFindMark {
        public boolean scanned;
        public FindMarkRecContent recContent;

        public CheckMarkScannedResultForFindMark(FindMarkRecContent recContent, boolean scanned) {
            this.recContent = recContent;
            this.scanned = scanned;
        }
    }
    public static CheckMarkScannedResultForFindMark checkMarkScannedForFindMark(FindMarkRec rec, String barCode) {
        // сначала проверим, сканиовали ли эту марку уже
        for (FindMarkRecContent recContent : rec.getFindMarkRecContentList()) {
            // в каждой позиции пройтись по сканированным маркам
            for (BaseRecContentMark findMarkRecContentMark : recContent.getBaseRecContentMarkList()) {
                if (findMarkRecContentMark.getMarkScanned().equals(barCode)) {
                    return new CheckMarkScannedResultForFindMark(recContent, true);
                }
            }
        }
        // Если не нашли
        // поищем в начальном документе
        for (FindMarkRecContent recContent : rec.getFindMarkRecContentList()) {
            // в каждой позиции пройтись по маркам во входном документе
            FindMarkContentIn findMarkContentIn = (FindMarkContentIn) recContent.getContentIn();

            for (String mark :  findMarkContentIn.getMark()) {
                if (mark.equals(barCode)) {
                    return new CheckMarkScannedResultForFindMark(recContent, false);
                }
            }
        }
        return null; // ничего не нашли - не сканирован и нет во входном документе

    }


}
