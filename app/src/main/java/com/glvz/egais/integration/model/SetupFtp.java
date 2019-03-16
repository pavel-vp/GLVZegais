package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class SetupFtp {
    @JsonProperty(value="paths_in_arr")
    private String[] pathsInArr;
    @JsonProperty(value="paths_out_arr")
    private String[] pathsOutArr;
    @JsonProperty(value="ftp_server")
    private String ftp_server;
    @JsonProperty(value="ftp_user")
    private String ftp_user;
    @JsonProperty(value="ftp_password")
    private String ftp_password;
    @JsonProperty(value="ftp_root_dir")
    private String ftp_root_dir;
    @JsonProperty(value="wifi_check_delay")
    private int wifi_check_delay;

    public String[] getPathsInArr() {
        return pathsInArr;
    }

    public void setPathsInArr(String[] pathsInArr) {
        this.pathsInArr = pathsInArr;
    }

    public String[] getPathsOutArr() {
        return pathsOutArr;
    }

    public void setPathsOutArr(String[] pathsOutArr) {
        this.pathsOutArr = pathsOutArr;
    }

    public String getFtp_server() {
        return ftp_server;
    }

    public void setFtp_server(String ftp_server) {
        this.ftp_server = ftp_server;
    }

    public String getFtp_user() {
        return ftp_user;
    }

    public void setFtp_user(String ftp_user) {
        this.ftp_user = ftp_user;
    }

    public String getFtp_root_dir() {
        return ftp_root_dir;
    }

    public void setFtp_root_dir(String ftp_root_dir) {
        this.ftp_root_dir = ftp_root_dir;
    }

    public int getWifi_check_delay() {
        return wifi_check_delay;
    }

    public void setWifi_check_delay(int wifi_check_delay) {
        this.wifi_check_delay = wifi_check_delay;
    }


    public String getFtp_password() {
        return ftp_password;
    }

    public void setFtp_password(String ftp_password) {
        this.ftp_password = ftp_password;
    }

    @Override
    public String toString() {
        return "SetupFtp{" +
                "pathsInArr=" + Arrays.toString(pathsInArr) +
                ", pathsOutArr=" + Arrays.toString(pathsOutArr) +
                ", ftp_server='" + ftp_server + '\'' +
                ", ftp_user='" + ftp_user + '\'' +
                ", ftp_password='" + ftp_password + '\'' +
                ", ftp_root_dir='" + ftp_root_dir + '\'' +
                ", wifi_check_delay=" + wifi_check_delay +
                '}';
    }
}
