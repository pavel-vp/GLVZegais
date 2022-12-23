package com.glvz.egais.dao;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.BaseColumns;
import android.support.v4.content.FileProvider;
import android.util.Log;
import com.glvz.egais.BuildConfig;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.daodb.AppDbHelper;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkContentIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkMark;
import com.glvz.egais.integration.model.doc.findmark.FindMarkContentIn;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.inv.InvContentIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.integration.sdcard.Integration;
import com.glvz.egais.integration.sdcard.IntegrationSDCard;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.wifi.SyncWiFiFtp;
import com.glvz.egais.model.*;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.model.checkmark.CheckMarkRecContent;
import com.glvz.egais.model.checkmark.CheckMarkRecContentMark;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.model.findmark.FindMarkRecContent;
import com.glvz.egais.model.income.*;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.model.inv.InvRecContentMark;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.model.photo.PhotoRec;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.model.writeoff.WriteoffRecContentMark;
import com.glvz.egais.service.CommandCall;
import com.glvz.egais.service.CommandFinishCallback;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;

import java.io.*;
import java.util.*;

public class DaoMem {


    public static final String KEY_LAST_DOCID = "last_docid";
    public static final String KEY_WRITEOFF = "writeoff";
    public static final String KEY_CHECKMARK = "checkmark";
    public static final String KEY_FINDMARK = "findmark";
    public static final String KEY_INV = "inv";
    public static final String CONTENT = "_content";
    public static final String CONTENT_MARK = "_content_mark";

    private static DaoMem daoMem = null;

    public static DaoMem getDaoMem() {
        if (daoMem == null) {
            daoMem = new DaoMem();
        }
        return daoMem;
    }

