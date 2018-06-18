package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pasha on 07.06.18.
 */
public class PostIn {
    @JsonProperty(value="ClientRegID")
    private String clientRegID;
    @JsonProperty(value="Name")
    private String name;
    @JsonProperty(value="GroupBoxEnable")
    private int groupBoxEnable;

    public PostIn() {
    }

    public String getClientRegID() {
        return clientRegID;
    }

    public void setClientRegID(String clientRegID) {
        this.clientRegID = clientRegID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGroupBoxEnable() {
        return groupBoxEnable;
    }

    public void setGroupBoxEnable(int groupBoxEnable) {
        this.groupBoxEnable = groupBoxEnable;
    }

    @Override
    public String toString() {
        return "PostIn{" +
                "clientRegID='" + clientRegID + '\'' +
                ", name='" + name + '\'' +
                ", groupBoxEnable=" + groupBoxEnable +
                '}';
    }
}
