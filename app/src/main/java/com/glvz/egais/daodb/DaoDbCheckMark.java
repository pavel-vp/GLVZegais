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
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkContentIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.model.checkmark.CheckMarkRecContent;
import com.glvz.egais.model.checkmark.CheckMarkRecContentMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaoDbCheckMark {
    private static DaoDbCheckMark daoDbCheckMark = null;

    public static DaoDbCheckMark getDaoDbCheckMark() {
        if (daoDbCheckMark == null) {
            daoDbCheckMark = new DaoDbCheckMark();
        }
        return daoDbCheckMark;
    }

    private AppDbHelper appDbHelper;

    private DaoDbCheckMark() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }

    public void readDbDataCheckMark(CheckMarkRec checkMarkRec) {
        Log.v("DaoMem", "readDbDataCheckMark start");
        Map<String, Object> dbCheckMarkRec = readDbCheckMarkRec(checkMarkRec.getDocId());
        if (dbCheckMarkRec != null) {
            checkMarkRec.setStatus((BaseRecStatus) dbCheckMarkRec.get(BaseRec.KEY_STATUS));
            checkMarkRec.setExported((Boolean) dbCheckMarkRec.get(BaseRec.KEY_EXPORTED));
            checkMarkRec.setCntDone((Integer) dbCheckMarkRec.get(BaseRec.KEY_CNTDONE));
        }

        // пройтись по строкам и прочитать доп.данные
        checkMarkRec.getRecContentList().clear();
        // Сначала читаем по всем входным строкам и создать по ним обертки
        for (DocContentIn contentIn : checkMarkRec.getDocContentInList()) {
            CheckMarkRecContent checkMarkRecContent = (CheckMarkRecContent) checkMarkRec.buildRecContent(contentIn);
            String nomenId = ((CheckMarkContentIn)contentIn).getNomenId();
            checkMarkRecContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(nomenId), null);

            String contentId = checkMarkRec.getDocId()+"_"+checkMarkRecContent.getPosition();

            Map<String, Object> dbCheckRecContent = readDbCheckMarkRecContent(contentId);
            if (dbCheckRecContent != null) {
                float qty = (Float) dbCheckRecContent.get(BaseRec.KEY_POS_QTYACCEPTED);
                if (qty != 0) {
                    checkMarkRecContent.setQtyAccepted(Double.valueOf(qty));
                }
                int cnt = (Integer) dbCheckRecContent.get(BaseRec.KEY_POS_MARKSCANNED);
                if (cnt > 0) {
                    List<Map<String, Object>> dbCheckRecContentMarks = readDbCheckMarkRecContentMarks(contentId);
                    for (Map<String, Object> markRec: dbCheckRecContentMarks) {
                        String mark = (String) markRec.get(BaseRec.KEY_POS_MARKSCANNED);
                        Integer markState = (Integer) markRec.get(BaseRec.KEY_POS_MARKSTATE);
                        checkMarkRecContent.getBaseRecContentMarkList().add(new CheckMarkRecContentMark(mark, BaseRecContentMark.MARK_SCANNED_AS_MARK, mark, markState));
                    }
                }
            }
            checkMarkRec.getRecContentList().add(checkMarkRecContent);
        }
        Log.v("DaoMem", "readDbDataCheckMark end contentSize="+checkMarkRec.getRecContentList().size());
    }

    private Map<String, Object> readDbCheckMarkRec(String docId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_CHECKMARK,   // The table to query
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

    private Map<String, Object> readDbCheckMarkRecContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_CHECKMARK+DaoMem.CONTENT,   // The table to query
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

    private List<Map<String, Object>> readDbCheckMarkRecContentMarks(String contentId) {
        List<Map<String, Object>> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK,   // The table to query
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

    public void saveDbCheckMarkRec(CheckMarkRec checkMarkRec) {
        Log.v("DaoDbCheckMark", "saveDbCheckMarkRec start");
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, checkMarkRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_CNTDONE, String.valueOf(checkMarkRec.getCntDone()));
        values.put(BaseRec.KEY_STATUS, checkMarkRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, checkMarkRec.getDocId());
        Map<String, Object> dbFindMarkRec = readDbCheckMarkRec(checkMarkRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbFindMarkRec == null) {
            db.insert(DaoMem.KEY_CHECKMARK, null, values);
        } else {
            db.update(DaoMem.KEY_CHECKMARK, values, BaseRec.KEY_DOCID + " = ?", new String[] { checkMarkRec.getDocId() });
        }
        Log.v("DaoDbCheckMark", "saveDbCheckMarkRec end");
    }

    public void saveDbCheckMarkRecContent(CheckMarkRec checkMarkRec, CheckMarkRecContent checkMarkRecContent) {
        Log.v("DaoDbCheckMark", "saveDbFindMarkRecContent start");
        saveDbCheckMarkRec(checkMarkRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, checkMarkRec.getDocId());
        values.put(BaseRec.KEY_POS_STATUS, checkMarkRecContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, checkMarkRecContent.getBaseRecContentMarkList().size());

        float qty = 0;
        if (checkMarkRecContent.getQtyAccepted() != null) {
            qty = checkMarkRecContent.getQtyAccepted().floatValue();
        }
        values.put(BaseRec.KEY_POS_QTYACCEPTED, qty);
        float qtyNew = 0;
        if (checkMarkRecContent.getQtyAcceptedNew() != null) {
            qtyNew = checkMarkRecContent.getQtyAcceptedNew().floatValue();
        }
//        values.put(BaseRec.KEY_POS_QTYACCEPTED_NEW, qtyNew);

        String contentId = checkMarkRec.getDocId()+"_"+checkMarkRecContent.getPosition();
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbCheckRecContent = readDbCheckMarkRecContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbCheckRecContent == null) {
            db.insert(DaoMem.KEY_CHECKMARK+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_CHECKMARK+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }
        if (checkMarkRecContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(DaoMem.KEY_CHECKMARK + DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbFindMarkRecContentMarks = readDbCheckMarkRecContentMarks(contentId);
            String sql = "INSERT INTO " + DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK + "("+BaseRec.KEY_DOCID+","
                    +BaseRec.KEY_DOC_CONTENTID+","
                    +BaseRec.KEY_DOC_CONTENT_MARKID+","
                    +BaseRec.KEY_POS_MARKSCANNED+","
                    +BaseRec.KEY_POS_MARKSCANNED_ASTYPE+","
                    +BaseRec.KEY_POS_MARKSCANNEDREAL+","
                    +BaseRec.KEY_POS_MARKBOX+","
                    +BaseRec.KEY_POS_MARKSTATE+") VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = db.compileStatement(sql);
            int idx = 1;
            db.beginTransaction();
            try {
                for (BaseRecContentMark baseRecContentMark : checkMarkRecContent.getBaseRecContentMarkList()) {
                    if (dbFindMarkRecContentMarks == null || idx > dbFindMarkRecContentMarks.size()) {
                        CheckMarkRecContentMark checkMarkRecContentMark = (CheckMarkRecContentMark) baseRecContentMark;
                        statement.clearBindings();
                        statement.bindString(1, checkMarkRec.getDocId());
                        statement.bindString(2, contentId);
                        statement.bindString(3, contentId + "_" + idx);
                        if (checkMarkRecContentMark.getMarkScanned() == null) {
                            statement.bindNull(4);
                        } else {
                            statement.bindString(4, checkMarkRecContentMark.getMarkScanned());
                        }
                        if (checkMarkRecContentMark.getMarkScannedAsType() == null) {
                            statement.bindNull(5);
                        } else {
                            statement.bindString(5, String.valueOf(checkMarkRecContentMark.getMarkScannedAsType()));
                        }
                        if (checkMarkRecContentMark.getMarkScannedReal() == null) {
                            statement.bindNull(6);
                        } else {
                            statement.bindString(6, checkMarkRecContentMark.getMarkScannedReal());
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

        Log.v("DaoDbCheckMark", "saveDbCheckMarkRecContent end");
    }

    public void deleteRecByDocId(String docId) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRows = db.delete(DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRowsContent = db.delete(DaoMem.KEY_CHECKMARK+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRow = db.delete(DaoMem.KEY_CHECKMARK, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
    }

    public void deleteCheckMarkNotInList(Set<String> allRemainRecs) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_CHECKMARK,   // The table to query
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
