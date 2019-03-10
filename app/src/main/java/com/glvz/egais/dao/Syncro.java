package com.glvz.egais.dao;

import android.content.Context;
import com.glvz.egais.integration.wifi.model.LocalFileRec;
import java.util.List;

public interface Syncro {

    void init(Context context);
    void addLocalFileRec(LocalFileRec localFileRec);
    void deleteLocalFileRec(String path, String name);

    List<LocalFileRec> getLocalFileRecsByPath(String path);

    LocalFileRec findFileByName(List<LocalFileRec> localFileRecList, String fileNameRemote);

    List<LocalFileRec> getLocalChangedFiles(String path);

    void writeLocalChangedFiles(String path, List<LocalFileRec> localFileList);

    List<LocalFileRec> getRemoteFileRecsByPath(String path);

}
