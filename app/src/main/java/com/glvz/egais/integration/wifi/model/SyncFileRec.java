package com.glvz.egais.integration.wifi.model;

public class SyncFileRec {
    private String remoteDir;
    private String localDir;

    public SyncFileRec(String remoteDir, String localDir) {
        this.remoteDir = remoteDir;
        this.localDir = localDir;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void setRemoteDir(String remoteDir) {
        this.remoteDir = remoteDir;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }
}
