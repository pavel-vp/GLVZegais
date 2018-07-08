package com.glvz.egais.integration;

import android.media.MediaScannerConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.glvz.egais.MainApp;
import com.glvz.egais.integration.model.IncomeIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.integration.model.ShopIn;
import com.glvz.egais.model.IncomeRec;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pasha on 07.06.18.
 */
public class IntegrationSDCard implements Integration {

    private String basePath;
    private static final String APK_DIR = "APK";
    private static final String DIC_DIR = "Dic";
    private static final String SHOPS_DIR = "Shops";
    private static final String IN_DIR = "IN";
    private static final String OUT_DIR = "OUT";

    private static final String SHOP_FILE = "shops.json";
    private static final String POST_FILE = "posts.json";
    private static final String NOMEN_FILE = "nomen.json";

    private ObjectMapper objectMapper = new ObjectMapper();


    public IntegrationSDCard(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public List<ShopIn> loadShops() {
        File pathToFile = new File(basePath  + "/" + DIC_DIR, SHOP_FILE);
        List<ShopIn> listShop = new ArrayList<>();
        try {
            listShop = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<ShopIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listShop;
    }

    @Override
    public List<PostIn> loadPosts() {
        File pathToFile = new File(basePath + "/" + DIC_DIR, POST_FILE);
        List<PostIn> listPost = new ArrayList<>();
        try {
            listPost = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<PostIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listPost;
    }

    @Override
    public List<NomenIn> loadNomen() {
        File pathToFile = new File(basePath + "/" + DIC_DIR, NOMEN_FILE);
        List<NomenIn> listNomen = new ArrayList<>();
        try {
            listNomen = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<NomenIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listNomen;
    }

    @Override
    public List<IncomeIn> loadIncome(String shopId) {
        List<IncomeIn> listIncomeIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                try {
                    IncomeIn incomeIn = objectMapper.readValue(file, IncomeIn.class);
                    listIncomeIn.add(incomeIn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return listIncomeIn;
    }

    @Override
    public void writeIncomeRec(String shopId, IncomeRec incomeRec) {
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
            File file = new File(path, incomeRec.getWbRegId() + ".txt");
            try {
                objectMapper.writeValue(file, incomeRec);
            } catch (IOException e) {
                e.printStackTrace();
            }
        MediaScannerConnection.scanFile(MainApp.getContext(), new String[] {path.toString()}, null, null);
    }

}
