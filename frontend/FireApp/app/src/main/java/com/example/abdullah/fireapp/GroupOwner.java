package com.example.abdullah.fireapp;

/**
 * Created by jharjuma on 12/5/17.
 */

public class GroupOwner {

    private String id;
    private String name;

    public GroupOwner() {

    }

    public GroupOwner(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
