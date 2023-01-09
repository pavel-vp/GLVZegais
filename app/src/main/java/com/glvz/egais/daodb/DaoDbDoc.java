package com.glvz.egais.daodb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import com.glvz.egais.MainApp;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoDbDoc {

    private static DaoDbDoc daoDbDoc = null;

    public static DaoDbDoc getDaoDbDoc() {
        if (daoDbDoc == null) {
            daoDbDoc = new DaoDbDoc();
        }
        return daoDbDoc;
    }

    private AppDbHelper appDbHelper;

    private DaoDbDoc() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }

    private Map<String, Object> readDbDocRec(String docId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_DOC,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { docId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            result = new HashMap<>();
            result.put(BaseRec.KEY_STATUS, BaseRecStatus.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_STATUS))));
            result.put(BaseRec.KEY_EXPORTED, Boolean.parseBoolean( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_EXPORTED))));
            result.put(BaseRec.KEY_CNTDONE, Integer.parseInt( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_CNTDONE))));
        }
        cursor.close();
        return result;
    }

    private Map<String, Object> readDbDocRecContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_DOC+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                new String[] { contentId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            result = new HashMap<>();
            result.put(BaseRec.KEY_POS_ID1C, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_ID1C)));
            result.put(BaseRec.KEY_POS_BARCODE, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_BARCODE)));
            result.put(BaseRec.KEY_POS_STATUS, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_STATUS)));
            result.put(BaseRec.KEY_POS_QTYACCEPTED, Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_QTYACCEPTED))));
            result.put(BaseRec.KEY_DOC_CONTENTID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID)));
        }
        cursor.close();
        return result;
    }

    private List<Map<String, Object>> readDbDocRecContentMarks(String contentId) {
        List<Map<String, Object>> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_DOC+DaoMem.CONTENT_MARK,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                new String[] { contentId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            if (result == null) {
                result = new ArrayList<>();
            }
            Map<String, Object> record = new HashMap<>();
            record.put(BaseRec.KEY_DOCID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOCID) ));
            record.put(BaseRec.KEY_DOC_CONTENTID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID)) );
            record.put(BaseRec.KEY_DOC_CONTENT_MARKID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENT_MARKID)) );
            record.put(BaseRec.KEY_POS_MARKSCANNED, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED) ));
            record.put(BaseRec.KEY_POS_MARKSCANNED_ASTYPE, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED_ASTYPE) ));
            record.put(BaseRec.KEY_POS_MARKSCANNEDREAL, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNEDREAL)) );
            record.put(BaseRec.KEY_POS_MARKBOX, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKBOX) ));
            result.add(record);
        }
        cursor.close();
        return result;
    }







    public void readDbDataDoc(BaseRec baseRec) {
        Log.v("DaoDbDoc", "readDbDataDoc start");
        Map<String, Object> dbRec = readDbDocRec(baseRec.getDocId());
        if (dbRec != null) {
            baseRec.setStatus((BaseRecStatus) dbRec.get(BaseRec.KEY_STATUS));
            baseRec.setExported((Boolean) dbRec.get(BaseRec.KEY_EXPORTED));
            baseRec.setCntDone((Integer) dbRec.get(BaseRec.KEY_CNTDONE));
        }

        // пройтись по строкам и прочитать доп.данные
        baseRec.getRecContentList().clear();
        // Сначала читаем по всем входным строкам и создать по ним обертки
        for (DocContentIn docContentIn : baseRec.getDocContentInList()) {
            BaseRecContent recContent = baseRec.buildRecContent(docContentIn);
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(recContent.getId1c()), recContent.getBarcode());
            baseRec.getRecContentList().add(recContent);
        }

        // Прочитать доп данные по входным строкам, плюс дополнительные данные по добаленным строкам
        readLocalDataDocContentAndMerge(baseRec);

        Log.v("DaoDbDoc", "readDbDataDoc end contentSize="+baseRec.getRecContentList().size());
    }

    private void readLocalDataDocContentAndMerge(BaseRec baseRec) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_DOC+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { baseRec.getDocId() },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );

        while(cursor.moveToNext()) {
            String id1c = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_ID1C));
            String barcode = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_BARCODE));
            String posStatus = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_STATUS));
            float qtyAccepted = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_QTYACCEPTED)));
            String docContentId = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID));
            int position = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_POSITION)));

            // Попробовать найти уже созданную строку
            BaseRecContent recContent = null;
            for (BaseRecContent brc : baseRec.getRecContentList()) {

                BaseRecContent ircTemp = (BaseRecContent) brc;
                if (ircTemp.getPosition() != null && Integer.parseInt(ircTemp.getPosition()) == position) {
                    recContent = ircTemp;
                }
            }
            recContent.setId1c(id1c);
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(id1c), barcode);
            recContent.setStatus(BaseRecContentStatus.valueOf(posStatus));
            recContent.setQtyAccepted((double) qtyAccepted);

            Cursor cursorMark = db.query(
                    DaoMem.KEY_DOC+DaoMem.CONTENT_MARK,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                    new String[] { docContentId },                              // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    BaseColumns._ID + " ASC"               // The sort order
            );

            while(cursorMark.moveToNext()) {
                BaseRecContentMark baseRecContentMark = new BaseRecContentMark(
                        cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED)),
                        Integer.parseInt(cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED_ASTYPE))),
                        cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNEDREAL))
                );
                recContent.getBaseRecContentMarkList().add(baseRecContentMark);
            }
            cursorMark.close();
        }
        cursor.close();
    }




    public void saveDbDocRec(BaseRec baseRec) {
        Log.v("DaoDbDoc", "saveDbDocRec start");
        // Синхронизируем общий статус накладной
        // Посчитаем число реально принятых строк
        int cntDone = 0;
        int cntZero = 0;
        for (BaseRecContent recContent : baseRec.getRecContentList()) {
            if (recContent.getStatus() == BaseRecContentStatus.DONE){
                cntDone++;
            }
            if (recContent.getQtyAccepted() != null &&  recContent.getQtyAccepted().equals(Double.valueOf(0))
                    && recContent.getNomenIn() == null){
                cntZero++;
            }
        }
        // Если все записи =0, и связок с товарами нет и в статусе накл = отказ, оставим отказ
        if (cntZero == baseRec.getRecContentList().size()
                && baseRec.getStatus() == BaseRecStatus.REJECTED) {
            // оставим отказ
        } else {
            if (baseRec.getDocContentInList().size() == cntDone) {
                baseRec.setStatus(BaseRecStatus.DONE);
            } else {
                baseRec.setStatus(BaseRecStatus.INPROGRESS);
            }
        }
        baseRec.setCntDone(cntDone);


        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, baseRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_STATUS, baseRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, baseRec.getDocId());
        values.put(BaseRec.KEY_CNTDONE, baseRec.getCntDone());
        Map<String, Object> dbInvRec = readDbDocRec(baseRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRec == null) {
            db.insert(DaoMem.KEY_DOC, null, values);
        } else {
            db.update(DaoMem.KEY_DOC, values, BaseRec.KEY_DOCID + " = ?", new String[] { baseRec.getDocId() });
        }
        Log.v("DaoDbDoc", "saveDbDocRec end");
    }

    public void saveDbDocRecWithContentDeletion(BaseRec baseRec) {
        saveDbDocRec(baseRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(DaoMem.KEY_DOC+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { baseRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_DOC+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { baseRec.getDocId() });
    }

    public void saveDbDocRecContent(BaseRec baseRec, BaseRecContent recContent) {
        Log.v("DaoDbDoc", "saveDbDocRecContent start");
        saveDbDocRec(baseRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, baseRec.getDocId());
        values.put(BaseRec.KEY_POS_ID1C, recContent.getId1c());
        values.put(BaseRec.KEY_POS_BARCODE, recContent.getBarcode());
        values.put(BaseRec.KEY_POS_STATUS, recContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_POSITION, recContent.getPosition().toString());
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, recContent.getBaseRecContentMarkList().size());

        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        values.put(BaseRec.KEY_POS_QTYACCEPTED, qty);
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, recContent.getBaseRecContentMarkList().size());

        String contentId = baseRec.getDocId()+"_"+recContent.getPosition();
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbInvRecContent = readDbDocRecContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRecContent == null) {
            db.insert(DaoMem.KEY_DOC+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_DOC+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }
        if (recContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(DaoMem.KEY_DOC + DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbDocRecContentMarks = readDbDocRecContentMarks(contentId);
            String sql = "INSERT INTO " + DaoMem.KEY_DOC+DaoMem.CONTENT_MARK + "("+BaseRec.KEY_DOCID+","
                    +BaseRec.KEY_DOC_CONTENTID+","
                    +BaseRec.KEY_DOC_CONTENT_MARKID+","
                    +BaseRec.KEY_POS_MARKSCANNED+","
                    +BaseRec.KEY_POS_MARKSCANNED_ASTYPE+","
                    +BaseRec.KEY_POS_MARKSCANNEDREAL+","
                    +BaseRec.KEY_POS_MARKBOX+") VALUES(?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            int idx = 1;
            db.beginTransaction();
            try {
                for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
                    if (dbDocRecContentMarks == null || idx > dbDocRecContentMarks.size()) {
                        statement.clearBindings();
                        statement.bindString(1, baseRec.getDocId());
                        statement.bindString(2, contentId);
                        statement.bindString(3, contentId + "_" + idx);
                        if (baseRecContentMark.getMarkScanned() == null) {
                            statement.bindNull(4);
                        } else {
                            statement.bindString(4, baseRecContentMark.getMarkScanned());
                        }
                        if (baseRecContentMark.getMarkScannedAsType() == null) {
                            statement.bindNull(5);
                        } else {
                            statement.bindString(5, String.valueOf(baseRecContentMark.getMarkScannedAsType()));
                        }
                        if (baseRecContentMark.getMarkScannedReal() == null) {
                            statement.bindNull(6);
                        } else {
                            statement.bindString(6, baseRecContentMark.getMarkScannedReal());
                        }
                        statement.bindNull(7);
                        statement.execute();
                    }
                    idx++;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.v("DaoDbDoc", "saveDbDocRecContent end");
    }



    public void writeLocalDataRec_ClearAllMarks(BaseRec baseRec) {
        for (BaseRecContent recContent : baseRec.getRecContentList()) {
            writeLocalDataRecContent_ClearAllMarks(baseRec.getDocId(), recContent);
        }
    }

    public void writeLocalDataRecContent_ClearAllMarks(String docId, BaseRecContent recContent) {
        String contentId = docId+"_"+recContent.getPosition();
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(DaoMem.KEY_DOC+DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
    }


}
