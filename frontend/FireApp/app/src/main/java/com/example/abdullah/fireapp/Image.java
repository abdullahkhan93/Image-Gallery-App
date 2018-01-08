package com.example.abdullah.fireapp;

/**
 * Created by jharjuma on 12/8/17.
 */

public class Image {

    public String url;
    private String firebaseId;
    private String author;

    public Image() {

    }

    public Image(String url, String author) {
        this.url = url;
        this.author = author;
    }

    public String getUrl() {
        return this.url;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getFirebaseId() {
        return this.firebaseId;
    }

    public String getAuthor() {
        return this.author;
    }
}
