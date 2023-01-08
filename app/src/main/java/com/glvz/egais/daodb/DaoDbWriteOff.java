package com.glvz.egais.daodb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import com.glvz.egais.MainApp;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.inv.InvRecContentMark;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.model.writeoff.WriteoffRecContentMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoDbWriteOff {
    private static DaoDbWriteOff daoDbWriteOff = null;

    public static DaoDbWriteOff getDaoDbWriteOff() {
        if (daoDbWriteOff == null) {
            daoDbWriteOff = new DaoDbWriteOff();
        }
        return daoDbWriteOff;
    }

    private AppDbHelper appDbHelper;

    private DaoDbWriteOff() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }


    public Map<String, WriteoffRec> readWriteoffRecList(String shopId) {
        Map<String, WriteoffRec> map = new HashMap<>();

        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_SHOPID + " = ?",              // The columns for the WHERE clause
                new String[] { shopId},                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            String docId = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOCID));
            WriteoffRec writeoffRec = readDbDataWriteoff(docId);
            map.put(writeoffRec.getDocId(), writeoffRec);
        }
        cursor.close();
        return map;
    }

    private WriteoffRec readDbDataWriteoff(String docId) {
        WriteoffRec writeoffRec = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { docId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            writeoffRec = new WriteoffRec(docId);
            writeoffRec.setDocNum(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_DOCNUM))));
            writeoffRec.setDateStr(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_DATE))));
            writeoffRec.setTypeDoc(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_TYPEDOC))));
            writeoffRec.setSkladId(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_SKLADID))));
            writeoffRec.setSkladName(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_SKLADNAME))));
            writeoffRec.setComment(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(WriteoffRec.KEY_COMMENT))));
        }
        cursor.close();
        if (writeoffRec != null) {
            List<WriteoffRecContent> recContentList = readDbDataWriteoffContentList(writeoffRec);
            writeoffRec.getRecContentList().addAll(recContentList);
        }
        return writeoffRec;
    }

    private List<WriteoffRecContent> readDbDataWriteoffContentList(WriteoffRec writeoffRec) {
        List<WriteoffRecContent> result = new ArrayList<>();
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { writeoffRec.getDocId() },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        int position = 1;
        while (cursor.moveToNext()) {
            WriteoffRecContent recContent = new WriteoffRecContent(String.valueOf(position), null);

            recContent.setId1c(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_ID1C))));
            String barcode = String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_BARCODE)));
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(recContent.getId1c()), barcode);

            recContent.setStatus(BaseRecContentStatus.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_STATUS))));

            float qtyAccepted = Float.parseFloat( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_QTYACCEPTED)));
            recContent.setQtyAccepted(Double.valueOf(qtyAccepted));

            recContent.getBaseRecContentMarkList().addAll(readDbDataWriteoffContentMarkList(writeoffRec, recContent));

            result.add(recContent);
            position++;
        }
        cursor.close();
        return result;
    }

    private Map<String, Object> readDbWriteoffContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                new String[] { contentId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while (cursor.moveToNext()) {
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

    private List<WriteoffRecContentMark> readDbDataWriteoffContentMarkList(WriteoffRec writeoffRec, WriteoffRecContent recContent) {
        List<WriteoffRecContentMark> result = new ArrayList<>();
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOC_CONTENTID + " = ?",              // The columns for the WHERE clause
                new String[] { writeoffRec.getDocId()+"_"+recContent.getPosition() },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while (cursor.moveToNext()) {
            WriteoffRecContentMark writeoffRecContentMark = new WriteoffRecContentMark(
                    String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED))),
                    Integer.parseInt( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNED_ASTYPE))),
                    String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKSCANNEDREAL))),
                    String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_MARKBOX)))
            );
            result.add(writeoffRecContentMark);
        }
        cursor.close();
        return result;
    }

    private List<Map<String, Object>> readDbWriteoffRecContentMarks(String contentId) {
        List<Map<String, Object>> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK,   // The table to query
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

    public void saveDbWriteoffRec(String shopId, WriteoffRec writeoffRec) {
        Log.v("DaoDbWriteOff", "saveDbWriteoffRec start");
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, writeoffRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_STATUS, writeoffRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, writeoffRec.getDocId());
        values.put(BaseRec.KEY_SHOPID, shopId);

        values.put(WriteoffRec.KEY_DOCNUM, writeoffRec.getDocNum());
        values.put(WriteoffRec.KEY_DATE, writeoffRec.getDateStr());
        values.put(WriteoffRec.KEY_TYPEDOC, writeoffRec.getTypeDoc());
        values.put(WriteoffRec.KEY_SKLADID, writeoffRec.getSkladId());
        values.put(WriteoffRec.KEY_SKLADNAME, writeoffRec.getSkladName());
        values.put(WriteoffRec.KEY_COMMENT, writeoffRec.getComment());
        values.put(WriteoffRec.KEY_DOCNUM, writeoffRec.getDocNum());

        WriteoffRec dbWriteOffRec = readDbDataWriteoff(writeoffRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbWriteOffRec == null) {
            db.insert(DaoMem.KEY_WRITEOFF, null, values);
        } else {
            db.update(DaoMem.KEY_WRITEOFF, values, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
        }
        Log.v("DaoDbWriteOff", "saveDbWriteoffRec end");
    }

    public void saveDbWriteoffRecWithOnlyContentDeletion(String shopId, WriteoffRec writeoffRec) {
        saveDbWriteoffRec(shopId, writeoffRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
    }


    public void saveDbWriteoffRecDeletion(String shopId, WriteoffRec writeoffRec) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRows = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
        int deletedRow = db.delete(DaoMem.KEY_WRITEOFF, BaseRec.KEY_DOCID + " = ?", new String[] { writeoffRec.getDocId() });
    }

    public void removeWriteoffRecContent(String shopId, WriteoffRec writeoffRec, WriteoffRecContent writeoffRecContent) {
        saveDbWriteoffRec(shopId, writeoffRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        String id = writeoffRec.getDocId() + "_" + writeoffRecContent.getId1c() + "_0";
        int deletedRowsMark = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { id });
        int deletedRowsContent = db.delete(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { id });
    }

    public void saveDbWriteoffRecContent(String shopId, WriteoffRec writeoffRec, WriteoffRecContent writeoffRecContent) {
        Log.v("DaoDbWriteoff", "saveDbWriteoffRecContent start");
        saveDbWriteoffRec(shopId, writeoffRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, writeoffRec.getDocId());
        values.put(BaseRec.KEY_POS_ID1C, writeoffRecContent.getId1c());
        values.put(BaseRec.KEY_POS_BARCODE, writeoffRecContent.getBarcode());
        values.put(BaseRec.KEY_POS_STATUS, writeoffRecContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_POSITION, writeoffRecContent.getPosition().toString());
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, writeoffRecContent.getBaseRecContentMarkList().size());

        float qty = 0;
        if (writeoffRecContent.getQtyAccepted() != null) {
            qty = writeoffRecContent.getQtyAccepted().floatValue();
        }
        values.put(BaseRec.KEY_POS_QTYACCEPTED, qty);
        values.put(BaseRec.KEY_POS_MARKSCANNED_CNT, writeoffRecContent.getBaseRecContentMarkList().size());

        String mrcS = String.valueOf(0);
        String contentId = writeoffRec.getDocId()+"_"+writeoffRecContent.getId1c()+"_"+mrcS;
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbInvRecContent = readDbWriteoffContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRecContent == null) {
            db.insert(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_WRITEOFF+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }

        if (writeoffRecContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(DaoMem.KEY_WRITEOFF + DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbWriteoffRecContentMarks = readDbWriteoffRecContentMarks(contentId);
            String sql = "INSERT INTO " + DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK + "("+BaseRec.KEY_DOCID+","
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
                for (BaseRecContentMark baseRecContentMark : writeoffRecContent.getBaseRecContentMarkList()) {
                    if (dbWriteoffRecContentMarks == null || idx > dbWriteoffRecContentMarks.size()) {
                        WriteoffRecContentMark writeoffRecContentMark = (WriteoffRecContentMark) baseRecContentMark;
                        statement.clearBindings();
                        statement.bindString(1, writeoffRec.getDocId());
                        statement.bindString(2, contentId);
                        statement.bindString(3, contentId + "_" + idx);
                        if (writeoffRecContentMark.getMarkScanned() == null) {
                            statement.bindNull(4);
                        } else {
                            statement.bindString(4, writeoffRecContentMark.getMarkScanned());
                        }
                        if (writeoffRecContentMark.getMarkScannedAsType() == null) {
                            statement.bindNull(5);
                        } else {
                            statement.bindString(5, String.valueOf(writeoffRecContentMark.getMarkScannedAsType()));
                        }
                        if (writeoffRecContentMark.getMarkScannedReal() == null) {
                            statement.bindNull(6);
                        } else {
                            statement.bindString(6, writeoffRecContentMark.getMarkScannedReal());
                        }
                        if (writeoffRecContentMark.getBox() == null) {
                            statement.bindNull(7);
                        } else {
                            statement.bindString(7, writeoffRecContentMark.getBox());
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
    }

}
