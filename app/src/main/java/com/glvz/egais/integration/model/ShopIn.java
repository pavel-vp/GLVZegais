package com.glvz.egais.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by pasha on 07.06.18.
 */
public class ShopIn {
    @JsonProperty(value="ID")
    private String id;
    @JsonProperty(value="Name")
    private String name;

    public ShopIn() {
    }

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

    @Override
    public String toString() {
        return "ShopIn{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
