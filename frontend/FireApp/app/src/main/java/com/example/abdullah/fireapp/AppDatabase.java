package com.example.abdullah.fireapp;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by jharjuma on 12/8/17.
 */

@Database(entities = {LocalImage.class, LocalGroup.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocalImageDao localImageDao();
    public abstract LocalGroupDao localGroupDao();
}
