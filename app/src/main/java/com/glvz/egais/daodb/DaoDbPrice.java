package com.glvz.egais.daodb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.glvz.egais.MainApp;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.price.PriceRec;
import com.glvz.egais.model.price.PriceRecContent;
import com.glvz.egais.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaoDbPrice {
    private static DaoDbPrice daoDbPrice = null;

    public static DaoDbPrice getDaoDbPrice() {
        if (daoDbPrice == null) {
            daoDbPrice = new DaoDbPrice();
        }
        return daoDbPrice;
    }

    private AppDbHelper appDbHelper;

    private DaoDbPrice() {
        appDbHelper = new AppDbHelper(MainApp.getContext());
    }

    public Map<String, PriceRec> readPriceRecList(String shopId) {
        Map<String, PriceRec> map = new HashMap<>();

        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_PRICE,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_SHOPID + " = ?",              // The columns for the WHERE clause
                new String[] { shopId},                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            String docId = cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOCID));
            PriceRec priceRec = readDbDataPrice(docId);
            map.put(priceRec.getDocId(), priceRec);
        }
        cursor.close();
        return map;
    }

    private PriceRec readDbDataPrice(String docId) {
        PriceRec priceRec = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_PRICE,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { docId },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        while(cursor.moveToNext()) {
            priceRec = new PriceRec(docId);

            priceRec.setExported(Boolean.parseBoolean(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_EXPORTED)))));
            priceRec.setStatus(BaseRecStatus.valueOf(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_STATUS)))));
            priceRec.setComment(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(PriceRec.KEY_COMMENT))));
            priceRec.setDate(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(PriceRec.KEY_DATE))));
        }
        cursor.close();
        if (priceRec != null) {
            List<PriceRecContent> recContentList = readDbDataPriceContentList(priceRec);
            priceRec.getRecContentList().addAll(recContentList);
        }
        return priceRec;
    }

    private List<PriceRecContent> readDbDataPriceContentList(PriceRec priceRec) {
        List<PriceRecContent> result = new ArrayList<>();
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_PRICE+DaoMem.CONTENT,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                BaseRec.KEY_DOCID + " = ?",              // The columns for the WHERE clause
                new String[] { priceRec.getDocId() },                              // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                BaseColumns._ID + " ASC"               // The sort order
        );
        int position = 1;
        while (cursor.moveToNext()) {
            PriceRecContent recContent = new PriceRecContent(String.valueOf(position), null);

            recContent.setId1c(String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_ID1C))));
            String barcode = String.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_BARCODE)));
            recContent.setNomenIn(DaoMem.getDaoMem().findNomenInByNomenId(recContent.getId1c()), barcode);

            recContent.setStatus(BaseRecContentStatus.valueOf( cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_POS_STATUS))));

            result.add(recContent);
            position++;
        }
        cursor.close();
        return result;
    }

    private Map<String, Object> readDbPriceContent(String contentId) {
        Map<String, Object> result = null;
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_PRICE+DaoMem.CONTENT,   // The table to query
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
            result.put(BaseRec.KEY_DOC_CONTENTID, cursor.getString(cursor.getColumnIndexOrThrow(BaseRec.KEY_DOC_CONTENTID)));
        }
        cursor.close();
        return result;
    }

    public void saveDbPriceRec(String shopId, PriceRec priceRec) {
        Log.v("DaoDbPrice", "saveDbPriceRec start");
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, priceRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_STATUS, priceRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, priceRec.getDocId());
        values.put(BaseRec.KEY_SHOPID, shopId);

        values.put(PriceRec.KEY_DATE, StringUtils.formatDateDisplay(priceRec.getDate()));
        values.put(PriceRec.KEY_COMMENT, priceRec.getComment());

        PriceRec dbPriceRec = readDbDataPrice(priceRec.getDocId());

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbPriceRec == null) {
            db.insert(DaoMem.KEY_PRICE, null, values);
        } else {
            db.update(DaoMem.KEY_PRICE, values, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
        }
        Log.v("DaoDbPrice", "saveDbPriceRec end");
    }

    public void saveDbPriceRecWithOnlyContentDeletion(String shopId, PriceRec priceRec) {
        saveDbPriceRec(shopId, priceRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
    }


    public void saveDbPriceRecDeletion(String shopId, PriceRec priceRec) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRows = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
        int deletedRowsContent = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
        int deletedRow = db.delete(DaoMem.KEY_PRICE, BaseRec.KEY_DOCID + " = ?", new String[] { priceRec.getDocId() });
    }

    public void removePriceRecContent(String shopId, PriceRec priceRec, PriceRecContent priceRecContent) {
        saveDbPriceRec(shopId, priceRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        String id = priceRec.getDocId() + "_" + priceRecContent.getId1c() + "_0";
        int deletedRowsMark = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { id });
        int deletedRowsContent = db.delete(DaoMem.KEY_PRICE+DaoMem.CONTENT, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { id });
    }

    public void saveDbPriceRecContent(String shopId, PriceRec priceRec, PriceRecContent priceRecContent) {
        Log.v("DaoDbPrice", "saveDbPriceRecContent start");
        saveDbPriceRec(shopId, priceRec);
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, priceRec.getDocId());
        values.put(BaseRec.KEY_POS_ID1C, priceRecContent.getId1c());
        values.put(BaseRec.KEY_POS_BARCODE, priceRecContent.getBarcode());
        values.put(BaseRec.KEY_POS_STATUS, priceRecContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_POSITION, priceRecContent.getPosition().toString());

        String mrcS = String.valueOf(0);
        String contentId = priceRec.getDocId()+"_"+priceRecContent.getId1c()+"_"+mrcS;
        values.put(BaseRec.KEY_DOC_CONTENTID, contentId);

        Map<String, Object> dbInvRecContent = readDbPriceContent(contentId);

        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        if (dbInvRecContent == null) {
            db.insert(DaoMem.KEY_PRICE+DaoMem.CONTENT, null, values);
        } else {
            db.update(DaoMem.KEY_PRICE+DaoMem.CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }

    }


}
