package com.glvz.egais.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.integration.sdcard.Integration;
import com.glvz.egais.integration.sdcard.IntegrationSDCard;
import com.glvz.egais.integration.wifi.model.LocalFileRec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyncroMem implements Syncro {
    private static final String KEY_LOCAL = "local";
    private static final String KEY_REMOTE = "remote";
    private SharedPreferences sharedPreferences;
    Integration integrationFile;

    @Override
    public void init(Context context) {
        sharedPreferences = context.getSharedPreferences("syncro", Activity.MODE_PRIVATE);
        File path = new File(Environment.getExternalStorageDirectory(), MainApp.getContext().getResources().getString(R.string.path_exchange));
        integrationFile = new IntegrationSDCard(path.getAbsolutePath());
    }

    @Override
    public void addLocalFileRec(LocalFileRec localFileRec) {
        List<LocalFileRec> localFileRecList = getLocalFileRecsByPath(localFileRec.getPath());
        boolean exists = false;
        for (LocalFileRec rec : localFileRecList) {
            if (rec.getFileName().equals(localFileRec.getFileName())) {
                rec.setTimestamp(localFileRec.getTimestamp());
                exists = true;
            }
        }
        if (!exists) {
            localFileRecList.add(localFileRec);
        }
        writeList(KEY_LOCAL, localFileRec.getPath(), localFileRecList);
    }

    @Override
    public void deleteLocalFileRec(String path, String name) {
        List<LocalFileRec> localFileRecList = getLocalFileRecsByPath(path);
        LocalFileRec found = null;
        for (LocalFileRec rec : localFileRecList) {
            if (rec.getFileName().equals(name)) {
                found = rec;
            }
        }
        if (found != null) {
            File file = new File(path + "/" + found.getFileName());
            file.delete();
            integrationFile.LogWrite(DaoMem.getDaoMem().getShopId(), "Do delete(2): " + found.getFileName());
            localFileRecList.remove(found);
        }
        writeList(KEY_LOCAL, path, localFileRecList);
    }

    private void writeList(String key, String path, List<LocalFileRec> localFileRecList) {
        Set<String> strings = new HashSet<>();
        for (LocalFileRec rec : localFileRecList) {
            String s = rec.getFileName() + "|" + rec.getTimestamp() + "|" + rec.isUploaded();
            strings.add(s);
        }
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putStringSet(key +"_"+path, strings);
        ed.apply();
    }

    @Override
    public List<LocalFileRec> getLocalFileRecsByPath(String path) {
        return getFileRecsByPath(KEY_LOCAL, path);
    }

    @Override
    public List<LocalFileRec> getRemoteFileRecsByPath(String path) {
        return getFileRecsByPath(KEY_REMOTE, path);
    }

    private List<LocalFileRec> getFileRecsByPath(String key, String path) {
        Set<String> strings = sharedPreferences.getStringSet(key+"_"+path, new HashSet<String>());
        List<LocalFileRec> result = new ArrayList<>();
        for (String s : strings) {
            String[] sArr = s.split("\\|");
            boolean isUploaded = false;
            try {
                isUploaded = Boolean.parseBoolean(sArr[2]);
            } catch (Exception e) {
                // Для совместимости
            }
            result.add(new LocalFileRec(path, sArr[0], Long.parseLong(sArr[1]), isUploaded));
        }
        return result;
    }

    @Override
    public LocalFileRec findFileByName(List<LocalFileRec> localFileRecList, String fileName) {
        for (LocalFileRec localFileRec : localFileRecList) {
            if (localFileRec.getFileName().equals(fileName)) {
                return localFileRec;
            }
        }
        return null;
    }

    @Override
    public List<LocalFileRec> getLocalChangedFiles(String path) {
        // Достать список файлов с директории, сравнив его с сохраненным. Если файл новый или у него изменилось время -
        // проставть  isProcessed
        File[] filesOnPath = new File(path).listFiles();
        List<LocalFileRec> localFileRecList = getRemoteFileRecsByPath(path);
        for (File fileOnPath : filesOnPath) {
            LocalFileRec localFileRec = findFileByName(localFileRecList, fileOnPath.getName());
            if (localFileRec == null) {
                // Файл новый
                localFileRec = new LocalFileRec(path, fileOnPath.getName(), fileOnPath.lastModified(), false);
                localFileRec.setProcessed(true);
                localFileRecList.add(localFileRec);
            } else {
                // проверить время - изменилось или нет
                if (localFileRec.getTimestamp() != fileOnPath.lastModified()) {
                    localFileRec.setTimestamp(fileOnPath.lastModified());
                    localFileRec.setProcessed(true);
                }
            }
        }
        return localFileRecList;
    }

    @Override
    public void writeLocalChangedFiles(String path, List<LocalFileRec> localFileList) {
        // Записать список файлов в этой дир в префсы
        writeList(KEY_REMOTE, path, localFileList);
    }

}
