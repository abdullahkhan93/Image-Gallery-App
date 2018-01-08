package com.example.abdullah.fireapp;

import java.net.URL;

/**
 * Created by joonas on 18.11.2017.
 */

public class Member {

    private String name;
    private String photo_url;

    public Member() {

    }

    public Member(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPhotoUrl(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getPhotoUrl() {
        return this.photo_url;
    }
}

