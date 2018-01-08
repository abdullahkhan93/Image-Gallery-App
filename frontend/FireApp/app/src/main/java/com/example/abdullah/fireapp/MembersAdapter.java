package com.example.abdullah.fireapp;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jharjuma on 11/24/17.
 */

public class MembersAdapter extends ArrayAdapter {

    private final Activity context;

    private List<Member> members;
    public MembersAdapter(Activity context, List<Member> members) {
        super(context,R.layout.members_row , members);
        this.members = members;
        this.context=context;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View row_view = inflater.inflate(R.layout.members_row, null,true);

        TextView display_name_view = (TextView) row_view.findViewById(R.id.member_display_name);
        ImageView photo_view = (ImageView) row_view.findViewById(R.id.member_photo);

        Member member = members.get(position);
        display_name_view.setText(member.getName());
        Picasso.with(this.context).load(member.getPhotoUrl()).into(photo_view);

        return row_view;

    };
}
