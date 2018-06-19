package com.glvz.egais.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import com.glvz.egais.MainApp;
import com.glvz.egais.integration.Integration;
import com.glvz.egais.integration.IntegrationSDCard;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.model.IncomeRec;
import com.glvz.egais.model.IncomeRecContent;
import com.glvz.egais.model.IncomeRecContentStatus;
import com.glvz.egais.model.IncomeRecStatus;

import java.util.*;

public class DaoMem {

    private static final String KEY_CNTDONE = "cntdone";
    private static final String KEY_STATUS = "status";
    private static final String KEY_POS_ID1C = "pos_id1c";
    private static final String KEY_POS_STATUS = "pos_status";
    private static final String KEY_POS_QTYACCEPTED = "pos_qtyaccepted";


    private static DaoMem daoMem = null;

    public static DaoMem getDaoMem() {
        if (daoMem == null) {
            daoMem = new DaoMem();
        }
        return daoMem;
    }

    Integration integrationFile;

    Dictionary dictionary;

    public Document getDocument() {
        return document;
    }

    Document document;

    List<ShopIn> listS;
    List<PostIn> listP;
    List<NomenIn> listN;
    List<IncomeIn> listIncomeIn;

    Map<String, IncomeRec> mapIncomeRec;
    SharedPreferences sharedPreferences;


    public void init(String path, String shopId) {
        sharedPreferences = MainApp.getContext().getSharedPreferences("settings", Activity.MODE_PRIVATE);

        integrationFile = new IntegrationSDCard(path);
        listS = integrationFile.loadShops();
        listP = integrationFile.loadPosts();
        listN = integrationFile.loadNomen();
        dictionary = new DictionaryMem(listS, listP, listN);
        listIncomeIn = integrationFile.loadIncome(shopId);
        document = new DocumentMem(listIncomeIn);

        // Прочитать локальные данные
        mapIncomeRec = readIncomeRec();

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
        // пройтись по строкам и прочитать доп.данные
        incomeRec.getIncomeRecContentList().clear();
        for (IncomeContentIn incomeContentIn : incomeRec.getIncomeIn().getContent()) {
            IncomeRecContent incomeRecContent = new IncomeRecContent(incomeContentIn.getPosition(), incomeContentIn);
            // прочитать данные по строке локальные
            incomeRecContent.setId1c(sharedPreferences.getString(KEY_POS_ID1C+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), null));
            incomeRecContent.setStatus( IncomeRecContentStatus.valueOf(
                    sharedPreferences.getString(KEY_POS_STATUS+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), IncomeRecContentStatus.NOT_ENTERED.toString())));
            float qty = sharedPreferences.getFloat(KEY_POS_QTYACCEPTED+"_"+incomeRec.getWbRegId()+"_"+incomeContentIn.getPosition(), 0);
            if (qty != 0) {
                incomeRecContent.setQtyAccepted(Double.valueOf(qty));
            }
            incomeRec.getIncomeRecContentList().add(incomeRecContent);
        }

    }

    public void writeLocalDataIncomeRec(IncomeRec incomeRec) {
        SharedPreferences.Editor ed = sharedPreferences.edit();
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
        ed.putString(KEY_POS_STATUS+"_"+wbRegId+"_"+incomeRecContent.getPosition(), incomeRecContent.getStatus().toString());
        float qty = 0;
        if (incomeRecContent.getQtyAccepted() != null) {
            qty = incomeRecContent.getQtyAccepted().floatValue();
        }
        ed.putFloat(KEY_POS_QTYACCEPTED+"_"+wbRegId+"_"+incomeRecContent.getPosition(), qty);
        ed.apply();
    }


    public Collection<IncomeRec> getIncomeRecList() {
        return mapIncomeRec.values();
    }

    public Map<String, IncomeRec> getMapIncomeRec() {
        return mapIncomeRec;
    }

    public Collection<IncomeRecContent> getIncomeRecContentList(String wbRegId) {
        return mapIncomeRec.get(wbRegId).getIncomeRecContentList();
    }

    public IncomeRecContent getIncomeRecContentByPosition(IncomeRec incomeRec, Integer position) {
        for (IncomeRecContent incomeRecContent : incomeRec.getIncomeRecContentList()) {
            if (incomeRecContent.getPosition().intValue() == position.intValue()) {
                return incomeRecContent;
            }
        }
        return null; // FIXME: обработку ексепшнов и сообщений пользователю
    }

    public Dictionary getDictionary() {
        return dictionary;
    }


}
