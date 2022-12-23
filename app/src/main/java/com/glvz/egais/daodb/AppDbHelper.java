package com.glvz.egais.daodb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;

public class AppDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GLVZ.db";

    private static final String SQL_CREATE_INV =
            "CREATE TABLE " + DaoMem.KEY_INV + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT)";
    private static final String SQL_CREATE_INV_INDEX =
            "CREATE INDEX "+DaoMem.KEY_INV+"_index ON " + DaoMem.KEY_INV + " ("+BaseRec.KEY_DOCID+")";

    private static final String SQL_CREATE_INV_CONTENT =
            "CREATE TABLE " + DaoMem.KEY_INV + DaoMem.CONTENT + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_POS_ID1C + " TEXT," +
                    BaseRec.KEY_POS_BARCODE + " TEXT," +
                    BaseRec.KEY_POS_STATUS + " TEXT," +
                    BaseRec.KEY_POS_QTYACCEPTED + " TEXT," +
                    BaseRec.KEY_POS_MANUAL_MRC + " TEXT," +
                    BaseRec.KEY_POS_POSITION + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_CNT + " TEXT)";
    private static final String SQL_CREATE_INV_CONTENT_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT+"_index_1 ON " + DaoMem.KEY_INV + DaoMem.CONTENT + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_INV_CONTENT_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT+"_index_2 ON " + DaoMem.KEY_INV + DaoMem.CONTENT + " ("+BaseRec.KEY_DOC_CONTENTID+")";

    private static final String SQL_CREATE_INV_CONTENT_MARK =
            "CREATE TABLE " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK +" (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENT_MARKID + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_ASTYPE + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNEDREAL + " TEXT," +
                    BaseRec.KEY_POS_MARKBOX + " TEXT)";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_INV = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV;
    private static final String SQL_DELETE_INV_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV+DaoMem.CONTENT;
    private static final String SQL_DELETE_INV_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV+DaoMem.CONTENT_MARK;

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_INV);
        db.execSQL(SQL_CREATE_INV_INDEX);

        db.execSQL(SQL_CREATE_INV_CONTENT);
        db.execSQL(SQL_CREATE_INV_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_INV_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_INV_CONTENT_MARK);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_3);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_INV_CONTENT_MARK);
        db.execSQL(SQL_DELETE_INV_CONTENT);
        db.execSQL(SQL_DELETE_INV);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}