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
import com.glvz.egais.integration.Integration;
import com.glvz.egais.integration.IntegrationSDCard;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.model.*;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;

import java.io.File;
import java.util.*;

public class DaoMem {

    private static final String KEY_SHOPID = "shopid";
    private static final String KEY_CNTDONE = "cntdone";
    private static final String KEY_STATUS = "status";
    private static final String KEY_EXPORTED = "exported";
    private static final String KEY_FILTER = "incomefilter";
    private static final String KEY_POS_ID1C = "pos_id1c";
    private static final String KEY_POS_BARCODE = "pos_barcode";
    private static final String KEY_POS_STATUS = "pos_status";
    private static final String KEY_POS_QTYACCEPTED = "pos_qtyaccepted";
    private static final String KEY_POS_MARKSCANNED_CNT = "pos_markscanned_cnt";
    private static final String KEY_POS_MARKSCANNED = "pos_markscanned";
    private static final String KEY_POS_MARKSCANNED_ASTYPE = "pos_markscanned_astype";
    private static final String KEY_POS_MARKSCANNEDREAL = "pos_markscannedreal";


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

    List<ShopIn> listS;
    List<PostIn> listP;
    List<UserIn> listU;
    List<NomenIn> listN;
    List<IncomeIn> listIncomeIn;

    Map<String, IncomeRec> mapIncomeRec;
    SharedPreferences sharedPreferences;

    private UserIn userIn;
    private String shopId;


    private void initDictionary() {
        File path = new File(Environment.getExternalStorageDirectory(), MainApp.getContext().getResources().getString(R.string.path_exchange));
        sharedPreferences = MainApp.getContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);
        integrationFile = new IntegrationSDCard(path.getAbsolutePath());
        listU = integrationFile.loadUsers();
        listS = integrationFile.loadShops();
        listP = integrationFile.loadPosts();
        listN = integrationFile.loadNomen();
        dictionary = new DictionaryMem(listU, listS, listP, listN);
        String shopIdStored = sharedPreferences.getString(KEY_SHOPID, null);
        if (shopIdStored != null) {
            ShopIn shopInStored = findShopInById(shopIdStored);
            if (shopIdStored != null) {
                setShopId(shopInStored.getId());
            }
        }
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(KEY_SHOPID, this.shopId);
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
        listIncomeIn = integrationFile.loadIncome(shopId);
        document = new DocumentMem(listIncomeIn);

        // Прочитать локальные данные
        mapIncomeRec = readIncomeRec();
        MessageUtils.showToastMessage("Данные загружены");

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

