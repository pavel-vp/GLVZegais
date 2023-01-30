package com.glvz.egais.dao;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.glvz.egais.BuildConfig;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.daodb.AppDbHelper;
import com.glvz.egais.daodb.DaoDbCheckMark;
import com.glvz.egais.daodb.DaoDbDoc;
import com.glvz.egais.daodb.DaoDbFindMark;
import com.glvz.egais.daodb.DaoDbInv;
import com.glvz.egais.daodb.DaoDbPrice;
import com.glvz.egais.daodb.DaoDbWriteOff;
import com.glvz.egais.integration.model.AlcCodeIn;
import com.glvz.egais.integration.model.CommandIn;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.integration.model.SetupFtp;
import com.glvz.egais.integration.model.ShopIn;
import com.glvz.egais.integration.model.UserIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkContentIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkMark;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.integration.sdcard.Integration;
import com.glvz.egais.integration.sdcard.IntegrationSDCard;
import com.glvz.egais.integration.wifi.SyncWiFiFtp;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.checkmark.CheckMarkRec;
import com.glvz.egais.model.checkmark.CheckMarkRecContent;
import com.glvz.egais.model.checkmark.CheckMarkRecContentMark;
import com.glvz.egais.model.findmark.FindMarkRec;
import com.glvz.egais.model.findmark.FindMarkRecContent;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.model.income.IncomeRecContent;
import com.glvz.egais.model.inv.InvRec;
import com.glvz.egais.model.inv.InvRecContent;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.model.photo.PhotoRec;
import com.glvz.egais.model.price.PriceRec;
import com.glvz.egais.model.price.PriceRecContent;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.service.CommandCall;
import com.glvz.egais.service.CommandFinishCallback;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaoMem {


    public static final String KEY_LAST_DOCID = "last_docid";
    public static final String KEY_WRITEOFF = "writeoff";
    public static final String KEY_CHECKMARK = "checkmark";
    public static final String KEY_FINDMARK = "findmark";
    public static final String KEY_INV = "inv";
    public static final String KEY_DOC = "doc";
    public static final String KEY_PRICE = "price";
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
    Map<String, PriceRec> mapPriceRec;
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
        Set<String> allRemainRecs = integrationFile.clearOldData(Integer.valueOf(MainApp.getContext().getResources().getString(R.string.num_days_old)));
        clearStoredDataNotInList(allRemainRecs, Integer.valueOf(MainApp.getContext().getResources().getString(R.string.num_days_old)));

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

    private void clearStoredDataNotInList(Set<String> allRemainRecs, int numDaysOld) {
        // удалить документы из Списания по дате
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -numDaysOld);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        DaoDbWriteOff.getDaoDbWriteOff().deleteWriteoffByDate(sdf.format(calendar.getTime()));

        // По остальным документам удалим все документы которых нет в списке
        DaoDbCheckMark.getDaoDbCheckMark().deleteCheckMarkNotInList(allRemainRecs);
        DaoDbDoc.getDaoDbDoc().deleteDocNotInList(allRemainRecs);
        DaoDbFindMark.getDaoDbFindMark().deleteFindMarkNotInList(allRemainRecs);
        DaoDbInv.getDaoDbInv().deleteInvNotInList(allRemainRecs);
        DaoDbPrice.getDaoDbPrice().deletePriceNotInList(allRemainRecs);

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
        mapWriteoffRec = DaoDbWriteOff.getDaoDbWriteOff().readWriteoffRecList(shopId);
        mapPriceRec = DaoDbPrice.getDaoDbPrice().readPriceRecList(shopId);
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
            DaoDbDoc.getDaoDbDoc().readDbDataDoc(moveRec);
            map.put(moveIn.getDocId(), moveRec);
        }

        return map;
    }

    private Map<String,CheckMarkRec> readCheckMarkRec() {
        Map<String, CheckMarkRec> map = new HashMap<>();

        for (CheckMarkIn checkMarkIn : listCheckMarkIn) {
            CheckMarkRec rec = new CheckMarkRec(checkMarkIn.getDocId(), checkMarkIn);
            DaoDbCheckMark.getDaoDbCheckMark().readDbDataCheckMark(rec);
            map.put(checkMarkIn.getDocId(), rec);
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
            DaoDbFindMark.getDaoDbFindMark().readDbDataFindMark(findMarkRec);
            map.put(findMarkRec.getDocId(), findMarkRec);
        }

        return map;
    }

    private Map<String,IncomeRec> readIncomeRec() {
        Map<String, IncomeRec> map = new HashMap<>();

        for (IncomeIn incomeIn : listIncomeIn) {
            IncomeRec incomeRec = new IncomeRec(incomeIn.getWbRegId(), incomeIn);
            DaoDbDoc.getDaoDbDoc().readDbDataDoc(incomeRec);
            map.put(incomeIn.getWbRegId(), incomeRec);
        }

        return map;
    }
    private Map<String,InvRec> readInvRec() {
        Map<String, InvRec> map = new HashMap<>();

        for (InvIn invIn : listInvIn) {
            InvRec invRec = new InvRec(invIn.getDocId(), invIn);
            DaoDbInv.getDaoDbInv().readDbDataInv(invRec);
            map.put(invIn.getDocId(), invRec);
        }

        return map;
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

    public Collection<PriceRec> getPriceRecListOrdered() {
        List<PriceRec> list = new ArrayList<>();
        list.addAll(mapPriceRec.values());

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

    public Map<String, PriceRec> getMapPriceRec() {
        return mapPriceRec;
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

    public Collection<PriceRecContent> getPriceRecContentList(String docId) {
        return mapPriceRec.get(docId).getPriceRecContentList();
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
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRec(shopId, newRec);
        return newRec;
    }

    public PriceRec addNewPriceRec(String shopId, String shopInName) {
        PriceRec newRec = new PriceRec(shopId, shopInName);
        mapPriceRec.put(newRec.getDocId(),newRec);
        DaoDbPrice.getDaoDbPrice().saveDbPriceRec(shopId, newRec);
        return newRec;
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
        rejectData(writeoffRec);
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRecDeletion(writeoffRec.getDocId());
        // Удалить сам файл
        integrationFile.deleteFileRec(writeoffRec, shopId);
    }

    public void deleteData(PriceRec pricefRec) {
        // удалять документ из списка и его out-файл (если есть).
        mapWriteoffRec.remove(pricefRec.getDocId());
        rejectData(pricefRec);
        DaoDbPrice.getDaoDbPrice().saveDbPriceRecDeletion(shopId, pricefRec);
        // Удалить сам файл
        integrationFile.deleteFileRec(pricefRec, shopId);
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

    public void callToWS(CommandIn commandIn, String barcode, String nomen, CommandFinishCallback commandFinishCallback) {
        // вызываем сервис
        CommandCall commandCall = new CommandCall(commandIn, barcode, shopId, userIn.getId(), nomen);
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
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(incomeRec);
        integrationFile.writeBaseRec(shopId, incomeRec);
        return true;
    }

    public boolean exportData(MoveRec moveRec) {
        exportDataBaseRec(moveRec);
        return true;
    }

    public boolean exportData(InvRec invRec) {
        invRec.setExported(true);
        DaoDbInv.getDaoDbInv().saveDbInvRec(invRec);
        integrationFile.writeBaseRec(shopId, invRec);
        return true;
    }

    public boolean exportData(WriteoffRec writeoffRec) {
        writeoffRec.setExported(true);
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRec(shopId, writeoffRec);
        integrationFile.writeBaseRec(shopId, writeoffRec);
        return true;
    }

    public boolean exportData(PriceRec priceRec) {
        priceRec.setExported(true);
        DaoDbPrice.getDaoDbPrice().saveDbPriceRec(shopId, priceRec);
        integrationFile.writeBaseRec(shopId, priceRec);
        return true;
    }

    public boolean exportData(PhotoRec photoRec) {
        integrationFile.writeBaseRec(shopId, photoRec);
        return true;
    }

    public boolean exportData(CheckMarkRec rec) {
        rec.setExported(true);
        DaoDbCheckMark.getDaoDbCheckMark().saveDbCheckMarkRec(rec);
        integrationFile.writeBaseRec(shopId, rec);
        return true;
    }

    private void exportDataBaseRec(BaseRec rec) {
        rec.setExported(true);
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(rec);
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
        rec.clearData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(rec);
        exportDataBaseRec(rec);
    }

    public void rejectData(BaseRec rec) {
        rec.rejectData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(rec);
        exportDataBaseRec(rec);
    }

    public void rejectData(MoveRec rec) {
        rec.rejectData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbDoc.getDaoDbDoc().saveDbDocRec(rec);
    }

    public void rejectData(WriteoffRec rec) {
        rec.rejectData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbWriteOff.getDaoDbWriteOff().saveDbWriteoffRecWithOnlyContentDeletion(shopId, rec);
    }

    public void rejectData(PriceRec rec) {
        rec.rejectData();
        DaoDbPrice.getDaoDbPrice().saveDbPriceRecWithOnlyContentDeletion(shopId, rec);
    }

    public void rejectData(CheckMarkRec rec) {
        rec.rejectData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbCheckMark.getDaoDbCheckMark().saveDbCheckMarkRec(rec);
    }

    public void rejectData(InvRec rec) {
        rec.rejectData();
        DaoDbDoc.getDaoDbDoc().writeLocalDataRec_ClearAllMarks(rec);
        DaoDbInv.getDaoDbInv().saveDbInvRecWithContentDeletion(rec);
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
