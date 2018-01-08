package com.example.abdullah.fireapp;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class ImageGridActivity extends BaseActivity {

    protected AppDatabase local_db;
    private GroupBySelection group_by_selection;
    private GroupByImageAdapter group_by_adapter;

    class GroupBySelection {

        public static final int CATEGORY = 0;
        public static final int AUTHOR = 1;

        private int selected;

        public GroupBySelection() {
            this.selected = AUTHOR;
        }

        public int getSelected() {
            return this.selected;
        }

        public void setSelected(int selected) {
            this.selected = selected;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.GALLERY);

        local_db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "schizodb").fallbackToDestructiveMigration().build();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.image_gallery_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<LocalImage> image_list = new ArrayList<LocalImage>();
        group_by_selection = new GroupBySelection();
        group_by_adapter = new GroupByImageAdapter(getApplicationContext(), image_list, group_by_selection);
        recyclerView.setAdapter(group_by_adapter);

        Bundle bundle = getIntent().getExtras();
        String group_id = bundle.getString("group_id");

        try {
            getSupportActionBar().setTitle(URLDecoder.decode(bundle.getString("group_name"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported");
        }

        new AsyncTask<String, Integer, Long>() {

            private ArrayList<LocalImage> image_list;
            private GroupByImageAdapter adapter;
            public AsyncTask<String, Integer, Long> init(ArrayList<LocalImage> image_list, GroupByImageAdapter adapter) {
                this.image_list = image_list;
                this.adapter = adapter;
                return this;
            }

            @Override
            protected Long doInBackground(String... params) {

                String user_id = params[0];
                String group_id = params[1];

                AppDatabase local_db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "schizodb").fallbackToDestructiveMigration().build();

                image_list.clear();
                image_list.addAll(local_db.localImageDao().getAllByUserAndGroup(user_id, group_id));

                adapter.notifyDataSetChanged();

                return null;
            }
        }.init(image_list, group_by_adapter).execute(mFirebaseUser.getUid(), group_id);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_grid_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.group_author) {
            group_by_selection.setSelected(GroupBySelection.AUTHOR);
            group_by_adapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }
}
