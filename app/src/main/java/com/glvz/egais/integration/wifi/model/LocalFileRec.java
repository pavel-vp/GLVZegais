package com.glvz.egais.integration.wifi.model;

public class LocalFileRec {
    private String path;
    private String fileName;
    private long timestamp;
    private boolean processed = false;

    public LocalFileRec(String path, String fileName, long timestamp) {
        this.path = path;
        this.fileName = fileName;
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    @Override
    public String toString() {
        return "LocalFileRec{" +
                "path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", timestamp=" + timestamp +
                ", processed=" + processed +
                '}';
    }
}
