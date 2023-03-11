package com.glvz.egais.integration.sdcard;

import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Base64;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.model.doc.BaseRecOutput;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkRecOutput;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.income.IncomeRecOutput;
import com.glvz.egais.integration.model.doc.inv.InvIn;
import com.glvz.egais.integration.model.doc.inv.InvRecOutput;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.integration.model.doc.move.MoveRecOutput;
import com.glvz.egais.integration.model.doc.writeoff.WriteoffRecOutput;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.photo.PhotoRec;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.utils.StringUtils;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private static final String SETUP_FTP_FILE = "setupftp.json";
    private static final String COMMAND_FILE = "websevice.json";


    private static final String DOC_PREFIX_INCOME_ALCO = "TTN";
    private static final String DOC_PREFIX_INCOME_CIGA = "ED";
    private static final String DOC_PREFIX_MOVE = "DOCMOVE";
    private static final String DOC_PREFIX_CHECKMARK = "CHECK";
    private static final String DOC_PREFIX_FINDMARK = "FINDMARK";
    private static final String DOC_PREFIX_INV = "INV";
    private static final String DOC_PREFIX_PHOTO = "IMG";
    private static final String DOC_PREFIX_LOG = "LOG";

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
    public List<CommandIn> loadCommands() {
        File pathToFile = new File(basePath  + "/" + DIC_DIR, COMMAND_FILE);
        List<CommandIn> listCommands = new ArrayList<>();
        try {
            listCommands = objectMapper.readValue(pathToFile, new TypeReference<ArrayList<CommandIn>>(){});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return listCommands;
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
    public List<MarkIn> loadMark(String shopId) {
        File pathToFile = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR, MARK_FILE);
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
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_INCOME_ALCO) ||
                    file.getName().toUpperCase().startsWith(DOC_PREFIX_INCOME_CIGA)) {
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
    public SetupFtp loadSetupFtp() {
        File pathToFile = new File(basePath , SETUP_FTP_FILE);
        if (!pathToFile.exists()) {
            pathToFile = new File(basePath + "/" + DIC_DIR, SETUP_FTP_FILE);
        }
        SetupFtp setupFtp = null;
        try {
            setupFtp = objectMapper.readValue(pathToFile, SetupFtp.class);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return setupFtp;
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
    public List<CheckMarkIn> loadCheckMark(String shopId) {
        List<CheckMarkIn> listCheckMarkIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_CHECKMARK)) {
                    try {
                        CheckMarkIn checkMarkIn = objectMapper.readValue(file, CheckMarkIn.class);
                        listCheckMarkIn.add(checkMarkIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listCheckMarkIn;
    }

    @Override
    public List<FindMarkIn> loadFindMark(String shopId) {
        List<FindMarkIn> listFindMarkIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_FINDMARK)) {
                    try {
                        FindMarkIn findMarkIn = objectMapper.readValue(file, FindMarkIn.class);
                        listFindMarkIn.add(findMarkIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listFindMarkIn;
    }

    @Override
    public List<InvIn> loadInv(String shopId) {
        List<InvIn> listInvIn = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + IN_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_INV)) {
                    try {
                        InvIn InvIn = objectMapper.readValue(file, InvIn.class);
                        listInvIn.add(InvIn);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listInvIn;
    }

    @Override
    public List<File> loadPhotoFiles(String shopId) {
        List<File> listPhoto = new ArrayList<>();
        File path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
        if (path.listFiles() != null) {
            for (File file : path.listFiles()) {
                if (file.getName().toUpperCase().startsWith(DOC_PREFIX_PHOTO) &&
                        !file.getName().toUpperCase().startsWith(DOC_PREFIX_PHOTO+"-MINI")) {
                    listPhoto.add(file);
                }
            }
        }
        return listPhoto;
    }

    @Override
    public void writeBaseRec(String shopId, BaseRec rec) {
        File path = null;
        if (rec instanceof PhotoRec) {
            path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
            File file = new File(path,  DOC_PREFIX_PHOTO + "-" + rec.getDocIdForExport() + ".jpeg");
            try(FileOutputStream fos =  new FileOutputStream(file)) {
                String out = ((PhotoRec) rec).getData();
                byte[] data = Base64.decode(out, 0);
                fos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = new File(path,  DOC_PREFIX_PHOTO + "-MINI-" + rec.getDocIdForExport() + ".jpeg");
            try(FileOutputStream fos =  new FileOutputStream(file)) {
                String out = ((PhotoRec) rec).getDataMini();
                byte[] data = Base64.decode(out, 0);
                fos.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR);
            File file = new File(path, rec.getDocIdForExport() + "_out.json");
            try {
                BaseRecOutput out = rec.formatAsOutput();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public Set<String> clearOldData(int numDaysOld) {
        Set<String> res = new HashSet<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, - numDaysOld);
        // Взять все файлы из директории
        List<File> files = new ArrayList<>();
        listf(basePath + "/" + SHOPS_DIR, files);
        for (File file : files) {
            boolean toDelete = false;
            // Приход
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_INCOME_ALCO) ||
                file.getName().toUpperCase().startsWith(DOC_PREFIX_INCOME_CIGA)) {
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        IncomeIn incomeIn = objectMapper.readValue(file, IncomeIn.class);
                        Date d = StringUtils.jsonStringToDate(incomeIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(incomeIn.getDocId());
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
            }
            // Перемещение
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_MOVE)) {
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        MoveIn moveIn = objectMapper.readValue(file, MoveIn.class);
                        Date d = StringUtils.jsonStringToDate(moveIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(moveIn.getDocId());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        MoveRecOutput moveRecOutput = objectMapper.readValue(file, MoveRecOutput.class);
                        Date d = StringUtils.jsonStringToDate(moveRecOutput.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }
            // Списание
            if (file.getName().toUpperCase().startsWith(WriteoffRec.TYEDOC_WRITEOFF)) {
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        WriteoffRecOutput writeoffRecOutput = objectMapper.readValue(file, WriteoffRecOutput.class);
                        Date d = StringUtils.jsonBottlingStringToDate(writeoffRecOutput.getDate());
                        if (d != null && d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }
            // Проверка марок
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_CHECKMARK)) {
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        CheckMarkIn checkMarkIn = objectMapper.readValue(file, CheckMarkIn.class);
                        Date d = StringUtils.jsonStringToDate(checkMarkIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(checkMarkIn.getDocId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        CheckMarkRecOutput checkMarkRecOutput = objectMapper.readValue(file, CheckMarkRecOutput.class);
                        Date d = StringUtils.jsonStringToDate(checkMarkRecOutput.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }
            // Поиск марок
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_FINDMARK)) {
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        FindMarkIn findMarkIn = objectMapper.readValue(file, FindMarkIn.class);
                        Date d = StringUtils.jsonStringToDate(findMarkIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(findMarkIn.getDocId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }
            // Инвентаризация
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_INV)) {
                // Если это импорт
                if (file.getAbsolutePath().contains("/" + IN_DIR + "/")) {
                    try {
                        InvIn invIn = objectMapper.readValue(file, InvIn.class);
                        Date d = StringUtils.jsonStringToDate(invIn.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        } else {
                            res.add(invIn.getDocId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        InvRecOutput invRecOutput = objectMapper.readValue(file, InvRecOutput.class);
                        Date d = StringUtils.jsonStringToDate(invRecOutput.getDate());
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }
            // Фото
            if (file.getName().toUpperCase().startsWith(DOC_PREFIX_PHOTO) ||
                    file.getName().toUpperCase().startsWith(DOC_PREFIX_LOG)) {
                // Если это экспорт
                if (file.getAbsolutePath().contains("/" + OUT_DIR + "/")) {
                    try {
                        // распарсить имя файла - вытащить дату
                        String date = null;
                        // IMG-MINI-2020-10-10 20:20:20.
                        // IMG-2020-10-10 20:20:20.
                        if (file.getName().toUpperCase().startsWith(DOC_PREFIX_PHOTO+"-MINI")) {
                            date = file.getName().substring(9,28);
                        } else {
                            date = file.getName().substring(4,23);
                        }
                        Date d = StringUtils.imgStringToDate(date);
                        if (d.before(calendar.getTime())) {
                            toDelete = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        toDelete = true;
                    }
                }
            }

            if (toDelete) {
                file.delete();
            }
        }

        return res;
    }

    @Override
    public void deleteFileRec(BaseRec baseRec, String shopId) {
        File path = null;
        if (baseRec instanceof PhotoRec) {
            path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR + "/IMG-" + baseRec.getDocIdForExport() + ".jpeg");
            path.delete();
            path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR + "/IMG-MINI-" + baseRec.getDocIdForExport() + ".jpeg");
            path.delete();
        } else {
            path = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR + "/" + baseRec.getDocIdForExport() + "_out.json");
            path.delete();
        }
        MediaScannerConnection.scanFile(MainApp.getContext(), new String[] {path.toString()}, null, null);
    }

    @Override
    public String getPhotoFileName(String skladId, String name) {
        return basePath + "/" + SHOPS_DIR + "/" + skladId + "/" + OUT_DIR + "/" + name;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    public void LogWrite(String shopId, String log_text) {
        try {
            DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String FILENAME = "LOG-" + df.format(date) + " 00_00_00-" + DaoMem.getDaoMem().getDeviceId() + ".json";
            File sdFile = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR, FILENAME);
            FileWriter fw = new FileWriter (sdFile, true);
            fw.write(dtf.format(date) + ": " + log_text + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportDbFile(String shopId, String pathDb) throws IOException {
            DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String FILENAME =  "GLVZ.db."+ DaoMem.getDaoMem().getDeviceId();
            File sdFile = new File(basePath + "/" + SHOPS_DIR + "/" + shopId + "/" + OUT_DIR, FILENAME);
            copy(new File(pathDb), sdFile);
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

}
