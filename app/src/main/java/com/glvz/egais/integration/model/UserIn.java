package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class UserIn {
    @JsonProperty(value="IDUser")
    private String id;
    @JsonProperty(value="NameUser")
    private String name;
    @JsonProperty(value="UserPodrs")
    private String[] usersPodrs;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getUsersPodrs() {
        return usersPodrs;
    }

    public void setUsersPodrs(String[] usersPodrs) {
        this.usersPodrs = usersPodrs;
    }

    @Override
    public String toString() {
        return "UserIn{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", usersPodrs=" + Arrays.toString(usersPodrs) +
                '}';
    }
}
