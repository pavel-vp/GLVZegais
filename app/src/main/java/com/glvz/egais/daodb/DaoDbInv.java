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
import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.model.inv.InvRecContentMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaoDbInv {
    private static DaoDbInv daoDbInv = null;

    public static DaoDbInv getDaoDbInv() {
        if (daoDbInv == null) {
            daoDbInv = new DaoDbInv();
        }
        return daoDbInv;
    }

    private AppDbHelper appDbHelper;

    private DaoDbInv() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }

    private Map<String, Object> readDbInvRec(String docId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV,   // The table to query
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
        }
        cursor.close();
        return result;
    }

    private Map<String, Object> readDbInvRecContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV+DaoMem.CONTENT,   // The table to query
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
            result.put(BaseRec.KEY_POS_MANUAL_MRC, Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MANUAL_MRC))));
            result.put(BaseRec.KEY_DOC_CONTENTID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID)));
        }
        cursor.close();
        return result;
    }

    private List<Map<String, Object>> readDbInvRecContentMarks(String contentId) {
        List<Map<String, Object>> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV+DaoMem.CONTENT_MARK,   // The table to query
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

    public void readDbDataInv(InvRec invRec) {
        Log.v("DaoMem", "readDbDataInv start");
        Map<String, Object> dbInvRec = readDbInvRec(invRec.getDocId());
        if (dbInvRec != null) {
            invRec.setStatus((BaseRecStatus) dbInvRec.get(BaseRec.KEY_STATUS));
            invRec.setExported((Boolean) dbInvRec.get(BaseRec.KEY_EXPORTED));
        }

        // пройтись по строкам и прочитать доп.данные
        invRec.getRecContentList().clear();
        // Сначала читаем по всем входным строкам и создать по ним обертки
        for (DocContentIn contentIn : invRec.getDocContentInList()) {
            InvContentIn invContentIn = (InvContentIn)contentIn;
            InvRecContent recContent = new InvRecContent(invContentIn.getPosition());
            recContent.setContentIn(invContentIn);
            recContent.setId1c(invContentIn.getNomenId());
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(invContentIn.getNomenId()), null);
            invRec.getRecContentList().add(recContent);
        }

        // Прочитать доп данные по входным строкам, плюс дополнительные данные по добаленным строкам
        readLocalDataInvContentAndMerge(invRec);

        Log.v("DaoMem", "readDbDataInv end contentSize="+invRec.getRecContentList().size());
    }

    private void readLocalDataInvContentAndMerge(InvRec invRec) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { invRec.getDocId() },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );

        while(cursor.moveToNext()) {
            String id1c = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_ID1C));
            String barcode = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_BARCODE));
            String posStatus = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_STATUS));
            float qtyAccepted = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_QTYACCEPTED)));
            float manualMrcFload = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MANUAL_MRC)));
            String docContentId = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID));

            Double manualMrc = manualMrcFload == 0 ? null : (double) manualMrcFload;

            // Попробовать найти уже созданную строку
            InvRecContent recContent = null;
            int maxPos = 0;
            for (BaseRecContent brc : invRec.getRecContentList()) {

                InvRecContent ircTemp = (InvRecContent) brc;
                if (ircTemp.getId1c() != null &&
                        ircTemp.getId1c().equals(id1c) &&
                        (manualMrc == null ||  // не указана ручная введенная МРЦ
                                ircTemp.getContentIn() != null && ircTemp.getContentIn().getMrc() != null && ircTemp.getContentIn().getMrc().equals(manualMrc)  // или она равна искомой
                        )
                ) {
                    recContent = ircTemp;
                }
                maxPos = Math.max(maxPos, Integer.parseInt(ircTemp.getPosition()));
            }
            // Если записи такой нет - то создать ее и добавить (вычислив новую позицию как максимум +1)
            if (recContent == null) {
                recContent = new InvRecContent(String.valueOf(maxPos + 1));
                invRec.getRecContentList().add(recContent);
            }
            recContent.setId1c(id1c);
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(id1c), barcode);
            recContent.setStatus(BaseRecContentStatus.valueOf(posStatus));
            recContent.setQtyAccepted((double) qtyAccepted);
            recContent.setManualMrc(manualMrc);

            Cursor cursorMark = db.query(
                    DaoMem.KEY_INV+DaoMem.CONTENT_MARK,   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                    new String[] { docContentId },                              // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    BaseColumns._ID + " ASC"               // The sort order
            );

            while(cursorMark.moveToNext()) {
                InvRecContentMark baseRecContentMark = new InvRecContentMark(
                        cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED)),
                        Integer.parseInt(cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED_ASTYPE))),
                        cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNEDREAL)),
                        cursorMark.getString(cursorMark.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKBOX))
                );
                recContent.getBaseRecContentMarkList().add(baseRecContentMark);
            }
            cursorMark.close();
        }
        cursor.close();
    }

    public void saveDbInvRec(InvRec invRec) {
        Log.v("DaoMem", "saveDbInvRec start");
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, invRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_STATUS, invRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, invRec.getDocId());
        Map<String, Object> dbInvRec = readDbInvRec(invRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRec == null) {
            db.insert(DaoMem.KEY_INV, null, values);
        } else {
            db.update(DaoMem.KEY_INV, values, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        }
        Log.v("DaoMem", "saveDbInvRec end");
    }

    public void saveDbInvRecWithContentDeletion(InvRec invRec) {
        saveDbInvRec(invRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(DaoMem.KEY_INV+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_INV+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
    }

    public void saveDbInvRecContent(InvRec invRec, InvRecContent recContent) {
        Log.v("DaoMem", "saveDbInvRecContent start");
        saveDbInvRec(invRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, invRec.getDocId());
        values.put(BaseRec.KEY_POS_ID1C, recContent.getId1c());
        values.put(BaseRec.KEY_POS_BARCODE, recContent.getBarcode());
        values.put(BaseRec.KEY_POS_STATUS, recContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_POSITION, recContent.getPosition().toString());
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, recContent.getBaseRecContentMarkList().size());

        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        float manualMrc = 0;
        if (recContent.getManualMrc() != null) {
            manualMrc = recContent.getManualMrc().floatValue();
        } else {
            if (recContent.getContentIn() != null && recContent.getContentIn().getMrc() != null) {
                manualMrc = recContent.getContentIn().getMrc().floatValue();
            }
        }
        values.put(BaseRec.KEY_POS_QTYACCEPTED, qty);
        values.put(BaseRec.KEY_POS_MANUAL_MRC, manualMrc);
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, recContent.getBaseRecContentMarkList().size());

        String mrcS = String.valueOf(manualMrc);
        String contentId = invRec.getDocId()+"_"+recContent.getId1c()+"_"+mrcS;
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbInvRecContent = readDbInvRecContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRecContent == null) {
            db.insert(DaoMem.KEY_INV+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_INV+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }
        if (recContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(DaoMem.KEY_INV + DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbInvRecContentMarks = readDbInvRecContentMarks(contentId);
            String sql = "INSERT INTO " + DaoMem.KEY_INV+DaoMem.CONTENT_MARK + "("+BaseRec.KEY_DOCID+","
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
                    if (dbInvRecContentMarks == null || idx > dbInvRecContentMarks.size()) {
                        InvRecContentMark invRecContentMark = (InvRecContentMark) baseRecContentMark;
                        statement.clearBindings();
                        statement.bindString(1, invRec.getDocId());
                        statement.bindString(2, contentId);
                        statement.bindString(3, contentId + "_" + idx);
                        if (invRecContentMark.getMarkScanned() == null) {
                            statement.bindNull(4);
                        } else {
                            statement.bindString(4, invRecContentMark.getMarkScanned());
                        }
                        if (invRecContentMark.getMarkScannedAsType() == null) {
                            statement.bindNull(5);
                        } else {
                            statement.bindString(5, String.valueOf(invRecContentMark.getMarkScannedAsType()));
                        }
                        if (invRecContentMark.getMarkScannedReal() == null) {
                            statement.bindNull(6);
                        } else {
                            statement.bindString(6, invRecContentMark.getMarkScannedReal());
                        }
                        if (invRecContentMark.getBox() == null) {
                            statement.bindNull(7);
                        } else {
                            statement.bindString(7, invRecContentMark.getBox());
                        }
                        statement.execute();
                    }
                    idx++;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        Log.v("DaoMem", "saveDbInvRecContent end");
    }


    public void deleteRecByDocId(String docId) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRows = db.delete(DaoMem.KEY_INV+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRowsContent = db.delete(DaoMem.KEY_INV+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRow = db.delete(DaoMem.KEY_INV, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
    }

    public void deleteInvNotInList(Set<String> allRemainRecs) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            String docId = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOCID));
            if (!allRemainRecs.contains(docId)) {
                deleteRecByDocId(docId);
            }
        }
    }



}
