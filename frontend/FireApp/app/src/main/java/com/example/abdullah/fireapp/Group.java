package com.example.abdullah.fireapp;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by joonas on 18.11.2017.
 */

@IgnoreExtraProperties
public class Group{

    public String groupname;
    public long duration;
    public Map<String, Member> members;
    public GroupOwner owner;
    public String token;

    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getGroupname() {
        return this.groupname;
    }

    public long getDuration() {
        return this.duration;
    }

    public Map<String,Member> getMembers() {
        return this.members;
    }

    public GroupOwner getOwner() {
        return this.owner;
    }

    public boolean isAdmin(String user_id) {

        if(user_id == null) return false;
        return user_id.equals(this.owner.getId());

    }

    public String getToken() {
        return this.token;
    }


}