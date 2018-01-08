package com.example.abdullah.fireapp;

import android.renderscript.Sampler;

import com.google.firebase.database.ValueEventListener;

/**
 * Created by jharjuma on 12/5/17.
 */

public class User {

    private String current_group;
    private String id;

    private ValueEventListener group_event_listener;

    public User() {

    }

    public User(String group_id) {
        this.current_group = current_group;
    }

    public String getCurrent_Group() {
        return this.current_group;
    }

}