    private void readLocalData(IncomeRec incomeRec) {
        incomeRec.setCntDone(sharedPreferences.getInt(KEY_CNTDONE+"_"+incomeRec.getWbRegId(), 0));
        incomeRec.setStatus(IncomeRecStatus.valueOf(sharedPreferences.getString(KEY_STATUS+"_"+incomeRec.getWbRegId(), IncomeRecStatus.NEW.toString())));
        incomeRec.setExported(sharedPreferences.getBoolean(KEY_EXPORTED + "_" + incomeRec.getWbRegId(), false));
        // пройтись по строкам и прочитать доп.данные
        incomeRec.getIncomeRecContentList().clear();
        for (IncomeContentIn incomeContentIn : incomeRec.getIncomeIn().getContent()) {
            IncomeRecContent incomeRecContent = new IncomeRecContent(incomeContentIn.getPosition(), incomeContentIn);
            // прочитать данные по строке локальные
            incomeRecContent.setId1c(sharedPreferences.getString(KEY_POS_ID1C+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), null));
            String barcode = sharedPreferences.getString(KEY_POS_BARCODE+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), null);
            incomeRecContent.setNomenIn(dictionary.findNomenById(incomeRecContent.getId1c()), barcode);
            incomeRecContent.setStatus( IncomeRecContentStatus.valueOf(
                    sharedPreferences.getString(KEY_POS_STATUS+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), IncomeRecContentStatus.NOT_ENTERED.toString())));
            float qty = sharedPreferences.getFloat(KEY_POS_QTYACCEPTED+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), 0);
            if (qty != 0) {
                incomeRecContent.setQtyAccepted(Double.valueOf(qty));
            }
            int cnt = sharedPreferences.getInt(KEY_POS_MARKSCANNED_CNT+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), 0);
            if (cnt > 0) {
                for (int i = 1; i <= cnt; i++) {
                    String mark = sharedPreferences.getString(KEY_POS_MARKSCANNED+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition()+"_" + i, null);
                    int typeAs = sharedPreferences.getInt(KEY_POS_MARKSCANNED_ASTYPE+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition()+"_" + i, 0);
                    String realMark = sharedPreferences.getString(KEY_POS_MARKSCANNEDREAL+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition()+"_" + i, null);
                    incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(mark, typeAs, realMark));
                }
            }
            incomeRec.getIncomeRecContentList().add(incomeRecContent);
        }

    }

    public void writeLocalDataIncomeRec(IncomeRec incomeRec) {
        // Синхронизируем общий статус накладной
        // Посчитаем число реально принятых строк
        int cntDone = 0;
        int cntZero = 0;
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            if (incomeRecContent.getStatus() == IncomeRecContentStatus.DONE){
                cntDone++;
            }
            if (incomeRecContent.getQtyAccepted() != null &&  incomeRecContent.getQtyAccepted().equals(Double.valueOf(0))
                    && incomeRecContent.getNomenIn() == null){
                cntZero++;
            }
        }
        // Если все записи =0, и связок с товарами нет и в статусе накл = отказ, оставим отказ
        if (cntZero == incomeRec.getIncomeRecContentList().size()
                && incomeRec.getStatus() == IncomeRecStatus.REJECTED) {
            // оставим отказ
        } else {
            if (incomeRec.getIncomeIn().getContent().length == cntDone) {
                incomeRec.setStatus(IncomeRecStatus.DONE);
            } else {
                incomeRec.setStatus(IncomeRecStatus.INPROGRESS);
            }
        }
        incomeRec.setCntDone(cntDone);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(KEY_EXPORTED+"_"+incomeRec.getWbRegId(), incomeRec.isExported());
        ed.putInt(KEY_CNTDONE+"_"+incomeRec.getWbRegId(), incomeRec.getCntDone());
        ed.putString(KEY_STATUS+"_"+incomeRec.getWbRegId(), incomeRec.getStatus().toString());
        ed.apply();
        // записать данные по строкам
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            writeLocalDataIncomeRecContent(incomeRec.getWbRegId(), incomeRecContent);
        }
    }

    public void writeLocalDataIncomeRecContent(String wbRegId, IncomeRecContent incomeRecContent) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(KEY_POS_ID1C+"_"+wbRegId+"_"+incomeRecContent.getPosition(), incomeRecContent.getId1c());
        ed.putString(KEY_POS_BARCODE+"_"+wbRegId+"_"+incomeRecContent.getPosition(), incomeRecContent.getBarcode());
        ed.putString(KEY_POS_STATUS+"_"+wbRegId+"_"+incomeRecContent.getPosition(), incomeRecContent.getStatus().toString());
        float qty = 0;
        if (incomeRecContent.getQtyAccepted() != null) {
            qty = incomeRecContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(KEY_POS_QTYACCEPTED+"_"+wbRegId+"_"+incomeRecContent.getPosition(), qty);
        ed.putInt(KEY_POS_MARKSCANNED_CNT + "_"+wbRegId+"_"+incomeRecContent.getPosition(), incomeRecContent.getIncomeRecContentMarkList().size());
        int idx = 1;
        for (IncomeRecContentMark incomeRecContentMark : incomeRecContent.getIncomeRecContentMarkList()) {
            ed.putString(KEY_POS_MARKSCANNED + "_"+wbRegId+"_"+incomeRecContent.getPosition()+"_"+idx, incomeRecContentMark.getMarkScanned());
            ed.putInt(KEY_POS_MARKSCANNED_ASTYPE + "_"+wbRegId+"_"+incomeRecContent.getPosition()+"_"+idx, incomeRecContentMark.getMarkScannedAsType());
            ed.putString(KEY_POS_MARKSCANNEDREAL + "_"+wbRegId+"_"+incomeRecContent.getPosition()+"_"+idx, incomeRecContentMark.getMarkScannedReal());
            idx++;
        }
        ed.apply();
    }


    public Collection<IncomeRec> getIncomeRecListOrdered() {

        List<IncomeRec> list = new ArrayList<>();
        list.addAll(mapIncomeRec.values());

        Collections.sort(list, new Comparator<IncomeRec>() {
            @Override
            public int compare(IncomeRec lhs, IncomeRec rhs) {


                Date d1 = StringUtils.jsonStringToDate(lhs.getIncomeIn().getDate());
                Date d2 = StringUtils.jsonStringToDate(rhs.getIncomeIn().getDate());

                if (d1.before(d2)) return 1;
                if (d1.after(d2)) return -1;

                int res = lhs.getIncomeIn().getPostName().compareTo(rhs.getIncomeIn().getPostName());
                if (res != 0) return res;

                return lhs.getIncomeIn().getNumber().compareTo(rhs.getIncomeIn().getNumber());
            }
        });
        return list;
    }

    public Map<String, IncomeRec> getMapIncomeRec() {
        return mapIncomeRec;
    }

    public Collection<IncomeRecContent> getIncomeRecContentList(String wbRegId) {
        return mapIncomeRec.get(wbRegId).getIncomeRecContentList();
    }

    public IncomeRecContent getIncomeRecContentByPosition(IncomeRec incomeRec, String position) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            if (incomeRecContent.getPosition().equals(position)) {
                return incomeRecContent;
            }
        }
        return null; // FIXME: обработку ексепшнов и сообщений пользователю
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    // Найти сканировалась ли уже эта марка
    public Integer checkMarkScanned(IncomeRec incomeRec, String mark) {
        // проверить сканирован ли этот ШК в накладной
        // пройтись по каждой позиции
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            // в каждой позиции пройтись по сканированным маркам
            for (IncomeRecContentMark incomeRecContentMark : incomeRecContent.getIncomeRecContentMarkList()) {
                if (incomeRecContentMark.getMarkScanned().equals(mark)) {
                    return incomeRecContentMark.getMarkScannedAsType();
                }
            }
        }
        return null; // ничего не нашли - не сканирован
    }

    // Найти непринятные позиции по алкокоду (может быть несколько)
    public List<IncomeRecContent> findIncomeRecContentListByAlcocodeNotDone(IncomeRec incomeRec, String alcocode) {
        List<IncomeRecContent> result = new ArrayList<>();
        // пройтись по каждой позиции
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            //
            if (alcocode != null && alcocode.equals(incomeRecContent.getIncomeContentIn().getAlccode())
                    && (incomeRecContent.getQtyAccepted() == null || incomeRecContent.getQtyAccepted() < incomeRecContent.getIncomeContentIn().getQty() ) ) {
                result.add(incomeRecContent);
            }
        }
        // Если позиций в списке более 1 - попытаться свернуть по датам
        if (result.size() > 1) {
            Map<String, IncomeRecContent> mapDate = new HashMap<>();
            for (IncomeRecContent irc : result) {
                mapDate.put(irc.getIncomeContentIn().getBottlingDate(), irc);
            }
            result.clear();
            result.addAll(mapDate.values());
        }

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

    public IncomeRecContentMark findIncomeRecContentMarkByMarkScanned(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (IncomeRecContentMark incomeRecContentMark : incomeRecContent.getIncomeRecContentMarkList()) {
                if (barcode.equals(incomeRecContentMark.getMarkScanned())){
                    return incomeRecContentMark;
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
        writeLocalDataIncomeRec(incomeRec);
        integrationFile.writeIncomeRec(shopId, incomeRec);
        return true;
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

    public void rejectData(IncomeRec incomeRec) {
        // Пройтись по всем строкам, очистить связки с товаром и проставить везде нули
        for (IncomeRecContent irc : incomeRec.getIncomeRecContentList()) {
            irc.setNomenIn(null, null);
            irc.setQtyAccepted(Double.valueOf(0));
            irc.getIncomeRecContentMarkList().clear();
            irc.setStatus(IncomeRecContentStatus.REJECTED);
        }
        incomeRec.setStatus(IncomeRecStatus.REJECTED);
        writeLocalDataIncomeRec(incomeRec);
        exportData(incomeRec);
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
                IncomeRecContentMark ircm = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(incomeRec, mb.icm.getMark());
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
            result = barcode == null ? 0 : 1; // все проверки должны быть выполнены до
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

    public IncomeRecContentMark findIncomeRecContentScannedMarkBox(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (IncomeRecContentMark incomeRecContentMark : incomeRecContent.getIncomeRecContentMarkList()) {
                if (barcode.equals(incomeRecContentMark.getMarkScanned()) || barcode.equals(incomeRecContentMark.getMarkScannedReal())){
                    return incomeRecContentMark;
                }
            }
        }
        return null;
    }

    public IncomeRecContent findIncomeRecContentByMarkScanned(IncomeRec incomeRec, String barcode) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            for (IncomeRecContentMark incomeRecContentMark : incomeRecContent.getIncomeRecContentMarkList()) {
                if (barcode.equals(incomeRecContentMark.getMarkScanned()) || barcode.equals(incomeRecContentMark.getMarkScannedReal())){
                    return incomeRecContent;
                }
            }
        }
        return null;
    }

    public boolean readFilterOnIncomeRec(IncomeRec incomeRec) {
        boolean filter = sharedPreferences.getBoolean(KEY_FILTER+"_"+incomeRec.getWbRegId(), true);
        return filter;
    }

    public void writeFilterOnIncomeRec(IncomeRec incomeRec, boolean checked) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putBoolean(KEY_FILTER+"_"+incomeRec.getWbRegId(), checked);
        ed.apply();
    }

    public boolean checkIncomeRecZeroQtyFact(IncomeRec incomeRec) {
        for (IncomeRecContent irc : incomeRec.getIncomeRecContentList()) {
            if (irc.getQtyAccepted() != null && irc.getQtyAccepted() > 0) {
                return false;
            }
        }
        return true;
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
        for (IncomeContentMarkIn icm : irc.getIncomeContentIn().getMarkInfo()) {
            if (icm.getBox().equals(icbt.getBox())) {
                resList.add(new MarkInBox(icm, level));
            }
        }

        // Вызвать рекурсивно со всеми потомками
        for (IncomeContentBoxTreeIn icbtChild : irc.getIncomeContentIn().getBoxTree()) {
            if (icbtChild.getParentBox().equals(icbt.getBox())) {
                getAllIncomeRecMarksByBoxBarcode(resList, irc, icbtChild, level + 1);
            }
        }
    }

    public IncomeContentBoxTreeIn findIncomeContentBoxTreeIn(IncomeRecContent incomeRecContent, String box) {
        if (incomeRecContent.getIncomeContentIn().getBoxTree() != null) {
            for (IncomeContentBoxTreeIn icbt : incomeRecContent.getIncomeContentIn().getBoxTree()) {
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
            if (irc.getIncomeContentIn().getBoxTree() != null) {
                for (IncomeContentBoxTreeIn icbt : irc.getIncomeContentIn().getBoxTree()) {
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

}
