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
import com.glvz.egais.BuildConfig;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.integration.model.doc.DocContentIn;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentIn;
import com.glvz.egais.integration.model.doc.income.IncomeContentMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.integration.sdcard.Integration;
import com.glvz.egais.integration.sdcard.IntegrationSDCard;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.wifi.SyncWiFiFtp;
import com.glvz.egais.model.*;
import com.glvz.egais.model.income.*;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;

import java.io.*;
import java.util.*;

public class DaoMem {


    private static DaoMem daoMem = null;

    public static DaoMem getDaoMem() {
        if (daoMem == null) {
            daoMem = new DaoMem();
        }
        return daoMem;
    }

    private DaoMem() {
        initDictionary();
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
    List<IncomeIn> listIncomeIn;
    List<MoveIn> listMoveIn;
    List<WriteoffRec> listWriteoff;

    Map<String, IncomeRec> mapIncomeRec;
    Map<String, MoveRec> mapMoveRec;
    Map<String, WriteoffRec> mapWriteoffRec;
    SharedPreferences sharedPreferences;

    private UserIn userIn;
    private String shopId;


    public void initDictionary() {
        File path = new File(Environment.getExternalStorageDirectory(), MainApp.getContext().getResources().getString(R.string.path_exchange));
        sharedPreferences = MainApp.getContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);
        integrationFile = new IntegrationSDCard(path.getAbsolutePath());
        List<DocIn> allRemainRecs = integrationFile.clearOldData(Integer.valueOf(MainApp.getContext().getResources().getString(R.string.num_days_old)));
        clearStoredDataNotInList(allRemainRecs);
        listU = integrationFile.loadUsers();
        listS = integrationFile.loadShops();
        listP = integrationFile.loadPosts();
        listN = integrationFile.loadNomen();
        listA = integrationFile.loadAlcCode();
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

    public void initDocuments() {
        integrationFile.initDirectories(shopId);
        listIncomeIn = integrationFile.loadIncome(shopId);
        listMoveIn = integrationFile.loadMove(shopId);
        listWriteoff = integrationFile.loadWriteoff(shopId);

        listM = integrationFile.loadMark(shopId);
        document = new DocumentMem(listIncomeIn);
        documentMove = new DocumentMoveMem(listMoveIn);

        // Прочитать локальные данные
        mapIncomeRec = readIncomeRec();
        mapMoveRec = readMoveRec();
        mapWriteoffRec = readWriteoffRec();
        MessageUtils.showToastMessage("Данные загружены");

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

    private Map<String,WriteoffRec> readWriteoffRec() {
        Map<String, WriteoffRec> map = new HashMap<>();

        for (WriteoffRec writeoffRec : listWriteoff) {
            readLocalData(writeoffRec);
            map.put(writeoffRec.getDocId(), writeoffRec);
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

    private void clearStoredDataNotInList(List<DocIn> recList) {
        Map<String, ?> allPrefs = sharedPreferences.getAll();
        for (DocIn rec : recList) {
            Iterator<? extends Map.Entry<String, ?>> iter = allPrefs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String,?> entry  = iter.next();
                if (entry.getKey().contains("_" + rec.getDocId() + "_")) {
                    // Удалить
                    iter.remove();
                }
            }
        }

        // Оставшиеся удаляем
        SharedPreferences.Editor ed = sharedPreferences.edit();
        for (Map.Entry<String,?> entry : allPrefs.entrySet()) {
            // НЕ связанные с документами
            if (!entry.getKey().equals(BaseRec.KEY_SHOPID)) {
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

    Comparator docRecDateComparator = new Comparator<BaseRec>() {
        @Override
        public int compare(BaseRec lhs, BaseRec rhs) {


            Date d1 = lhs.getDate();
            Date d2 = rhs.getDate();

            if (d1.before(d2)) return 1;
            if (d1.after(d2)) return -1;

            int res = lhs.getAgentName().compareTo(rhs.getAgentName());
            if (res != 0) return res;

            return lhs.getDocNum().compareTo(rhs.getDocNum());
        }
    };

    public Collection<IncomeRec> getIncomeRecListOrdered() {

        List<IncomeRec> list = new ArrayList<>();
        list.addAll(mapIncomeRec.values());

        Collections.sort(list, docRecDateComparator);
        return list;
    }

    public Map<String, IncomeRec> getMapIncomeRec() {
        return mapIncomeRec;
    }

    public Map<String, MoveRec> getMapMoveRec() {
        return mapMoveRec;
    }

    public Collection<IncomeRecContent> getIncomeRecContentList(String wbRegId) {
        return mapIncomeRec.get(wbRegId).getIncomeRecContentList();
    }

    public Collection<MoveRecContent> getMoveRecContentList(String docId) {
        return mapMoveRec.get(docId).getMoveRecContentList();
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
                if (incomeContentMarkIn.getMark().equals(barcode)) {
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

    public boolean exportData(WriteoffRec writeoffRec) {
        exportDataBaseRec(writeoffRec);
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

    public void rejectData(BaseRec rec) {
        rec.rejectData();
        writeLocalDataBaseRec(rec);
        exportDataBaseRec(rec);
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

                    intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainApp.getContext().startActivity(intent);
                }


            }
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

    public MarkIn findMarkByBarcode(String barCode) {
        for (MarkIn markIn : listM) {
            if (markIn.getMark().equals(barCode)) {
                return markIn;
            }
        }
        return null;
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

    public NomenIn findNomenInAlcoByNomenId(String nomenId) {
        for (NomenIn nomenIn: listN) {
            if (nomenIn.getId().equals(nomenId) && nomenIn.getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK) {
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


}
