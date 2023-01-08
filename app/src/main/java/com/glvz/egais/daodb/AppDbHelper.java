package com.glvz.egais.daodb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.writeoff.WriteoffRec;

public class AppDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GLVZ.db";

    //
    // INV
    //
    private static final String SQL_CREATE_INV =
            "CREATE TABLE " + DaoMem.KEY_INV + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_SHOPID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT," +
                    BaseRec.KEY_CNTDONE + " TEXT)";
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
                    BaseRec.KEY_POS_MARKBOX + " TEXT," +
                    BaseRec.KEY_POS_MARKSTATE + " TEXT)";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_INV_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_INV+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_INV + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_INV = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV;
    private static final String SQL_DELETE_INV_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV+DaoMem.CONTENT;
    private static final String SQL_DELETE_INV_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_INV+DaoMem.CONTENT_MARK;

    //
    // FINDMARK
    //
    private static final String SQL_CREATE_FINDMARK =
            "CREATE TABLE " + DaoMem.KEY_FINDMARK + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_SHOPID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT," +
                    BaseRec.KEY_CNTDONE + " TEXT)";
    private static final String SQL_CREATE_FINDMARK_INDEX =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+"_index ON " + DaoMem.KEY_FINDMARK + " ("+BaseRec.KEY_DOCID+")";

    private static final String SQL_CREATE_FINDMARK_CONTENT =
            "CREATE TABLE " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT + " (" +
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
    private static final String SQL_CREATE_FINDMARK_CONTENT_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+DaoMem.CONTENT+"_index_1 ON " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_FINDMARK_CONTENT_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+DaoMem.CONTENT+"_index_2 ON " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT + " ("+BaseRec.KEY_DOC_CONTENTID+")";

    private static final String SQL_CREATE_FINDMARK_CONTENT_MARK =
            "CREATE TABLE " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT_MARK +" (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENT_MARKID + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_ASTYPE + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNEDREAL + " TEXT," +
                    BaseRec.KEY_POS_MARKBOX + " TEXT," +
                    BaseRec.KEY_POS_MARKSTATE + " TEXT)";
    private static final String SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_FINDMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_FINDMARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_FINDMARK;
    private static final String SQL_DELETE_FINDMARK_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_FINDMARK+DaoMem.CONTENT;
    private static final String SQL_DELETE_FINDMARK_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_FINDMARK+DaoMem.CONTENT_MARK;

    //
    // CHECKMARK
    //
    private static final String SQL_CREATE_CHECKMARK =
            "CREATE TABLE " + DaoMem.KEY_CHECKMARK + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_SHOPID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT," +
                    BaseRec.KEY_CNTDONE + " TEXT)";
    private static final String SQL_CREATE_CHECKMARK_INDEX =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+"_index ON " + DaoMem.KEY_CHECKMARK + " ("+BaseRec.KEY_DOCID+")";

    private static final String SQL_CREATE_CHECKMARK_CONTENT =
            "CREATE TABLE " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT + " (" +
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
    private static final String SQL_CREATE_CHECKMARK_CONTENT_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+DaoMem.CONTENT+"_index_1 ON " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_CHECKMARK_CONTENT_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+DaoMem.CONTENT+"_index_2 ON " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT + " ("+BaseRec.KEY_DOC_CONTENTID+")";

    private static final String SQL_CREATE_CHECKMARK_CONTENT_MARK =
            "CREATE TABLE " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT_MARK +" (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENT_MARKID + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_ASTYPE + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNEDREAL + " TEXT," +
                    BaseRec.KEY_POS_MARKBOX + " TEXT," +
                    BaseRec.KEY_POS_MARKSTATE + " TEXT)";
    private static final String SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_CHECKMARK + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_CHECKMARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_CHECKMARK;
    private static final String SQL_DELETE_CHECKMARK_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_CHECKMARK+DaoMem.CONTENT;
    private static final String SQL_DELETE_CHECKMARK_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_CHECKMARK+DaoMem.CONTENT_MARK;

    //
    // WRITEOFF
    //
    private static final String SQL_CREATE_WRITEOFF =
            "CREATE TABLE " + DaoMem.KEY_WRITEOFF + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_SHOPID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT," +
                    WriteoffRec.KEY_DOCNUM + " TEXT," +
                    WriteoffRec.KEY_DATE + " TEXT," +
                    WriteoffRec.KEY_TYPEDOC + " TEXT," +
                    WriteoffRec.KEY_SKLADID + " TEXT," +
                    WriteoffRec.KEY_SKLADNAME + " TEXT," +
                    WriteoffRec.KEY_COMMENT + " TEXT," +
                    BaseRec.KEY_CNTDONE + " TEXT)";
    private static final String SQL_CREATE_WRITEOFF_INDEX =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+"_index ON " + DaoMem.KEY_WRITEOFF + " ("+BaseRec.KEY_DOCID+")";

    private static final String SQL_CREATE_WRITEOFF_CONTENT =
            "CREATE TABLE " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT + " (" +
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
    private static final String SQL_CREATE_WRITEOFF_CONTENT_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+DaoMem.CONTENT+"_index_1 ON " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_WRITEOFF_CONTENT_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+DaoMem.CONTENT+"_index_2 ON " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT + " ("+BaseRec.KEY_DOC_CONTENTID+")";

    private static final String SQL_CREATE_WRITEOFF_CONTENT_MARK =
            "CREATE TABLE " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT_MARK +" (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENT_MARKID + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_ASTYPE + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNEDREAL + " TEXT," +
                    BaseRec.KEY_POS_MARKBOX + " TEXT," +
                    BaseRec.KEY_POS_MARKSTATE + " TEXT)";
    private static final String SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_WRITEOFF + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_WRITEOFF = "DROP TABLE IF EXISTS " + DaoMem.KEY_WRITEOFF;
    private static final String SQL_DELETE_WRITEOFF_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_WRITEOFF+DaoMem.CONTENT;
    private static final String SQL_DELETE_WRITEOFF_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_WRITEOFF+DaoMem.CONTENT_MARK;

    //
    // DOC
    //
    private static final String SQL_CREATE_DOC =
            "CREATE TABLE " + DaoMem.KEY_DOC + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_SHOPID + " TEXT," +
                    BaseRec.KEY_STATUS + " TEXT," +
                    BaseRec.KEY_EXPORTED + " TEXT," +
                    BaseRec.KEY_CNTDONE + " TEXT)";
    private static final String SQL_CREATE_DOC_INDEX =
            "CREATE INDEX "+DaoMem.KEY_DOC+"_index ON " + DaoMem.KEY_DOC + " ("+BaseRec.KEY_DOCID+")";

    private static final String SQL_CREATE_DOC_CONTENT =
            "CREATE TABLE " + DaoMem.KEY_DOC + DaoMem.CONTENT + " (" +
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
    private static final String SQL_CREATE_DOC_CONTENT_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_DOC+DaoMem.CONTENT+"_index_1 ON " + DaoMem.KEY_DOC + DaoMem.CONTENT + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_DOC_CONTENT_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_DOC+DaoMem.CONTENT+"_index_2 ON " + DaoMem.KEY_DOC + DaoMem.CONTENT + " ("+BaseRec.KEY_DOC_CONTENTID+")";

    private static final String SQL_CREATE_DOC_CONTENT_MARK =
            "CREATE TABLE " + DaoMem.KEY_DOC + DaoMem.CONTENT_MARK +" (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    BaseRec.KEY_DOCID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENTID + " TEXT," +
                    BaseRec.KEY_DOC_CONTENT_MARKID + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNED_ASTYPE + " TEXT," +
                    BaseRec.KEY_POS_MARKSCANNEDREAL + " TEXT," +
                    BaseRec.KEY_POS_MARKBOX + " TEXT," +
                    BaseRec.KEY_POS_MARKSTATE + " TEXT)";
    private static final String SQL_CREATE_DOC_CONTENT_MARK_INDEX_1 =
            "CREATE INDEX "+DaoMem.KEY_DOC+DaoMem.CONTENT_MARK+"_index_1 ON " + DaoMem.KEY_DOC + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOCID+")";
    private static final String SQL_CREATE_DOC_CONTENT_MARK_INDEX_2 =
            "CREATE INDEX "+DaoMem.KEY_DOC+DaoMem.CONTENT_MARK+"_index_2 ON " + DaoMem.KEY_DOC + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENTID+")";
    private static final String SQL_CREATE_DOC_CONTENT_MARK_INDEX_3 =
            "CREATE INDEX "+DaoMem.KEY_DOC+DaoMem.CONTENT_MARK+"_index_3 ON " + DaoMem.KEY_DOC + DaoMem.CONTENT_MARK + " ("+BaseRec.KEY_DOC_CONTENT_MARKID+")";

    private static final String SQL_DELETE_DOC = "DROP TABLE IF EXISTS " + DaoMem.KEY_DOC;
    private static final String SQL_DELETE_DOC_CONTENT = "DROP TABLE IF EXISTS " + DaoMem.KEY_DOC+DaoMem.CONTENT;
    private static final String SQL_DELETE_DOC_CONTENT_MARK = "DROP TABLE IF EXISTS " + DaoMem.KEY_DOC+DaoMem.CONTENT_MARK;


    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        //
        // INV
        //
        db.execSQL(SQL_CREATE_INV);
        db.execSQL(SQL_CREATE_INV_INDEX);

        db.execSQL(SQL_CREATE_INV_CONTENT);
        db.execSQL(SQL_CREATE_INV_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_INV_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_INV_CONTENT_MARK);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_INV_CONTENT_MARK_INDEX_3);
        //
        // FINDMARK
        //
        db.execSQL(SQL_CREATE_FINDMARK);
        db.execSQL(SQL_CREATE_FINDMARK_INDEX);

        db.execSQL(SQL_CREATE_FINDMARK_CONTENT);
        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_MARK);
        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_FINDMARK_CONTENT_MARK_INDEX_3);
        //
        // CHECKMARK
        //
        db.execSQL(SQL_CREATE_CHECKMARK);
        db.execSQL(SQL_CREATE_CHECKMARK_INDEX);

        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT);
        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_MARK);
        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_CHECKMARK_CONTENT_MARK_INDEX_3);
        //
        // WRITEOFF
        //
        db.execSQL(SQL_CREATE_WRITEOFF);
        db.execSQL(SQL_CREATE_WRITEOFF_INDEX);

        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT);
        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_MARK);
        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_WRITEOFF_CONTENT_MARK_INDEX_3);
        //
        // DOC
        //
        db.execSQL(SQL_CREATE_DOC);
        db.execSQL(SQL_CREATE_DOC_INDEX);

        db.execSQL(SQL_CREATE_DOC_CONTENT);
        db.execSQL(SQL_CREATE_DOC_CONTENT_INDEX_1);
        db.execSQL(SQL_CREATE_DOC_CONTENT_INDEX_2);

        db.execSQL(SQL_CREATE_DOC_CONTENT_MARK);
        db.execSQL(SQL_CREATE_DOC_CONTENT_MARK_INDEX_1);
        db.execSQL(SQL_CREATE_DOC_CONTENT_MARK_INDEX_2);
        db.execSQL(SQL_CREATE_DOC_CONTENT_MARK_INDEX_3);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // INV
        db.execSQL(SQL_DELETE_INV_CONTENT_MARK);
        db.execSQL(SQL_DELETE_INV_CONTENT);
        db.execSQL(SQL_DELETE_INV);
        // FINDMARK
        db.execSQL(SQL_DELETE_FINDMARK_CONTENT_MARK);
        db.execSQL(SQL_DELETE_FINDMARK_CONTENT);
        db.execSQL(SQL_DELETE_FINDMARK);
        // CHECKMARK
        db.execSQL(SQL_DELETE_CHECKMARK_CONTENT_MARK);
        db.execSQL(SQL_DELETE_CHECKMARK_CONTENT);
        db.execSQL(SQL_DELETE_CHECKMARK);
        // WRITEOFF
        db.execSQL(SQL_DELETE_WRITEOFF_CONTENT_MARK);
        db.execSQL(SQL_DELETE_WRITEOFF_CONTENT);
        db.execSQL(SQL_DELETE_WRITEOFF);
        // DOC
        db.execSQL(SQL_DELETE_DOC_CONTENT_MARK);
        db.execSQL(SQL_DELETE_DOC_CONTENT);
        db.execSQL(SQL_DELETE_DOC);

        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}