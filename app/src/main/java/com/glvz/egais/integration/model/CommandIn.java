package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class CommandIn {
    public final static String PARAM_SHOPID = "ShopID";
    public final static String PARAM_BARCODE = "Barcode";
    public final static String PARAM_USERID = "IDUser";
    public final static String PARAM_NOMEN = "Nomen";

    @JsonProperty(value="ID")
    private String id;
    @JsonProperty(value="ParentID")
    private String parentID;
    @JsonProperty(value="Name")
    private String name;
    @JsonProperty(value="Caption")
    private String caption;
    @JsonProperty(value="URL")
    private String url;
    @JsonProperty(value="User")
    private String user;
    @JsonProperty(value="Pass")
    private String pass;
    @JsonProperty(value="Operation")
    private String operation;
    @JsonProperty(value="Params")
    private String[] params;
    @JsonProperty(value="NS")
    private String ns;
    @JsonProperty(value="ServiceName")
    private String serviceName;
    @JsonProperty(value="TimeOut")
    private Integer timeOut;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public String toString() {
        return "CommandIn{" +
                "id='" + id + '\'' +
                ", parentID='" + parentID + '\'' +
                ", name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                ", url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", pass='" + pass + '\'' +
                ", operation='" + operation + '\'' +
                ", params=" + Arrays.toString(params) +
                ", ns='" + ns + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", timeout='" + timeOut + '\'' +
                '}';
    }
}
