package com.example.abdullah.fireapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by jharjuma on 12/9/17.
 */


@Dao
public interface LocalGroupDao {

    @Insert
    void insert(LocalGroup local_group);

    @Query("SELECT * FROM localgroup where id = (:id)")
    LocalGroup getById(String id);

    @Query("SELECT * FROM localgroup where id = (:id) and user_id = (:user_id)")
    LocalGroup getByIdAndUser(String id, String user_id);

    @Query("SELECT * FROM localgroup where user_id = (:user_id)")
    List<LocalGroup> getAllByUser(String user_id);

}
