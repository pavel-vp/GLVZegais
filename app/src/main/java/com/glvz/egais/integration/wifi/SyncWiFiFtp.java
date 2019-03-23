package com.glvz.egais.integration.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.glvz.egais.MainApp;
import com.glvz.egais.dao.Syncro;
import com.glvz.egais.dao.SyncroMem;
import com.glvz.egais.integration.model.SetupFtp;
import com.glvz.egais.integration.wifi.model.LocalFileRec;
import com.glvz.egais.integration.wifi.model.SyncFileRec;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SyncWiFiFtp {

    public static final int SYNC_SUCCESS = 1;
    public static final int SYNC_NO_WIFI = 2;
    public static final int SYNC_ERROR = 3;
    private static final String SHOPID_DIR_TEMPLATE = "%SHOPID%";

    private Context context;
    private Syncro syncro;
    private String basePath;
    private SetupFtp setupFtp;


    public void init(Context context, String basePath, SetupFtp setupFtp) {
        this.context = context;
        this.syncro = new SyncroMem();
        this.syncro.init(context);
        this.basePath = basePath;
        this.setupFtp = setupFtp;
    }


    private void waitForWiFi(WifiManager wifiManager,int times, int delay) {
        int cnt = 1;
        boolean result = false;
        while (!result && cnt < times) {
            result = wifiManager.isWifiEnabled();
            if (!result) {
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            cnt++;
        }
        if (!result) {
            throw new RuntimeException("Выполните обмен через USB-кабель (WiFi-подключение отсутствует)");
        }
    }

    public void syncShared() throws Exception {
        if (this.setupFtp == null) throw new RuntimeException();
        List<SyncFileRec> pathsIn = convertDirs(this.setupFtp.getPathsInArr(), "0");
        List<SyncFileRec> pathsOut = convertDirs(this.setupFtp.getPathsOutArr(), "0");

        FTPClient ftpClient = initFTPClient();

        Log.v("DaoMem", "IN===============================");
        // IN
        for (SyncFileRec rec : pathsIn) {
            if (rec.isShared()) {
                syncFilesIn(ftpClient, rec);
            }
        }
        Log.v("DaoMem", "IN=DONE==============================");

    }

    private void syncFilesIn(FTPClient ftpClient, SyncFileRec rec) throws Exception {
        Log.v("DaoMem", "Remote directory: " + rec.getRemoteDir() + " -> local:" + rec.getLocalDir());

        File pathLocal = new File(rec.getLocalDir());
        pathLocal.mkdirs();
        List<LocalFileRec> localFileRecList = syncro.getLocalFileRecsByPath(rec.getLocalDir());

        FTPFile[] ftpFiles = ftpClient.listFiles(rec.getRemoteDir());
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.isFile()) {
                String fileNameRemote = ftpFile.getName();
                long timeRemote = ftpFile.getTimestamp().getTimeInMillis();
                Log.v("DaoMem", "File remote: " + fileNameRemote + ", time:" + timeRemote);

                LocalFileRec localFileRec = syncro.findFileByName(localFileRecList, fileNameRemote);

                Log.v("DaoMem", "File local: " + localFileRec);
                if (localFileRec == null || localFileRec.getTimestamp() != timeRemote) {
                    File fileLocal = new File(pathLocal + "/" + fileNameRemote);
                    OutputStream outputStream = new FileOutputStream(fileLocal);
                    ftpClient.retrieveFile(rec.getRemoteDir() + "/" + fileNameRemote, outputStream);
                    outputStream.close();
                    if (localFileRec == null) {
                        localFileRec = new LocalFileRec(rec.getLocalDir(), fileNameRemote, timeRemote, false);
                    } else {
                        localFileRec.setTimestamp(timeRemote);
                    }
                    syncro.addLocalFileRec(localFileRec);
                    Log.v("DaoMem", "File local has rewritten");
                }
                localFileRec.setProcessed(true);
            }
        }
        // По оставшимся необработанным локальным файлам пройтись и удалить
        for (LocalFileRec localFileRec : localFileRecList) {
            if (!localFileRec.isProcessed()) {
                syncro.deleteLocalFileRec(localFileRec.getPath(), localFileRec.getFileName());
            }
        }
    }

    private FTPClient initFTPClient() throws Exception {
        WifiManager wifi = (WifiManager)(context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        waitForWiFi(wifi, 2, setupFtp.getWifi_check_delay());
        //wifi is enabled
        Log.v("DaoMem", "try connect to ftpclient :: " + setupFtp);
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getByName(setupFtp.getFtp_server()));
        ftpClient.login(setupFtp.getFtp_user(), setupFtp.getFtp_password());
        Log.v("DaoMem", "status :: " + ftpClient.getStatus());
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    public void syncShopDocs(String shopId) throws Exception {
        if (this.setupFtp == null) throw new RuntimeException();
        List<SyncFileRec> pathsIn = convertDirs(this.setupFtp.getPathsInArr(), shopId);
        List<SyncFileRec> pathsOut = convertDirs(this.setupFtp.getPathsOutArr(), shopId);

        WifiManager wifi = (WifiManager) (context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        waitForWiFi(wifi, 2, setupFtp.getWifi_check_delay());
        //wifi is enabled
        FTPClient ftpClient = initFTPClient();


        Log.v("DaoMem", "IN===============================");
        // IN
        for (SyncFileRec rec : pathsIn) {
            if (!rec.isShared()) {
                syncFilesIn(ftpClient, rec);
            }
        }
        Log.v("DaoMem", "IN=DONE===============================");

        Log.v("DaoMem", "OUT===============================");
        // OUT
        for (SyncFileRec rec : pathsOut) {
            Log.v("DaoMem", "Local: " + rec.getLocalDir() + " -> Remote directory:" + rec.getRemoteDir());

            File directory = new File(rec.getLocalDir());
            ftpClient.makeDirectory("/" + rec.getRemoteDir());

            // Get all CHNGED files from a local directory.
            List<LocalFileRec> localFileList = syncro.getLocalChangedFiles(rec.getLocalDir());

            for (LocalFileRec localFileRec : localFileList) {
                if (localFileRec.isProcessed() || !localFileRec.isUploaded()) {
                    Log.v("DaoMem", "Local file->: " + localFileRec);

                    boolean isDeleted = ftpClient.deleteFile("/" + rec.getRemoteDir() + "/" + localFileRec.getFileName());
                    int replyCode = ftpClient.getReplyCode();
                    String replyString = ftpClient.getReplyString();
                    Log.v("DaoMem", "isDeleted: " + isDeleted + ",replyCode:" + replyCode + ",replyString:" + replyString);
                    InputStream inputStream = new FileInputStream(new File(localFileRec.getPath() + "/" + localFileRec.getFileName()));
                    boolean isWritten = ftpClient.storeFile("/" + rec.getRemoteDir() + "/" + localFileRec.getFileName(), inputStream);
                    replyCode = ftpClient.getReplyCode();
                    replyString = ftpClient.getReplyString();
                    Log.v("DaoMem", "isWritten: " + isWritten + ",replyCode:" + replyCode + ",replyString:" + replyString);
                    inputStream.close();
                    localFileRec.setUploaded(isWritten);
                }
            }
            syncro.writeLocalChangedFiles(rec.getLocalDir(), localFileList);
        }
        Log.v("DaoMem", "OUT=DONE===============================");

    }

    private long findRemoteFileTimeByName(FTPFile[] remoteFileList, File file) {
        for (FTPFile ftpFile : remoteFileList) {
            if (ftpFile.getName().equals(file.getName())) {
                return ftpFile.getTimestamp().getTimeInMillis();
            }
        }
        return 0;
    }

    private List<SyncFileRec> convertDirs(String[] paths, String shopId) {
        List<SyncFileRec> result = new ArrayList<>();
        for (String path : paths) {
            String pathConv = path.replaceAll(SHOPID_DIR_TEMPLATE, shopId);
            result.add(new SyncFileRec(this.setupFtp.getFtp_root_dir()+ pathConv, basePath + "/" + pathConv, !path.contains(SHOPID_DIR_TEMPLATE)));
        }
        return result;
    }

}
