package com.glvz.egais.integration.sdcard;

import android.media.MediaScannerConnection;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.glvz.egais.MainApp;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.income.IncomeRecOutput;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.utils.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private static final String ALCCODE_FILE = "alccodes.json";
    private static final String MARK_FILE = "marks.json";
    private static final String USER_FILE = "users.json";
    private static final String APK_FILE = "glvzegais.apk";

    private static final String DOC_PREFIX_INCOME = "TTN";
    private static final String DOC_PREFIX_MOVE = "DOCMOVE";

    private ObjectMapper objectMapper = new ObjectMapper();


    public IntegrationSDCard(String basePath) {
        this.basePath = basePath;
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
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
    public List<AlcCodeIn> loadAlcCode() {
        File pathToFile = new File(basePath + "/" + DIC_DIR, ALCCODE_FILE);
        List<AlcCodeIn> listAlcCode = new ArrayList<>();
        try {
            listAlcCode = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<AlcCodeIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listAlcCode;
    }

    @Override
    public List<MarkIn> loadMark() {
        File pathToFile = new File(basePath + "/" + DIC_DIR, MARK_FILE);
        List<MarkIn> listMark = new ArrayList<>();
        try {
            listMark = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<MarkIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listMark;
    }

    @Override
    public List<UserIn> loadUsers() {
        File pathToFile = new File(basePath + "/" + DIC_DIR, USER_FILE);
        List<UserIn> listUser = new ArrayList<>();
        try {
            listUser = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<UserIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listUser;
    }

    @Override
    public void initDirectories(String shopId) {
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        path.mkdirs();
        path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
        path.mkdirs();

    }

    @Override
    public List<IncomeIn> loadIncome(String shopId) {
        List<IncomeIn> listIncomeIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_INCOME)) {
                    try {
                        IncomeIn incomeIn = objectMapper.readValue(file, IncomeIn.class);
                        listIncomeIn.add(incomeIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listIncomeIn;
    }

    @Override
    public List<MoveIn> loadMove(String shopId) {
        List<MoveIn> listMoveIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_MOVE)) {
                    try {
                        MoveIn moveIn = objectMapper.readValue(file, MoveIn.class);
                        listMoveIn.add(moveIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listMoveIn;
    }

    @Override
    public void writeIncomeRec(String shopId, IncomeRec incomeRec) {
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
        File file = new File(path, incomeRec.getDocId() + "_out.json");
            try {
                IncomeRecOutput out = incomeRec.formatAsOutput();
                objectMapper.writeValue(file, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        MediaScannerConnection.scanFile(MainApp.getContext(), new String[] {path.toString()}, null, null);
    }

    @Override
    public File loadNewApk() {
        File path = new File(basePath + "/" + APK_DIR);
        File file = new File(path, APK_FILE );
        return file;
    }

    private void listf(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all the files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listf(file.getAbsolutePath(), files);
                }
            }
        }
    }

    @Override
    public List<IncomeIn> clearOldData(int numDaysOld) {
        List<IncomeIn> res = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, - numDaysOld);
        // Взять все файлы из директории
        List<File> files = new ArrayList<>();
        listf(basePath + "/" + SHOPS_DIR, files);
        for (File file : files) {
            if (file.getName().toUpperCase().startsWith("/" + DOC_PREFIX_INCOME + "/")) {
                boolean toDelete = false;
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        IncomeIn incomeIn = objectMapper.readValue(file, IncomeIn.class);
                        Date d = StringUtils.jsonStringToDate(incomeIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(incomeIn);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        IncomeRecOutput incomeRecOutput = objectMapper.readValue(file, IncomeRecOutput.class);
                        Date d = StringUtils.jsonStringToDate(incomeRecOutput.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
                if (toDelete) {
                    file.delete();
                }
            }
        }

        return res;
    }

}
