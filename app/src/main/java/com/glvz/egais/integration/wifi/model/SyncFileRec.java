package com.glvz.egais.integration.wifi.model;

public class SyncFileRec {
    private String remoteDir;
    private String localDir;
    private boolean shared;

    public SyncFileRec(String remoteDir, String localDir, boolean shared) {
        this.remoteDir = remoteDir;
        this.localDir = localDir;
        this.shared = shared;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public String getLocalDir() {
        return localDir;
    }
    public boolean isShared() {
        return shared;
    }
}
