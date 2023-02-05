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
import com.glvz.egais.integration.model.doc.findmark.FindMarkContentIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.model.findmark.FindMarkRecContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaoDbFindMark {
    private static DaoDbFindMark daoDbFindMark = null;

    public static DaoDbFindMark getDaoDbFindMark() {
        if (daoDbFindMark == null) {
            daoDbFindMark = new DaoDbFindMark();
        }
        return daoDbFindMark;
    }

    private AppDbHelper appDbHelper;

    private DaoDbFindMark() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }

    public void readDbDataFindMark(FindMarkRec findMarkRec) {
        Log.v("DaoDbFindMark", "readDbDataFindMark start");
        Map<String, Object> dbFindMarkRec = readDbFindMarkRec(findMarkRec.getDocId());
        if (dbFindMarkRec != null) {
            findMarkRec.setStatus((BaseRecStatus) dbFindMarkRec.get(BaseRec.KEY_STATUS));
            findMarkRec.setExported((Boolean) dbFindMarkRec.get(BaseRec.KEY_EXPORTED));
            findMarkRec.setCntDone((Integer) dbFindMarkRec.get(BaseRec.KEY_CNTDONE));
        }

        // пройтись по строкам и прочитать доп.данные
        findMarkRec.getRecContentList().clear();
        // Сначала читаем по всем входным строкам и создать по ним обертки
        for (DocContentIn contentIn : findMarkRec.getDocContentInList()) {
            FindMarkRecContent findMarkRecContent = (FindMarkRecContent) findMarkRec.buildRecContent(contentIn);
            String nomenId = ((FindMarkContentIn)contentIn).getNomenId();
            findMarkRecContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(nomenId), null);

            String contentId = findMarkRec.getDocId()+"_"+findMarkRecContent.getPosition();

            Map<String, Object> dbFindRecContent = readDbFindMarkRecContent(contentId);
            if (dbFindRecContent != null) {
                float qty = (Float) dbFindRecContent.get(BaseRec.KEY_POS_QTYACCEPTED);
                if (qty != 0) {
                    findMarkRecContent.setQtyAccepted(Double.valueOf(qty));
                }
//                int cnt = (Integer) dbFindRecContent.get(BaseRec.KEY_POS_MARKSCANNED);
//                if (cnt > 0) {
                    List<Map<String, Object>> dbFindRecContentMarks = readDbFindMarkRecContentMarks(contentId);
                    for (Map<String, Object> markRec: dbFindRecContentMarks) {
                        String mark = (String) markRec.get(BaseRec.KEY_POS_MARKSCANNED);
                        findMarkRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(mark, BaseRecContentMark.MARK_SCANNED_AS_MARK, mark));
                    }
//                }
            }
            findMarkRec.getRecContentList().add(findMarkRecContent);
        }
        Log.v("DaoDbFindMark", "readDbDataFindMark end contentSize="+findMarkRec.getRecContentList().size());
    }

    private Map<String, Object> readDbFindMarkRec(String docId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_FINDMARK,   // The table to query
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

    private Map<String, Object> readDbFindMarkRecContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_FINDMARK+DaoMem.CONTENT,   // The table to query
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

    private List<Map<String, Object>> readDbFindMarkRecContentMarks(String contentId) {
        List<Map<String, Object>> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK,   // The table to query
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


    public void saveDbFindMarkRec(FindMarkRec findMarkRec) {
        Log.v("DaoMem", "saveDbFindMarkRec start");
        int cntDone = 0;
        for (FindMarkRecContent recContent : findMarkRec.getFindMarkRecContentList()) {
            if (recContent.getBaseRecContentMarkList().size() >= recContent.getContentIn().getQty()) {
                cntDone++;
            }
        }
        findMarkRec.setCntDone(cntDone);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, findMarkRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_CNTDONE, String.valueOf(findMarkRec.getCntDone()));
        values.put(BaseRec.KEY_STATUS, findMarkRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, findMarkRec.getDocId());
        Map<String, Object> dbFindMarkRec = readDbFindMarkRec(findMarkRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbFindMarkRec == null) {
            db.insert(DaoMem.KEY_FINDMARK, null, values);
        } else {
            db.update(DaoMem.KEY_FINDMARK, values, BaseRec.KEY_DOCID + " = ?", new String[] { findMarkRec.getDocId() });
        }
        Log.v("DaoMem", "saveDbFindMarkRec end");
    }

    public void saveDbFindMarkRecContent(FindMarkRec findMarkRec, FindMarkRecContent findMarkRecContent) {
        Log.v("DaoMem", "saveDbFindMarkRecContent start");
        saveDbFindMarkRec(findMarkRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, findMarkRec.getDocId());
        values.put(BaseRec.KEY_POS_STATUS, findMarkRecContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, findMarkRecContent.getBaseRecContentMarkList().size());

        float qty = 0;
        if (findMarkRecContent.getQtyAccepted() != null) {
            qty = findMarkRecContent.getQtyAccepted().floatValue();
        }
        values.put(BaseRec.KEY_POS_QTYACCEPTED, qty);

        String contentId = findMarkRec.getDocId()+"_"+findMarkRecContent.getPosition();
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbFindRecContent = readDbFindMarkRecContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbFindRecContent == null) {
            db.insert(DaoMem.KEY_FINDMARK+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_FINDMARK+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }
        if (findMarkRecContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(DaoMem.KEY_FINDMARK + DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbFindMarkRecContentMarks = readDbFindMarkRecContentMarks(contentId);
            String sql = "INSERT INTO " + DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK + "("+BaseRec.KEY_DOCID+","
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
                for (BaseRecContentMark baseRecContentMark : findMarkRecContent.getBaseRecContentMarkList()) {
                    if (dbFindMarkRecContentMarks == null || idx > dbFindMarkRecContentMarks.size()) {
                        statement.clearBindings();
                        statement.bindString(1, findMarkRec.getDocId());
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

        Log.v("DaoMem", "saveDbFindMarkRecContent end");
    }

    public void deleteRecByDocId(String docId) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRows = db.delete(DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRowsContent = db.delete(DaoMem.KEY_FINDMARK+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
        int deletedRow = db.delete(DaoMem.KEY_FINDMARK, BaseRec.KEY_DOCID + " = ?", new String[] { docId });
    }

    public void deleteFindMarkNotInList(Set<String> allRemainRecs) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_FINDMARK,   // The table to query
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
