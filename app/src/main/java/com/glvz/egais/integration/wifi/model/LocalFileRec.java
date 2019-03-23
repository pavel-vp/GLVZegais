package com.glvz.egais.integration.wifi.model;

public class LocalFileRec {
    private String path;
    private String fileName;
    private long timestamp;
    private boolean processed = false;
    private boolean uploaded = false;

    public LocalFileRec(String path, String fileName, long timestamp, boolean uploaded) {
        this.path = path;
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.uploaded = uploaded;
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
    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }


    @Override
    public String toString() {
        return "LocalFileRec{" +
                "path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", timestamp=" + timestamp +
                ", processed=" + processed +
                ", uploaded=" + uploaded +
                '}';
    }
}
