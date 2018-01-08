package com.example.abdullah.fireapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by jharjuma on 12/9/17.
 */

@Entity
public class LocalGroup {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "user_id")
    public String user_id;

    public String getId() { return this.id; };

    public String getName() {
        return this.name;
    }

    public String getUserId() { return this.user_id; };

    public LocalGroup(String id, String user_id, String name) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
    }
}
