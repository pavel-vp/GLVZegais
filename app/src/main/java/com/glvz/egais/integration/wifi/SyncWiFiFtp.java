package com.glvz.egais.integration.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.glvz.egais.MainApp;
import com.glvz.egais.dao.Syncro;
import com.glvz.egais.dao.SyncroMem;
import com.glvz.egais.integration.wifi.model.LocalFileRec;
import com.glvz.egais.integration.wifi.model.SyncFileRec;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class SyncWiFiFtp {

    private static final String SHOPID_DIR_TEMPLATE = "%SHOPID%";

    private Context context;
    private Syncro syncro;
    private String basePath;
    private String[] pathsInArr;
    private String[] pathsOutArr;
    private String ftp_server;
    private String ftp_user;
    private String ftp_root_dir;
    private int wifi_check_delay;


    public void init(Context context, String basePath, String[] pathsInArr, String[] pathsOutArr,
                     String ftp_server, String ftp_user, String ftp_root_dir, int wifi_check_delay) {
        this.context = context;
        this.syncro = new SyncroMem();
        this.syncro.init(context);
        this.basePath = basePath;
        this.pathsInArr = pathsInArr;
        this.pathsOutArr = pathsOutArr;
        this.ftp_server = ftp_server;
        this.ftp_user = ftp_user;
        if (ftp_root_dir != null) {
            this.ftp_root_dir = ftp_root_dir + "/";
        }
        this.wifi_check_delay = wifi_check_delay;
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
        }
        if (!result) {
            throw new RuntimeException("Выполните обмен через USB-кабель (WiFi-подключение отсутствует)");
        }
    }

    public void syncWiFiFtp(String shopId) throws Exception {
        List<SyncFileRec> pathsIn = convertDirs(this.pathsInArr, shopId);
        List<SyncFileRec> pathsOut = convertDirs(this.pathsOutArr, shopId);

        WifiManager wifi = (WifiManager)(context.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
        waitForWiFi(wifi, 2, wifi_check_delay);
            //wifi is enabled

                FTPClient ftpClient = new FTPClient();
                ftpClient.connect(InetAddress.getByName(ftp_server));
                ftpClient.login(ftp_user, ftp_user);
                Log.v("DaoMem", "status :: " + ftpClient.getStatus());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                Log.v("DaoMem", "IN===============================");
                // IN
                for (SyncFileRec rec : pathsIn) {
                    Log.v("DaoMem", "Remote directory: " + rec.getRemoteDir() + " -> local:"+rec.getLocalDir());

                    File pathLocal = new File(rec.getLocalDir());
                    pathLocal.mkdirs();
                    List<LocalFileRec> localFileRecList = syncro.getLocalFileRecsByPath(rec.getLocalDir());


                    FTPFile[] ftpFiles = ftpClient.listFiles(rec.getRemoteDir());
                    for (FTPFile ftpFile : ftpFiles) {
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
                                localFileRec = new LocalFileRec(rec.getLocalDir(), fileNameRemote, timeRemote);
                            } else {
                                localFileRec.setTimestamp(timeRemote);
                            }
                            syncro.addLocalFileRec(localFileRec);
                            Log.v("DaoMem", "File local has rewritten");
                        }
                        localFileRec.setProcessed(true);
                    }
                    // По оставшимся необработанным локальным файлам пройтись и удалить
                    for (LocalFileRec localFileRec : localFileRecList) {
                        if (!localFileRec.isProcessed()) {
                            syncro.deleteLocalFileRec(localFileRec.getPath(), localFileRec.getFileName());
                        }
                    }

                }

                Log.v("DaoMem", "OUT===============================");
                // OUT
                for (SyncFileRec rec : pathsOut) {
                    Log.v("DaoMem", "Local: " + rec.getLocalDir() + " -> Remote directory:"+rec.getRemoteDir());

                    File directory = new File(rec.getLocalDir());
                    ftpClient.makeDirectory("/" + rec.getRemoteDir());

                    // Get all CHNGED files from a local directory.
                    List<LocalFileRec> localFileList = syncro.getLocalChangedFiles(rec.getLocalDir());

                    for (LocalFileRec localFileRec : localFileList) {
                        if (localFileRec.isProcessed()) {
                            Log.v("DaoMem", "Local file->: " + localFileRec);

                            InputStream inputStream = new FileInputStream(new File(localFileRec.getPath() + "/" + localFileRec.getFileName()));
                            ftpClient.storeFile("/" + rec.getRemoteDir() + "/" + localFileRec.getFileName(), inputStream);
                            inputStream.close();
                        }
                    }
                    syncro.writeLocalChangedFiles(rec.getLocalDir(), localFileList);
                }

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
            result.add(new SyncFileRec(this.ftp_root_dir + pathConv, basePath + "/" + pathConv));
        }
        return result;
    }

}
