package com.example.abdullah.fireapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by jharjuma on 12/8/17.
 */

@Dao
public interface LocalImageDao {

    @Insert
    void insert(LocalImage local_image);

    @Query("SELECT * FROM localimage where group_id = (:group_id)")
    List<LocalImage> getAllByGroup(String group_id);

    @Query("SELECT * FROM localimage where group_id = (:group_id) and user_id =  (:user_id)")
    List<LocalImage> getAllByUserAndGroup(String user_id, String group_id);

    @Query("SELECT * FROM localimage where id = (:id)")
    LocalImage getById(String id);

    @Update
    int update(LocalImage local_image);



}
