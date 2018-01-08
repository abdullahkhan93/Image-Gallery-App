package com.example.abdullah.fireapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by jharjuma on 12/8/17.
 */

@Entity
public class LocalImage {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "group_id")
    public String group_id;

    @ColumnInfo(name = "url")
    public String url;

    @ColumnInfo(name = "path")
    public String path;

    @ColumnInfo(name = "user_id")
    public String user_id;

    @ColumnInfo(name = "author")
    public String author;

    public String getId() { return this.id; };

    public String getGroupId() {
        return this.group_id;
    }

    public String getUrl() { return this.url; };

    public String getPath() { return this.path; };

    public String getUserId() { return this.user_id; };

    public String getAuthor() { return this.author; };

    public LocalImage(String id, String user_id, String group_id, String url, String path, String author) {
        this.user_id = user_id;
        this.id = id;
        this.group_id = group_id;
        this.url = url;
        this.path = path;
        this.author = author;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