    private DaoMem() {
        sharedPreferences = MainApp.getContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);
        appDbHelper = new AppDbHelper(MainApp.getContext());
        initDictionary();
        //deviceId = Settings.Secure.getString(MainApp.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceId = Build.SERIAL;
    }

    Integration integrationFile;

    Dictionary dictionary;

    Document document;
    DocumentMove documentMove;

    SyncWiFiFtp syncWiFiFtp;

    SetupFtp setupFtp;
    List<ShopIn> listS;
    List<PostIn> listP;
    List<UserIn> listU;
    List<NomenIn> listN;
    List<AlcCodeIn> listA;
    List<MarkIn> listM;
    List<CommandIn> listCommands;
    List<IncomeIn> listIncomeIn;
    List<MoveIn> listMoveIn;
    List<CheckMarkIn> listCheckMarkIn;
    List<FindMarkIn> listFindMarkIn;
    List<InvIn> listInvIn;

    Map<String, IncomeRec> mapIncomeRec;
    Map<String, MoveRec> mapMoveRec;
    Map<String, WriteoffRec> mapWriteoffRec;
    Map<String, CheckMarkRec> mapCheckMarkRec;
    Map<String, FindMarkRec> mapFindMarkRec;
    Map<String, InvRec> mapInvRec;
    Map<String, PhotoRec> mapPhotoRec;

    SharedPreferences sharedPreferences;
    AppDbHelper appDbHelper;

    private String deviceId;

    private UserIn userIn;
    private String shopId;


    public void initDictionary() {
        File path = new File(Environment.getExternalStorageDirectory(), MainApp.getContext().getResources().getString(R.string.path_exchange));
        integrationFile = new IntegrationSDCard(path.getAbsolutePath());
        List<String> allRemainRecs = integrationFile.clearOldData(Integer.valueOf(MainApp.getContext().getResources().getString(R.string.num_days_old)));
        clearStoredDataNotInList(allRemainRecs);
        listU = integrationFile.loadUsers();
        listS = integrationFile.loadShops();
        listP = integrationFile.loadPosts();
        listN = integrationFile.loadNomen();
        listA = integrationFile.loadAlcCode();
        listCommands = integrationFile.loadCommands();
        String shopIdStored = sharedPreferences.getString(BaseRec.KEY_SHOPID, null);
        if (shopIdStored != null) {
            ShopIn shopInStored = findShopInById(shopIdStored);
            if (shopInStored != null) {
                setShopId(shopInStored.getId());
            }
        }
        dictionary = new DictionaryMem(listU, listS, listP, listN, listA);
        setupFtp = integrationFile.loadSetupFtp();
        syncWiFiFtp = new SyncWiFiFtp();
        syncWiFiFtp.init(MainApp.getContext(), path.getAbsolutePath(),
                setupFtp);
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(BaseRec.KEY_SHOPID, this.shopId);
        ed.apply();
    }

    private ShopIn findShopInById(String shopIdStored) {
        for (ShopIn shopIn : listS) {
            if (shopIn.getId().equals(shopIdStored)){
                return shopIn;
            }
        }
        return null;
    }

    public List<CommandIn> getListCommands(String parent) {
        List<CommandIn> result = new ArrayList<>();
        for (CommandIn commandIn : listCommands) {
            if (parent == null || "".equals(parent) || commandIn.getParentID().equals(parent)){
                result.add(commandIn);
            }
        }
        return result;
    }

    public CommandIn getCommandByID(String id) {
        for (CommandIn commandIn : listCommands) {
            if (commandIn.getId().equals(id)){
                return commandIn;
            }
        }
        return null;
    }

    public String getNewDocId() {
        String lastDocId = sharedPreferences.getString(KEY_LAST_DOCID, "0");
        String newDocId =  String.valueOf(Long.parseLong(lastDocId) + 1);
        storeLastDocId(newDocId);
        return newDocId;
    }

    private void storeLastDocId(String docId) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(KEY_LAST_DOCID, docId);
        ed.apply();
    }

    public void clearAllSharedPreferences() {
        // test by LAG
        MainApp.getContext().getSharedPreferences("settings", Activity.MODE_PRIVATE).edit().clear().apply();
    }

    public void initDocuments(boolean notify) {
        integrationFile.initDirectories(shopId);
        listIncomeIn = integrationFile.loadIncome(shopId);
        listMoveIn = integrationFile.loadMove(shopId);
        listCheckMarkIn = integrationFile.loadCheckMark(shopId);
        listFindMarkIn = integrationFile.loadFindMark(shopId);
        listInvIn = integrationFile.loadInv(shopId);

        listM = integrationFile.loadMark(shopId);
        document = new DocumentMem(listIncomeIn);
        documentMove = new DocumentMoveMem(listMoveIn);

        // Прочитать локальные данные
        mapIncomeRec = readIncomeRec();
        mapMoveRec = readMoveRec();
        mapCheckMarkRec = readCheckMarkRec();
        mapWriteoffRec = readWriteoffRec(shopId);
        mapPhotoRec = readPhotoRec(shopId);
        mapFindMarkRec = readFindMarkRec();
        mapInvRec = readInvRec();
        if (notify) {
            MessageUtils.showToastMessage("Данные загружены");
        }

    }

    private Map<String,MoveRec> readMoveRec() {
        Map<String, MoveRec> map = new HashMap<>();

        for (MoveIn moveIn : listMoveIn) {
            MoveRec moveRec = new MoveRec(moveIn.getDocId(), moveIn);
            readLocalData(moveRec);
            map.put(moveIn.getDocId(), moveRec);
        }

        return map;
    }

    private Map<String,CheckMarkRec> readCheckMarkRec() {
        Map<String, CheckMarkRec> map = new HashMap<>();

        for (CheckMarkIn checkMarkIn : listCheckMarkIn) {
            CheckMarkRec rec = new CheckMarkRec(checkMarkIn.getDocId(), checkMarkIn);
            readLocalDataCheckMark(rec);
            map.put(checkMarkIn.getDocId(), rec);
        }

        return map;
    }

    private Map<String,WriteoffRec> readWriteoffRec(String shopId) {
        Map<String, WriteoffRec> map = new HashMap<>();

        Set<String> docIds = sharedPreferences.getStringSet(KEY_WRITEOFF+"_"+shopId, Collections.<String>emptySet());

        for (String docId : docIds ) {
            WriteoffRec writeoffRec = new WriteoffRec(docId);
            readLocalDataWriteoff(writeoffRec);
            map.put(writeoffRec.getDocId(), writeoffRec);
        }

        return map;
    }

    private Map<String,PhotoRec> readPhotoRec(String shopId) {
        Map<String, PhotoRec> map = new HashMap<>();

        for (File file : integrationFile.loadPhotoFiles(shopId) ) {
            byte[] data = new byte[0];
            byte[] dataMini = null;
            try {

                //data = readBytes(file);
                String fileName = file.getName();
                // parse fileName IMG-2020-01-01 20:00:00.jpeg
                String date = fileName.substring(4,23);
                File fileMini = new File(file.getParent()+"/IMG-MINI-"+date+".jpeg");
                dataMini = readBytes(fileMini);

                PhotoRec photoRec = new PhotoRec(shopId, shopId, data, dataMini, date);
                map.put(photoRec.getDocId(), photoRec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return map;
    }

    public byte[] readBytes(File file) throws IOException {
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }

    private Map<String,FindMarkRec> readFindMarkRec() {
        Map<String, FindMarkRec> map = new HashMap<>();

        for (FindMarkIn findMarkIn : listFindMarkIn) {
            FindMarkRec findMarkRec = new FindMarkRec(findMarkIn.getDocId(), findMarkIn);
            readLocalDataFindMark(findMarkRec);
            map.put(findMarkRec.getDocId(), findMarkRec);
        }

        return map;
    }

    private Map<String,IncomeRec> readIncomeRec() {
        Map<String, IncomeRec> map = new HashMap<>();

        for (IncomeIn incomeIn : listIncomeIn) {
            IncomeRec incomeRec = new IncomeRec(incomeIn.getWbRegId(), incomeIn);
            readLocalData(incomeRec);
            map.put(incomeIn.getWbRegId(), incomeRec);
        }

        return map;
    }
    private Map<String,InvRec> readInvRec() {
        Map<String, InvRec> map = new HashMap<>();

        for (InvIn invIn : listInvIn) {
            InvRec invRec = new InvRec(invIn.getDocId(), invIn);
            readLocalDataInv(invRec);
            map.put(invIn.getDocId(), invRec);
        }

        return map;
    }

    private void readLocalData(BaseRec baseRec) {
        baseRec.setCntDone(sharedPreferences.getInt(BaseRec.KEY_CNTDONE + "_" + baseRec.getDocId() + "_", 0));
        baseRec.setStatus(BaseRecStatus.valueOf(sharedPreferences.getString(BaseRec.KEY_STATUS + "_" + baseRec.getDocId() + "_", BaseRecStatus.NEW.toString())));
        baseRec.setExported(sharedPreferences.getBoolean(BaseRec.KEY_EXPORTED + "_" + baseRec.getDocId() + "_", false));
        // пройтись по строкам и прочитать доп.данные
        baseRec.getRecContentList().clear();
        for (DocContentIn docContentIn : baseRec.getDocContentInList()) {
            BaseRecContent recContent = baseRec.buildRecContent(docContentIn);
            // прочитать данные по строке локальные
            recContent.setId1c(sharedPreferences.getString(BaseRec.KEY_POS_ID1C + "_" + baseRec.getDocId() + "_" + recContent.getPosition(), null));
            String barcode = sharedPreferences.getString(BaseRec.KEY_POS_BARCODE + "_" + baseRec.getDocId() + "_" + recContent.getPosition(), null);
            recContent.setNomenIn(dictionary.findNomenById(recContent.getId1c()), barcode);
            recContent.setStatus(BaseRecContentStatus.valueOf(
                    sharedPreferences.getString(BaseRec.KEY_POS_STATUS + "_" + baseRec.getDocId() + "_" + recContent.getPosition(), BaseRecContentStatus.NOT_ENTERED.toString())));
            float qty = sharedPreferences.getFloat(BaseRec.KEY_POS_QTYACCEPTED + "_" + baseRec.getDocId() + "_" + recContent.getPosition(), 0);
            if (qty != 0) {
                recContent.setQtyAccepted(Double.valueOf(qty));
            }
            int cnt = sharedPreferences.getInt(BaseRec.KEY_POS_MARKSCANNED_CNT + "_" + baseRec.getDocId() + "_" + recContent.getPosition(), 0);
            if (cnt > 0) {
                for (int i = 1; i <= cnt; i++) {
                    String mark = sharedPreferences.getString(BaseRec.KEY_POS_MARKSCANNED + "_" + baseRec.getDocId() + "_" + recContent.getPosition() + "_" + i, null);
                    int typeAs = sharedPreferences.getInt(BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_" + baseRec.getDocId() + "_" + recContent.getPosition() + "_" + i, 0);
                    String realMark = sharedPreferences.getString(BaseRec.KEY_POS_MARKSCANNEDREAL + "_" + baseRec.getDocId() + "_" + recContent.getPosition() + "_" + i, null);
                    recContent.getBaseRecContentMarkList().add(new BaseRecContentMark(mark, typeAs, realMark));
                }
            }
            baseRec.getRecContentList().add(recContent);
        }
    }

    private void readLocalDataWriteoff(WriteoffRec writeoffRec) {
        writeoffRec.setDocNum(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DOCNUM + "_" + writeoffRec.getDocId() + "_", ""));
        writeoffRec.setDateStr(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DATE + "_" + writeoffRec.getDocId() + "_", ""));
        writeoffRec.setTypeDoc(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_TYPEDOC + "_" + writeoffRec.getDocId() + "_", ""));
        writeoffRec.setSkladId(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADID + "_" + writeoffRec.getDocId() + "_", ""));
        writeoffRec.setSkladName(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADNAME + "_" + writeoffRec.getDocId() + "_", ""));
        writeoffRec.setComment(sharedPreferences.getString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_COMMENT + "_" + writeoffRec.getDocId() + "_", ""));
        int contentSize = sharedPreferences.getInt(KEY_WRITEOFF + "_" + WriteoffRec.KEY_CONTENT_SIZE + "_" + writeoffRec.getDocId() + "_", 0);
        for (int i = 1; i<= contentSize; i++) {
            WriteoffRecContent recContent = readLocalDataWriteoffContent(writeoffRec, i);
            writeoffRec.getRecContentList().add(recContent);
        }
    }

    private WriteoffRecContent readLocalDataWriteoffContent(WriteoffRec writeoffRec, int position) {

        WriteoffRecContent recContent = new WriteoffRecContent(String.valueOf(position), null);

        String id1c = sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_ID1C+"_"+writeoffRec.getDocId()+"_"+position, "");
        recContent.setId1c(id1c);
        String barcode = sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_BARCODE+"_"+writeoffRec.getDocId()+"_"+position, "");
        recContent.setNomenIn(findNomenInByNomenId(id1c), barcode);

        String posStatus = sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_STATUS+"_"+writeoffRec.getDocId()+"_"+position, BaseRecContentStatus.IN_PROGRESS.toString());
        recContent.setStatus(BaseRecContentStatus.valueOf(posStatus));

        float qtyAccepted = sharedPreferences.getFloat(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_QTYACCEPTED+"_"+writeoffRec.getDocId()+"_"+position, 0);
        recContent.setQtyAccepted(Double.valueOf(qtyAccepted));

        int markScannedSize = sharedPreferences.getInt(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+writeoffRec.getDocId()+"_"+position, 0);
        for (int idx = 1; idx <= markScannedSize; idx++) {
            WriteoffRecContentMark writeoffRecContentMark = new WriteoffRecContentMark(
                sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED + "_"+writeoffRec.getDocId()+"_"+position+"_"+idx, ""),
                sharedPreferences.getInt(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_"+writeoffRec.getDocId()+"_"+position+"_"+idx, 0),
                sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNEDREAL + "_"+writeoffRec.getDocId()+"_"+position+"_"+idx, ""),
                sharedPreferences.getString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKBOX + "_"+writeoffRec.getDocId()+"_"+position+"_"+idx, "")
            );
            recContent.getBaseRecContentMarkList().add(writeoffRecContentMark);
        }
        return recContent;
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
                DaoMem.KEY_INV+CONTENT,   // The table to query
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
                DaoMem.KEY_INV+CONTENT_MARK,   // The table to query
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

    private void readLocalDataInv(InvRec invRec) {
        Log.v("DaoMem", "readLocalDataInv start");
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

        Log.v("DaoMem", "readLocalDataInv end contentSize="+invRec.getRecContentList().size());
    }

    private void readLocalDataInvContentAndMerge(InvRec invRec) {
        SQLiteDatabase db = appDbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DaoMem.KEY_INV+CONTENT,   // The table to query
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
            recContent.setNomenIn(findNomenInByNomenId(id1c), barcode);
            recContent.setStatus(BaseRecContentStatus.valueOf(posStatus));
            recContent.setQtyAccepted((double) qtyAccepted);
            recContent.setManualMrc(manualMrc);

            Cursor cursorMark = db.query(
                    DaoMem.KEY_INV+CONTENT_MARK,   // The table to query
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

    private void readLocalDataCheckMark(CheckMarkRec rec) {
        rec.setExported(sharedPreferences.getBoolean(KEY_CHECKMARK + "_" + BaseRec.KEY_EXPORTED + "_" + rec.getDocId() + "_", false));
        rec.setStatus(BaseRecStatus.valueOf(sharedPreferences.getString(KEY_CHECKMARK + "_" + BaseRec.KEY_STATUS + "_" + rec.getDocId() + "_", BaseRecStatus.NEW.toString())));
        // пройтись по строкам и прочитать доп.данные
        rec.getRecContentList().clear();
        for (DocContentIn docContentIn : rec.getDocContentInList()) {
            CheckMarkRecContent recContent = (CheckMarkRecContent) rec.buildRecContent(docContentIn);
            String nomenId = ((CheckMarkContentIn)docContentIn).getNomenId();
            recContent.setNomenIn(findNomenInByNomenId(nomenId), null);

            // прочитать данные по строке локальные
            float qty = sharedPreferences.getFloat(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED + "_" + rec.getDocId() + "_" + recContent.getPosition(), 0);
            if (qty != 0) {
                recContent.setQtyAccepted(Double.valueOf(qty));
            }
            float qtyNew = sharedPreferences.getFloat(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED_NEW + "_" + rec.getDocId() + "_" + recContent.getPosition(), 0);
            if (qtyNew != 0) {
                recContent.setQtyAcceptedNew(Double.valueOf(qtyNew));
            }
            int cnt = sharedPreferences.getInt(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_" + rec.getDocId() + "_" + recContent.getPosition(), 0);
            if (cnt > 0) {
                for (int i = 1; i <= cnt; i++) {
                    String mark = sharedPreferences.getString(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSCANNED + "_" + rec.getDocId() + "_" + recContent.getPosition() + "_" + i, null);
                    int state = sharedPreferences.getInt(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSTATE + "_" + rec.getDocId() + "_" + recContent.getPosition() + "_" + i, 0);
                    recContent.getBaseRecContentMarkList().add(new CheckMarkRecContentMark(mark, BaseRecContentMark.MARK_SCANNED_AS_MARK, mark, state));
                }
            }
            rec.getRecContentList().add(recContent);
        }

    }

    private void readLocalDataFindMark(FindMarkRec rec) {
        try {
            rec.setCntDone(sharedPreferences.getInt(KEY_FINDMARK + "_" + BaseRec.KEY_CNTDONE + "_" + rec.getDocId() + "_", 0));
        } catch (Exception e) {}
        rec.setStatus(BaseRecStatus.valueOf(sharedPreferences.getString(KEY_FINDMARK + "_" + BaseRec.KEY_STATUS + "_" + rec.getDocId() + "_", BaseRecStatus.NEW.toString())));
        // пройтись по строкам и прочитать доп.данные
        rec.getRecContentList().clear();
        for (DocContentIn docContentIn : rec.getDocContentInList()) {
            FindMarkRecContent recContent = (FindMarkRecContent) rec.buildRecContent(docContentIn);
            String nomenId = ((FindMarkContentIn)docContentIn).getNomenId();
            recContent.setNomenIn(findNomenInByNomenId(nomenId), null);

            // прочитать данные по строке локальные
            float qty = sharedPreferences.getFloat(KEY_FINDMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED + "_" + rec.getDocId() + "_" + recContent.getPosition(), 0);
            if (qty != 0) {
                recContent.setQtyAccepted(Double.valueOf(qty));
            }
            int cnt = sharedPreferences.getInt(KEY_FINDMARK + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_" + rec.getDocId() + "_" + recContent.getPosition(), 0);
            if (cnt > 0) {
                for (int i = 1; i <= cnt; i++) {
                    String mark = sharedPreferences.getString(KEY_FINDMARK + "_" + BaseRec.KEY_POS_MARKSCANNED + "_" + rec.getDocId() + "_" + recContent.getPosition() + "_" + i, null);
                    recContent.getBaseRecContentMarkList().add(new BaseRecContentMark(mark, BaseRecContentMark.MARK_SCANNED_AS_MARK, mark));
                }
            }
            rec.getRecContentList().add(recContent);
        }
    }

    private void clearStoredDataNotInList(List<String> docIdList) {
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        for (String docId : docIdList) {
            Iterator<? extends Map.Entry<String, ?>> iter = allPrefs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String,?> entry  = iter.next();
                if (entry.getKey().contains("_" + docId + "_")) {
                    // Удалить
                    iter.remove();
                }
            }
        }

        // Оставшиеся удаляем
        SharedPreferences.Editor ed = sharedPreferences.edit();
        for (Map.Entry<String,?> entry : allPrefs.entrySet()) {
            // НЕ связанные с документами
            if (!entry.getKey().equals(BaseRec.KEY_SHOPID) &&
                    !entry.getKey().equals(KEY_LAST_DOCID) &&
                    !entry.getKey().startsWith(KEY_WRITEOFF) /*&&
                    !entry.getKey().startsWith(KEY_INV) &&
                    !entry.getKey().startsWith(KEY_CHECKMARK) &&
                    !entry.getKey().startsWith(KEY_FINDMARK)*/) {
                ed.putString(entry.getKey(), null);
            }
        }
        ed.apply();

    }

    public void writeLocalDataBaseRec(BaseRec baseRec) {
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
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(BaseRec.KEY_EXPORTED+"_"+baseRec.getDocId()+"_", baseRec.isExported());
        ed.putInt(BaseRec.KEY_CNTDONE+"_"+baseRec.getDocId()+"_", baseRec.getCntDone());
        ed.putString(BaseRec.KEY_STATUS+"_"+baseRec.getDocId()+"_", baseRec.getStatus().toString());
        ed.apply();
        // записать данные по строкам
        for (BaseRecContent recContent : baseRec.getRecContentList()) {
            writeLocalDataBaseRecContent(baseRec.getDocId(), recContent);
        }
    }

    private void writeLocalDataBaseRecContent(String docId, BaseRecContent recContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(BaseRec.KEY_POS_ID1C+"_"+docId+"_"+recContent.getPosition(), recContent.getId1c());
        ed.putString(BaseRec.KEY_POS_BARCODE+"_"+docId+"_"+recContent.getPosition(), recContent.getBarcode());
        ed.putString(BaseRec.KEY_POS_STATUS+"_"+docId+"_"+recContent.getPosition(), recContent.getStatus().toString());
        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(BaseRec.KEY_POS_QTYACCEPTED+"_"+docId+"_"+recContent.getPosition(), qty);
        ed.putInt(BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+docId+"_"+recContent.getPosition(), recContent.getBaseRecContentMarkList().size());
        int idx = 1;
        for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
            ed.putString(BaseRec.KEY_POS_MARKSCANNED + "_"+docId+"_"+recContent.getPosition()+"_"+idx, baseRecContentMark.getMarkScanned());
            ed.putInt(BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_"+docId+"_"+recContent.getPosition()+"_"+idx, baseRecContentMark.getMarkScannedAsType());
            ed.putString(BaseRec.KEY_POS_MARKSCANNEDREAL + "_"+docId+"_"+recContent.getPosition()+"_"+idx, baseRecContentMark.getMarkScannedReal());
            idx++;
        }
        ed.apply();
    }

    public void writeLocalDataRec_ClearAllMarks(BaseRec baseRec) {
        for (BaseRecContent recContent : baseRec.getRecContentList()) {
            writeLocalDataRecContent_ClearAllMarks(baseRec.getDocId(), recContent);
        }
    }

    public void writeLocalDataRecContent_ClearAllMarks(String docId, BaseRecContent recContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        int idx = 1;
        for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
            ed.remove(BaseRec.KEY_POS_MARKSCANNED + "_" + docId + "_" + recContent.getPosition() + "_" + idx);
            ed.remove(BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_" + docId + "_" + recContent.getPosition() + "_" + idx);
            ed.remove(BaseRec.KEY_POS_MARKSCANNEDREAL + "_" + docId + "_" + recContent.getPosition() + "_" + idx);
            ed.remove(BaseRec.KEY_POS_MARKBOX + "_" + docId + "_" + recContent.getPosition() + "_" + idx);
            idx++;
        }
        ed.apply();
    }

    private void writeLocalDataWriteoffRec(WriteoffRec writeoffRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DOCNUM + "_" + writeoffRec.getDocId() + "_", writeoffRec.getDocNum());
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DATE + "_" + writeoffRec.getDocId() + "_", writeoffRec.getDateStr());
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_TYPEDOC + "_" + writeoffRec.getDocId() + "_", writeoffRec.getTypeDoc());
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADID + "_" + writeoffRec.getDocId() + "_", writeoffRec.getSkladId());
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADNAME + "_" + writeoffRec.getDocId() + "_", writeoffRec.getSkladName());
        ed.putString(KEY_WRITEOFF + "_" + WriteoffRec.KEY_COMMENT + "_" + writeoffRec.getDocId() + "_", writeoffRec.getComment());
        ed.putInt(KEY_WRITEOFF + "_" + WriteoffRec.KEY_CONTENT_SIZE + "_" + writeoffRec.getDocId() + "_", writeoffRec.getRecContentList().size());
        ed.apply();
        // записать данные по строкам
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            writeLocalDataWriteoffRecContent(writeoffRec.getDocId(), recContent);
        }
    }

    private void writeLocalDataWriteoffRecContent(String docId, WriteoffRecContent recContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_ID1C+"_"+docId+"_"+recContent.getPosition(), recContent.getId1c());
        ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_BARCODE+"_"+docId+"_"+recContent.getPosition(), recContent.getBarcode());
        ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_STATUS+"_"+docId+"_"+recContent.getPosition(), recContent.getStatus().toString());
        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_QTYACCEPTED+"_"+docId+"_"+recContent.getPosition(), qty);
        ed.putInt(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+docId+"_"+recContent.getPosition(), recContent.getBaseRecContentMarkList().size());
        int idx = 1;
        for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
            WriteoffRecContentMark writeoffRecContentMark = (WriteoffRecContentMark) baseRecContentMark;
            ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED + "_"+docId+"_"+recContent.getPosition()+"_"+idx, writeoffRecContentMark.getMarkScanned());
            ed.putInt(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_"+docId+"_"+recContent.getPosition()+"_"+idx, writeoffRecContentMark.getMarkScannedAsType());
            ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNEDREAL + "_"+docId+"_"+recContent.getPosition()+"_"+idx, writeoffRecContentMark.getMarkScannedReal());
            ed.putString(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKBOX + "_"+docId+"_"+recContent.getPosition()+"_"+idx, writeoffRecContentMark.getBox());
            idx++;
        }
        ed.apply();
    }

    public void writeLocalDataWriteoffRec_Clear(WriteoffRec writeoffRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DOCNUM + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_DATE + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_TYPEDOC + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADID + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_SKLADNAME + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_COMMENT + "_" + writeoffRec.getDocId() + "_");
        ed.remove(KEY_WRITEOFF + "_" + WriteoffRec.KEY_CONTENT_SIZE + "_" + writeoffRec.getDocId() + "_");
        ed.apply();
        // записать данные по строкам
        for (WriteoffRecContent recContent : writeoffRec.getWriteoffRecContentList()) {
            writeLocalDataWriteoffRecContent_Clear(writeoffRec.getDocId(), recContent);
        }
    }

    private void writeLocalDataWriteoffRecContent_Clear(String docId, WriteoffRecContent recContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_ID1C+"_"+docId+"_"+recContent.getPosition());
        ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_BARCODE+"_"+docId+"_"+recContent.getPosition());
        ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_STATUS+"_"+docId+"_"+recContent.getPosition());
        ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_QTYACCEPTED+"_"+docId+"_"+recContent.getPosition());
        ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+docId+"_"+recContent.getPosition());
        int idx = 1;
        for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
            ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED + "_"+docId+"_"+recContent.getPosition()+"_"+idx);
            ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNED_ASTYPE + "_"+docId+"_"+recContent.getPosition()+"_"+idx);
            ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKSCANNEDREAL + "_"+docId+"_"+recContent.getPosition()+"_"+idx);
            ed.remove(KEY_WRITEOFF + "_" + BaseRec.KEY_POS_MARKBOX + "_"+docId+"_"+recContent.getPosition()+"_"+idx);
            idx++;
        }
        ed.apply();
    }

    public void writeLocalDataCheckMarkRec(CheckMarkRec checkMarkRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(KEY_CHECKMARK + "_" + BaseRec.KEY_EXPORTED+"_"+checkMarkRec.getDocId()+"_", checkMarkRec.isExported());
        ed.putString(KEY_CHECKMARK + "_" + BaseRec.KEY_STATUS+"_"+checkMarkRec.getDocId()+"_", checkMarkRec.getStatus().toString());
        ed.apply();
        // записать данные по строкам
        for (CheckMarkRecContent recContent : checkMarkRec.getCheckMarkRecContentList()) {
            writeLocalDataCheckMarkRecContent(checkMarkRec.getDocId(), recContent);
        }
    }

    private void writeLocalDataCheckMarkRecContent(String docId, CheckMarkRecContent recContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED+"_"+docId+"_"+recContent.getPosition(), qty);
        float qtyNew = 0;
        if (recContent.getQtyAcceptedNew() != null) {
            qtyNew = recContent.getQtyAcceptedNew().floatValue();
        }
        ed.putFloat(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED_NEW+"_"+docId+"_"+recContent.getPosition(), qtyNew);
        ed.putInt(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+docId+"_"+recContent.getPosition(), recContent.getBaseRecContentMarkList().size());
        int idx = 1;
        for (CheckMarkRecContentMark contentMark : recContent.getCheckMarkRecContentMarkList()) {
            ed.putString(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSCANNED + "_"+docId+"_"+recContent.getPosition()+"_"+idx, contentMark.getMarkScanned());
            ed.putInt(KEY_CHECKMARK + "_" + BaseRec.KEY_POS_MARKSTATE + "_"+docId+"_"+recContent.getPosition()+"_"+idx, contentMark.getState());
            idx++;
        }
        ed.apply();
    }

    public void writeLocalDataFindMarkRec(FindMarkRec findMarkRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        int cntDone = 0;
        for (FindMarkRecContent recContent : findMarkRec.getFindMarkRecContentList()) {
            if (recContent.getBaseRecContentMarkList().size() >= recContent.getContentIn().getQty()) {
                cntDone++;
            }
        }
        findMarkRec.setCntDone(cntDone);
        ed.putInt(KEY_FINDMARK + "_" + BaseRec.KEY_CNTDONE+"_"+findMarkRec.getDocId()+"_", findMarkRec.getCntDone());
        ed.putString(KEY_FINDMARK + "_" + BaseRec.KEY_STATUS+"_"+findMarkRec.getDocId()+"_", findMarkRec.getStatus().toString());
        // записать данные по строкам
        for (FindMarkRecContent recContent : findMarkRec.getFindMarkRecContentList()) {
            writeLocalDataFindMarkRecContent(ed, findMarkRec.getDocId(), recContent);
        }
        ed.apply();
    }

    private void writeLocalDataFindMarkRecContent(SharedPreferences.Editor ed, String docId, FindMarkRecContent recContent) {
        float qty = 0;
        if (recContent.getQtyAccepted() != null) {
            qty = recContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(KEY_FINDMARK + "_" + BaseRec.KEY_POS_QTYACCEPTED+"_"+docId+"_"+recContent.getPosition(), qty);
        ed.putInt(KEY_FINDMARK + "_" + BaseRec.KEY_POS_MARKSCANNED_CNT + "_"+docId+"_"+recContent.getPosition(), recContent.getBaseRecContentMarkList().size());
        int idx = 1;
        for (BaseRecContentMark contentMark : recContent.getBaseRecContentMarkList()) {
            ed.putString(KEY_FINDMARK + "_" + BaseRec.KEY_POS_MARKSCANNED + "_"+docId+"_"+recContent.getPosition()+"_"+idx, contentMark.getMarkScanned());
            idx++;
        }
    }

    public void writeLocalDataInvRec(InvRec invRec) {
        Log.v("DaoMem", "writeLocalDataInvRec start");
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(KEY_INV+CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        int deletedRowsContent = db.delete(KEY_INV+CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        int deletedRow = db.delete(KEY_INV, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });

        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_EXPORTED, invRec.isExported() ? "true" : "false");
        values.put(BaseRec.KEY_STATUS, invRec.getStatus().toString());
        values.put(BaseRec.KEY_DOCID, invRec.getDocId());
        long newDbDocId = db.insert(KEY_INV, null, values);

        // записать данные по строкам
        int position = 1;
        for (InvRecContent recContent : invRec.getInvRecContentList()) {
            writeLocalDataInvRecContent(invRec.getDocId(), recContent, position);
            position++;
        }
        Log.v("DaoMem", "writeLocalDataInvRec end");
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
            db.insert(KEY_INV, null, values);
        } else {
            db.update(KEY_INV, values, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        }
        Log.v("DaoMem", "saveDbInvRec end");
    }

    private void saveDbInvRecWithContentDeletion(InvRec invRec) {
        saveDbInvRec(invRec);
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        int deletedRowsMark = db.delete(KEY_INV+CONTENT_MARK, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
        int deletedRowsContent = db.delete(KEY_INV+CONTENT, BaseRec.KEY_DOCID + " = ?", new String[] { invRec.getDocId() });
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
            db.insert(KEY_INV+CONTENT, null, values);
        } else {
            db.update(KEY_INV+CONTENT, values, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[] { contentId });
        }
        if (recContent.getBaseRecContentMarkList().isEmpty()) {
            int deletedRowsMark = db.delete(KEY_INV + CONTENT_MARK, BaseRec.KEY_DOC_CONTENTID + " = ?", new String[]{contentId});
        } else {
            List<Map<String, Object>> dbInvRecContentMarks = readDbInvRecContentMarks(contentId);

            int idx = 1;
            for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
                if (dbInvRecContentMarks == null || idx > dbInvRecContentMarks.size()) {
                    InvRecContentMark invRecContentMark = (InvRecContentMark) baseRecContentMark;
                    ContentValues valuesMark = new ContentValues();
                    valuesMark.put(BaseRec.KEY_DOCID, invRec.getDocId());
                    valuesMark.put(BaseRec.KEY_DOC_CONTENTID, contentId);
                    valuesMark.put(BaseRec.KEY_DOC_CONTENT_MARKID, contentId + "_" + idx);
                    valuesMark.put(BaseRec.KEY_POS_MARKSCANNED, invRecContentMark.getMarkScanned());
                    valuesMark.put(BaseRec.KEY_POS_MARKSCANNED_ASTYPE, invRecContentMark.getMarkScannedAsType());
                    valuesMark.put(BaseRec.KEY_POS_MARKSCANNEDREAL, invRecContentMark.getMarkScannedReal());
                    valuesMark.put(BaseRec.KEY_POS_MARKBOX, invRecContentMark.getBox());
                    long newContentMarkRowId = db.insert(KEY_INV + CONTENT_MARK, null, valuesMark);
                }
                idx++;
            }
        }

        Log.v("DaoMem", "saveDbInvRecContent end");
    }

    private void writeLocalDataInvRecContent(String docId, InvRecContent recContent, int position) {
        SQLiteDatabase db = appDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BaseRec.KEY_DOCID, docId);
        values.put(BaseRec.KEY_DOC_CONTENTID, docId+"_"+position);
        values.put(BaseRec.KEY_POS_ID1C, recContent.getId1c());
        values.put(BaseRec.KEY_POS_BARCODE, recContent.getBarcode());
        values.put(BaseRec.KEY_POS_STATUS, recContent.getStatus().toString());
        values.put(BaseRec.KEY_POS_POSITION, recContent.getPosition().toString());

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
        long newContentRowId = db.insert(KEY_INV+CONTENT, null, values);
        int idx = 1;
        for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
            InvRecContentMark invRecContentMark = (InvRecContentMark)baseRecContentMark;
            ContentValues valuesMark = new ContentValues();
            valuesMark.put(BaseRec.KEY_DOCID, docId);
            valuesMark.put(BaseRec.KEY_DOC_CONTENTID, docId+"_"+position);
            valuesMark.put(BaseRec.KEY_DOC_CONTENT_MARKID, docId+"_"+position+"_"+idx);
            valuesMark.put(BaseRec.KEY_POS_MARKSCANNED, invRecContentMark.getMarkScanned());
            valuesMark.put(BaseRec.KEY_POS_MARKSCANNED_ASTYPE, invRecContentMark.getMarkScannedAsType());
            valuesMark.put(BaseRec.KEY_POS_MARKSCANNEDREAL, invRecContentMark.getMarkScannedReal());
            valuesMark.put(BaseRec.KEY_POS_MARKBOX, invRecContentMark.getBox());
            long newContentMarkRowId = db.insert(KEY_INV+CONTENT_MARK, null, valuesMark);
            idx++;
        }
    }


    Comparator docRecDateComparator = new Comparator<BaseRec>() {
        @Override
        public int compare(BaseRec lhs, BaseRec rhs) {


            Date d1 = lhs.getDate();
            Date d2 = rhs.getDate();

            if (d1 == null || d2 == null || d1.before(d2)) return 1;
            if (d1.after(d2)) return -1;
            if (lhs.getAgentName() != null && rhs.getAgentName() != null) {
                int res = lhs.getAgentName().compareTo(rhs.getAgentName());
                if (res != 0) return res;
            }

            return lhs.getDocNum().compareTo(rhs.getDocNum());
        }
    };

    public Collection<IncomeRec> getIncomeRecListOrdered() {

        List<IncomeRec> list = new ArrayList<>();
        list.addAll(mapIncomeRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public Collection<WriteoffRec> getWriteoffRecListOrdered() {
        List<WriteoffRec> list = new ArrayList<>();
        list.addAll(mapWriteoffRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public Collection<CheckMarkRec> getCheckMarkRecListOrdered() {
        List<CheckMarkRec> list = new ArrayList<>();
        list.addAll(mapCheckMarkRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public Collection<FindMarkRec> getFindMarkRecListOrdered() {
        List<FindMarkRec> list = new ArrayList<>();
        list.addAll(mapFindMarkRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }



    public Map<String, IncomeRec> getMapIncomeRec() {
        return mapIncomeRec;
    }

    public Map<String, MoveRec> getMapMoveRec() {
        return mapMoveRec;
    }

    public Map<String, WriteoffRec> getMapWriteoffRec() {
        return mapWriteoffRec;
    }

    public Map<String, CheckMarkRec> getMapCheckMarkRec() {
        return mapCheckMarkRec;
    }

    public Collection<IncomeRecContent> getIncomeRecContentList(String wbRegId) {
        return mapIncomeRec.get(wbRegId).getIncomeRecContentList();
    }

    public Collection<MoveRecContent> getMoveRecContentList(String docId) {
        return mapMoveRec.get(docId).getMoveRecContentList();
    }

    public Collection<WriteoffRecContent> getWriteoffRecContentList(String docId) {
        return mapWriteoffRec.get(docId).getWriteoffRecContentList();
    }

    public Collection<CheckMarkRecContent> getCheckMarkRecContentList(String docId) {
        return mapCheckMarkRec.get(docId).getCheckMarkRecContentList();
    }

    public Collection<FindMarkRecContent> getFindMarkRecContentList(String docId) {
        return mapFindMarkRec.get(docId).getFindMarkRecContentList();
    }

    public Collection<InvRecContent> getInvRecContentList(String docId, final int filterType, final int sortType) {
        List<InvRecContent> result = new ArrayList<InvRecContent>();

        Comparator<InvRecContent> comparator = new Comparator<InvRecContent>() {
            @Override
            public int compare(InvRecContent o1, InvRecContent o2) {
                switch (sortType) {
                    case InvRecContent.INV_SORT_TYPE_POSITION:
                        Integer p1 = new Integer(o1.getPosition());
                        Integer p2 = new Integer(o2.getPosition());
                        return p1.compareTo(p2);
                    case InvRecContent.INV_SORT_TYPE_NAME:
                        int n = 0;
                        if (o1.getNomenIn() != null && o1.getNomenIn().getName() != null &&
                                o2.getNomenIn() != null && o2.getNomenIn().getName() != null) {
                            n = o1.getNomenIn().getName().compareTo(o2.getNomenIn().getName());
                        } else {
                            if (o1.getNomenIn() != null && o1.getNomenIn().getName() != null) return 1;
                            if (o2.getNomenIn() != null && o2.getNomenIn().getName() != null) return -1;
                        }
                        if (n == 0) {
                            if (o1.getManualMrc() != null && o2.getManualMrc() != null) {
                                return o1.getManualMrc().compareTo(o2.getManualMrc());
                            }
                        }
                        return n;
                    case InvRecContent.INV_SORT_TYPE_DIFF:
                        Double d1 = new Double(o1.getDiff());
                        Double d2 = new Double(o2.getDiff());
                        return d1.compareTo(d2);
                }
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) return false;
                return super.equals(obj);
            }
        };

        for (InvRecContent irc : mapInvRec.get(docId).getInvRecContentList()) {
            switch (filterType) {
                case InvRecContent.INV_FILTER_TYPE_STATUS:
                    if (irc.getStatus() != BaseRecContentStatus.NOT_ENTERED) {
                        continue;
                    }
                    break;
                case InvRecContent.INV_FILTER_TYPE_DIFF:
                    if (irc.getDiff() == 0) {
                        continue;
                    }
                    break;
            }
            result.add(irc);
        }

        Collections.sort(result, comparator);

        return result;
    }

    public BaseRecContent getRecContentByPosition(BaseRec rec, String position) {
        for (BaseRecContent recContent : rec.getRecContentList()) {
            if (recContent.getPosition().equals(position)) {
                return recContent;
            }
        }
        return null; // FIXME: обработку ексепшнов и сообщений пользователю
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public WriteoffRec addNewWriteoffRec(String shopId, String shopInName, String type) {
        WriteoffRec newRec = new WriteoffRec(shopId, shopInName, type);
        mapWriteoffRec.put(newRec.getDocId(),newRec);
        writeLocalWriteoffRec(newRec);
        return newRec;
    }

    public void writeLocalWriteoffRec(WriteoffRec newRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        Set<String> docIds = new HashSet<>();
        for (WriteoffRec writeoffRec : mapWriteoffRec.values()) {
            docIds.add(writeoffRec.getDocId());
        }
        ed.putStringSet(KEY_WRITEOFF+"_"+shopId, docIds);
        ed.apply();
        writeLocalDataWriteoffRec(newRec);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Map<String, FindMarkRec> getMapFindMarkRec() {
        return mapFindMarkRec;
    }

    public Map<String, InvRec> getMapInvRec() {
        return mapInvRec;
    }

    public void deleteData(WriteoffRec writeoffRec) {
        // удалять документ из списка и его out-файл (если есть).
        mapWriteoffRec.remove(writeoffRec.getDocId());
        SharedPreferences.Editor ed = sharedPreferences.edit();
        Set<String> docIds = new HashSet<>();
        for (WriteoffRec rec : mapWriteoffRec.values()) {
            docIds.add(rec.getDocId());
        }
        ed.putStringSet(KEY_WRITEOFF+"_"+shopId, docIds);
        ed.apply();
        rejectData(writeoffRec);
        writeLocalDataWriteoffRec(writeoffRec);
        // Удалить сам файл
        integrationFile.deleteFileRec(writeoffRec, shopId);
    }

    public PhotoRec addPhotoRec(String shopId, String shopInName, byte[] byteArray, byte[] byteArrayMini) {
        PhotoRec newRec = new PhotoRec(shopId, shopInName, byteArray, byteArrayMini, StringUtils.formatDateImg(Calendar.getInstance().getTime()));
        mapPhotoRec.put(newRec.getDocId(),newRec);
        exportData(newRec);
        newRec.setData(null);
        return newRec;
    }

    public Collection<PhotoRec> getPhotoRecListOrdered() {
        List<PhotoRec> list = new ArrayList<>();
        list.addAll(mapPhotoRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public String getPhotoFileName(PhotoRec req) {
        return integrationFile.getPhotoFileName(shopId, "IMG-"+req.getDocIdForExport()+".jpeg");
    }

    public void callToWS(CommandIn commandIn, String barcode, CommandFinishCallback commandFinishCallback) {
        // вызываем сервис
        CommandCall commandCall = new CommandCall(commandIn, barcode, shopId, userIn.getId());
        commandCall.call(commandFinishCallback);
    }

    public static class CheckMarkScannedResult {
        public Integer markScannedAsType;
        public BaseRecContent recContent;

        public CheckMarkScannedResult(Integer markScannedAsType, BaseRecContent recContent) {
            this.markScannedAsType = markScannedAsType;
            this.recContent = recContent;
        }
    }

    // Найти сканировалась ли уже эта марка
    public CheckMarkScannedResult checkMarkScanned(BaseRec rec, String mark) {
        // проверить сканирован ли этот ШК в накладной
        // пройтись по каждой позиции
        for (BaseRecContent recContent : rec.getRecContentList()) {
            // в каждой позиции пройтись по сканированным маркам
            for (BaseRecContentMark baseRecContentMark : recContent.getBaseRecContentMarkList()) {
                if (baseRecContentMark.getMarkScanned().equals(mark)) {
                    return new CheckMarkScannedResult(baseRecContentMark.getMarkScannedAsType(), recContent);
                }
            }
        }
        return null; // ничего не нашли - не сканирован
    }

    public static class CheckMarkScannedResultForCheckMark {
        public Integer state;
        public BaseRecContent recContent;

        public CheckMarkScannedResultForCheckMark(Integer state, BaseRecContent recContent) {
            this.state = state;
            this.recContent = recContent;
        }
    }

    // Найти сканировалась ли уже эта марка (для задания сканиования марок)
    public CheckMarkScannedResultForCheckMark checkMarkScannedForCheckMark(CheckMarkRec rec, String mark) {
        // сначала проверим, сканиовали ли эту марку уже
        for (CheckMarkRecContent recContent : rec.getCheckMarkRecContentList()) {
            // в каждой позиции пройтись по сканированным маркам
            for (CheckMarkRecContentMark checkMarkRecContentMark : recContent.getCheckMarkRecContentMarkList()) {
                if (checkMarkRecContentMark.getMarkScanned().equals(mark)) {
                    return new CheckMarkScannedResultForCheckMark(checkMarkRecContentMark.getState(), recContent);
                }
            }
        }
        // Если не нашли
        // поищем в начальном документе
        for (CheckMarkRecContent recContent : rec.getCheckMarkRecContentList()) {
            // в каждой позиции пройтись по маркам во входном документе
            CheckMarkContentIn checkMarkContentIn = (CheckMarkContentIn) recContent.getContentIn();

            for (CheckMarkMark checkMarkMark :  checkMarkContentIn.getMarks()) {
                if (checkMarkMark.getMark().equals(mark)) {
                    return new CheckMarkScannedResultForCheckMark(checkMarkMark.getState(), recContent);
                }
            }
        }
        return null; // ничего не нашли - не сканирован и нет во входном документе
    }

    public static class CheckMarkScannedResultForFindMark {
        public boolean scanned;
        public FindMarkRecContent recContent;

        public CheckMarkScannedResultForFindMark(FindMarkRecContent recContent, boolean scanned) {
            this.recContent = recContent;
            this.scanned = scanned;
        }
    }
    public CheckMarkScannedResultForFindMark checkMarkScannedForFindMark(FindMarkRec rec, String barCode) {
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

    // Найти непринятные позиции по алкокоду (может быть несколько)
    public List<IncomeRecContent> findIncomeRecContentListByAlcocodeNotDone(IncomeRec incomeRec, String alcocode, boolean checkCompletedToo) {
        List<IncomeRecContent> result = new ArrayList<>();
        // пройтись по каждой позиции
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            //
            if (alcocode != null && alcocode.equals(incomeRecContent.getContentIn().getAlccode())
                    && (incomeRecContent.getQtyAccepted() == null || incomeRecContent.getQtyAccepted() < incomeRecContent.getContentIn().getQty()
                        /*|| incomeRecContent.getContentIn().getQtyDirectInput() == 1 */ ) ) {
                result.add(incomeRecContent);
            }
        }

        // если в итоге ничего нет, но стоит флаг -  checkNotCompletedToo - выведем также принятые
        if (result.size() == 0 && checkCompletedToo) {
            // пройтись по каждой позиции
            for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
                if (alcocode != null && alcocode.equals(incomeRecContent.getContentIn().getAlccode()) ) {
                    result.add(incomeRecContent);
                }
            }
        }

        // Если позиций в списке более 1 - попытаться свернуть по датам
        if (result.size() > 1) {
            Map<String, IncomeRecContent> mapDate = new HashMap<>();
            for (IncomeRecContent irc : result) {
                mapDate.put(irc.getContentIn().getBottlingDate(), irc);
            }
            result.clear();
            result.addAll(mapDate.values());
        }

        // если пусто - непринятффе
        return result;
    }

    // найти позицию по ШК в приходе ЕГАИС
    public IncomeRecContent findIncomeRecContentByMark(IncomeRec incomeRec, String barcode) {
        IncomeContentIn foundIncomeContentIn = null;
        // в каждой позиции пройтись по маркам в ТТН
        for (IncomeContentIn incomeContentIn : incomeRec.getIncomeIn().getContent()) {
            for (IncomeContentMarkIn incomeContentMarkIn : incomeContentIn.getMarkInfo()) {
//                if (incomeContentMarkIn.getMark().equals(barcode)) { // by LAG 2022-01-18 было
                if (barcode.startsWith(incomeContentMarkIn.getMark())) { // by LAG 2022-01-18 стало
                    foundIncomeContentIn = incomeContentIn;
                }
            }
        }
        // Если нашли в ТТН эту марку
        if (foundIncomeContentIn != null) {
            // найти эту позицию по номеру и вернуть Rec
            for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
                if (incomeRecContent.getPosition().equals(foundIncomeContentIn.getPosition())) {
                    return incomeRecContent;
                }
            }
        }
        return null; // ничего не нашли
    }

    // найти марку по ШК в приходе ЕГАИС
    public IncomeContentMarkIn findIncomeContentMarkByMark(IncomeRec incomeRec, String barcode) {
        // в каждой позиции пройтись по маркам в ТТН
        for (IncomeContentIn incomeContentIn : incomeRec.getIncomeIn().getContent()) {
            for (IncomeContentMarkIn incomeContentMarkIn : incomeContentIn.getMarkInfo()) {
                if (incomeContentMarkIn.getMark().equals(barcode)) {
                    return incomeContentMarkIn;
                }
            }
        }
        return null; // ничего не нашли
    }

    public BaseRecContentMark findIncomeRecContentMarkByMarkScanned(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (BaseRecContentMark baseRecContentMark : incomeRecContent.getBaseRecContentMarkList()) {
                if (barcode.equals(baseRecContentMark.getMarkScanned())){
                    return baseRecContentMark;
                }
            }
        }
        return null;
    }

    public boolean exportData(IncomeRec incomeRec) {
        // Операция выгрузки: перед выгрузкой проверить что нет товарных строк с количеством больше нуля и не сопоставленной
        //номенклатурой. При наличии таких строк выгрузку не делать, показать сообщение "Имеются строки не сопоставленные
        //с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки
        //позиции."
        for (IncomeRecContent irc : incomeRec.getIncomeRecContentList()) {
            if (irc.getQtyAccepted() != null && irc.getQtyAccepted().doubleValue() > 0 && irc.getNomenIn() == null) {
                return false;
            }
        }
        incomeRec.setExported(true);
        writeLocalDataBaseRec(incomeRec);
        integrationFile.writeBaseRec(shopId, incomeRec);
        return true;
    }

    public boolean exportData(MoveRec moveRec) {
        exportDataBaseRec(moveRec);
        return true;
    }

    public boolean exportData(InvRec invRec) {
        invRec.setExported(true);
        saveDbInvRec(invRec);
        integrationFile.writeBaseRec(shopId, invRec);
        return true;
    }

    public boolean exportData(WriteoffRec writeoffRec) {
        writeoffRec.setExported(true);
        writeLocalDataWriteoffRec(writeoffRec);
        integrationFile.writeBaseRec(shopId, writeoffRec);
        return true;
    }

    public boolean exportData(PhotoRec photoRec) {
        integrationFile.writeBaseRec(shopId, photoRec);
        return true;
    }

    public boolean exportData(CheckMarkRec rec) {
        rec.setExported(true);
        writeLocalDataCheckMarkRec(rec);
        integrationFile.writeBaseRec(shopId, rec);
        return true;
    }

    private void exportDataBaseRec(BaseRec rec) {
        rec.setExported(true);
        writeLocalDataBaseRec(rec);
        integrationFile.writeBaseRec(shopId, rec);
    }

    public List<ShopIn> getListS() {
        return listS;
    }

    public List<UserIn> getListU() {
        return listU;
    }


    public String getShopInName() {
        ShopIn shopIn = findShopInById(this.shopId);
        if (shopIn == null ) {
            return "Не выбран";
        } else {
            return shopIn.getName();
        }
    }

    public String getShopId() {
        return shopId;
    }

    public void clearData(IncomeRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.clearData();
        writeLocalDataBaseRec(rec);
        exportDataBaseRec(rec);
    }

    public void rejectData(BaseRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.rejectData();
        writeLocalDataBaseRec(rec);
        exportDataBaseRec(rec);
    }

    public void rejectData(MoveRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.rejectData();
        writeLocalDataBaseRec(rec);
    }

    public void rejectData(WriteoffRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.rejectData();
        writeLocalWriteoffRec(rec);
    }

    public void rejectData(CheckMarkRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.rejectData();
        writeLocalDataCheckMarkRec(rec);
    }

    public void rejectData(InvRec rec) {
        writeLocalDataRec_ClearAllMarks(rec);
        rec.rejectData();
        saveDbInvRecWithContentDeletion(rec);
    }

    public int calculateQtyToAdd(IncomeRec incomeRec, IncomeRecContent incomeRecContent, String barcode) {
        // посчитать количесвто общее товара по этой упаковке
        int result = 0;
        IncomeContentBoxTreeIn icb = DaoMem.getDaoMem().findIncomeContentBoxTreeIn(incomeRecContent, barcode);
        if (icb != null) { // если коробка
            List<DaoMem.MarkInBox> resList = new ArrayList<>();
            DaoMem.getDaoMem().getAllIncomeRecMarksByBoxBarcode(resList, incomeRecContent, icb, 1);
            // пройтись по каждой из них
            for (DaoMem.MarkInBox mb : resList) {
                BaseRecContentMark ircm = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(incomeRec, mb.icm.getMark());
                //Если добавляемой марки еще нет в списке принятых: добавить ее в список принятых, признак сканирования установить в значение уровня вложенности упаковки (см. пред. пункт), принятое количество увеличить на 1 шт.
                if (ircm == null) {
                    result++;
                }
                //Если добавляемая марка уже есть в списке принятых - нужно только изменить признак сканирования (установить меньшее значение из того что уже стоит по марке и уровня вложенности текущей упаковки). Принятое количество менять не надо.
                if (ircm != null) {
                    // Не добавляем
                }
            }
        } else {
            result = (barcode == null) || incomeRecContent.getContentIn().getQtyDirectInput() == 1 ? 0 : 1; // все проверки должны быть выполнены до
        }
        return result;
    }

    public void checkIsNeedToUpdate(Activity activity) {
        try {
            // Проверить есть ли файл
            File fileToUpdate = integrationFile.loadNewApk();
            if (fileToUpdate.exists()) {
                final PackageManager pm = MainApp.getContext().getPackageManager();
                PackageInfo newInfo = pm.getPackageArchiveInfo(fileToUpdate.getAbsolutePath(), PackageManager.GET_META_DATA);
                if (newInfo.versionCode > BuildConfig.VERSION_CODE) {
                    // запрос обновления


                    File file = fileToUpdate;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        String fp = BuildConfig.APPLICATION_ID + ".provider";
                        Uri apkUri = FileProvider.getUriForFile(activity, fp, file);
                        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                        intent.setData(apkUri);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MainApp.getContext().startActivity(intent);
                    }


                }
            }
        } catch (Exception e) {
            Log.e("DaoMem", "checkIsNeedToUpdate", e);
        }
    }

    public BaseRecContentMark findIncomeRecContentScannedMarkBox(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (BaseRecContentMark baseRecContentMark : incomeRecContent.getBaseRecContentMarkList()) {
                if (barcode.equals(baseRecContentMark.getMarkScanned()) || barcode.equals(baseRecContentMark.getMarkScannedReal())){
                    return baseRecContentMark;
                }
            }
        }
        return null;
    }

    public IncomeRecContent findIncomeRecContentByMarkScanned(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (BaseRecContentMark baseRecContentMark : incomeRecContent.getBaseRecContentMarkList()) {
                if (barcode.equals(baseRecContentMark.getMarkScanned()) || barcode.equals(baseRecContentMark.getMarkScannedReal())){
                    return incomeRecContent;
                }
            }
        }
        return null;
    }

    public boolean readFilterOnIncomeRec(IncomeRec incomeRec) {
        boolean filter = sharedPreferences.getBoolean(BaseRec.KEY_FILTER+"_"+incomeRec.getDocId()+"_", true);
        return filter;
    }

    public void writeFilterOnIncomeRec(IncomeRec incomeRec, boolean checked) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(BaseRec.KEY_FILTER+"_"+incomeRec.getDocId()+"_", checked);
        ed.apply();
    }

    public boolean checkRecZeroQtyFact(BaseRec rec) {
        for (BaseRecContent irc : rec.getRecContentList()) {
            if (irc.getQtyAccepted() != null && irc.getQtyAccepted() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isQtyAcceptedFull(BaseRec rec) {
        for (BaseRecContent irc : rec.getRecContentList()) {
            if (irc.getQtyAccepted() == null ||
                    (irc.getQtyAccepted().equals(irc.getContentIn().getQty())  )) {
                return false;
            }
        }
        return true;
    }

    public void syncWiFiFtpShared() throws Exception {
        this.syncWiFiFtp.syncShared();
    }

    public void syncWiFiFtpShopDocs() throws Exception {
        this.syncWiFiFtp.syncShopDocs(shopId);
    }

    public boolean isNeedToCheckMark(String checkMark, BarcodeObject.BarCodeType barCodeType) {
        if ( (ShopIn.CHECKMARK_DM.equals(checkMark) && barCodeType == BarcodeObject.BarCodeType.DATAMATRIX) ||
                ShopIn.CHECKMARK_DMPDF.equals(checkMark)) {
            return true;
        }
        return false;
    }

    public boolean isNeedToCheckMarkForWriteoff(BarcodeObject.BarCodeType barCodeType) {
        ShopIn shopIn = findShopInById(shopId);
        if ( (ShopIn.CHECKMARK_DM.equals(shopIn.getCheckMark()) && barCodeType == BarcodeObject.BarCodeType.DATAMATRIX) ||
                ShopIn.CHECKMARK_DMPDF.equals(shopIn.getCheckMark())) {
            return true;
        }
        return false;
    }

    public MarkIn findMarkByBarcode(String barCode) {
        for (MarkIn markIn : listM) {
            if (markIn.getMark().equals(barCode)) {
                return markIn;
            }
        }
        return null;
    }

    public List<MarkIn> findMarksByBoxBarcode(String barCode) {
        List<MarkIn> result = new ArrayList<>();
        for (MarkIn markIn : listM) {
            if (barCode.equals(markIn.getBox())) {
                result.add(markIn);
            }
        }
        return result;
    }

    public AlcCodeIn findAlcCode(String alcCode) {
        for (AlcCodeIn alcCodeIn: listA) {
            if (alcCodeIn.getAlcCode().equals(alcCode)) {
                return alcCodeIn;
            }
        }
        return null;
    }

    public NomenIn findNomenInAlcoByBarCode(String barCodeIn) {
        for (NomenIn nomenIn: listN) {
            if (nomenIn.getBarcode() != null && nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK) {
                for (String barcode : nomenIn.getBarcode()) {
                    if (barCodeIn.equals(barcode)) {
                        return nomenIn;
                    }
                }
            }
        }
        return null;
    }

    public NomenIn findNomenInByBarCode(String barCodeIn) {
        for (NomenIn nomenIn: listN) {
            if (nomenIn.getBarcode() != null) {
                for (String barcode : nomenIn.getBarcode()) {
                    if (barCodeIn.equals(barcode)) {
                        return nomenIn;
                    }
                }
            }
        }
        return null;
    }

    public NomenIn findNomenInAlcoByNomenId(String nomenId) {
        for (NomenIn nomenIn: listN) {
            if (nomenIn.getId().equals(nomenId) && nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK) {
                return nomenIn;
            }
        }
        return null;
    }

    public NomenIn findNomenInByNomenId(String nomenId) {
        for (NomenIn nomenIn: listN) {
            if (nomenIn.getId().equals(nomenId)) {
                return nomenIn;
            }
        }
        return null;
    }

    public static class MarkInBox {
        public IncomeContentMarkIn icm;
        public int level;

        public MarkInBox(IncomeContentMarkIn icm, int level) {
            this.icm = icm;
            this.level = level;
        }
    }

    public void getAllIncomeRecMarksByBoxBarcode(List<MarkInBox> resList, IncomeRecContent irc, IncomeContentBoxTreeIn icbt, int level) {
        // Достать все марки по этой коробке
        for (IncomeContentMarkIn icm : irc.getContentIn().getMarkInfo()) {
            if (icm.getBox().equals(icbt.getBox())) {
                resList.add(new MarkInBox(icm, level));
            }
        }

        // Вызвать рекурсивно со всеми потомками
        for (IncomeContentBoxTreeIn icbtChild : irc.getContentIn().getBoxTree()) {
            if (icbtChild.getParentBox().equals(icbt.getBox())) {
                getAllIncomeRecMarksByBoxBarcode(resList, irc, icbtChild, level + 1);
            }
        }
    }

    public IncomeContentBoxTreeIn findIncomeContentBoxTreeIn(IncomeRecContent incomeRecContent, String box) {
        if (incomeRecContent.getContentIn().getBoxTree() != null) {
            for (IncomeContentBoxTreeIn icbt : incomeRecContent.getContentIn().getBoxTree()) {
                if (icbt.getBox().equals(box)) {
                    return icbt;
                }
            }
        }
        return null;
    }

    public IncomeRecContent findIncomeRecContentByBoxBarcode(IncomeRec incomeRec, String box) {
        // по всем строкам
        for (IncomeRecContent irc :incomeRec.getIncomeRecContentList()) {
            // по каждой строке - пройтись по BoxTree
            if (irc.getContentIn().getBoxTree() != null) {
                for (IncomeContentBoxTreeIn icbt : irc.getContentIn().getBoxTree()) {
                    if (icbt.getBox().equals(box)) {
                        return irc;
                    }
                }
            }
        }
        return null;
    }

    public UserIn getUserIn() {
        return userIn;
    }

    public void setUserIn(UserIn userIn) {
        this.userIn = userIn;
        if (userIn.getUsersPodrs() != null && userIn.getUsersPodrs().length == 1) {
            this.setShopId(userIn.getUsersPodrs()[0]);
        } else{
            this.setShopId(null);
        }
    }

    public Collection<MoveRec> getMoveRecListOrdered() {
        List<MoveRec> list = new ArrayList<>();
        list.addAll(mapMoveRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public Collection<InvRec> getInvRecListOrdered() {
        List<InvRec> list = new ArrayList<>();
        list.addAll(mapInvRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public String getBasePath() {
        return integrationFile.getBasePath();
    }

    public void deletePhoto(PhotoRec photoRec) {
        integrationFile.deleteFileRec(photoRec, shopId);
        mapPhotoRec.remove(photoRec.getDocId());
    }

}
