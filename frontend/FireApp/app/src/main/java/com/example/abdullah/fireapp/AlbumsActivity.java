package com.example.abdullah.fireapp;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends BaseActivity {


    class AlbumAdapter extends ArrayAdapter<Album> {


        public AlbumAdapter(Context context, ArrayList<Album> albums) {
            super(context, 0, albums);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Album album = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.album_layout, parent, false);
            }
            // Lookup view for data population
            TextView name = (TextView) convertView.findViewById(R.id.album_name);
            TextView number_of_photos = (TextView) convertView.findViewById(R.id.album_size);
            ImageView album_photo = (ImageView) convertView.findViewById(R.id.album_photo);
            ImageView album_sync_photo = (ImageView) convertView.findViewById(R.id.album_sync_photo);
            // Populate the data into the template view using the data object

            try {
                name.setText(URLDecoder.decode(album.getLocalGroup().getName(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 not supported");
            }

            number_of_photos.setText(Integer.toString(album.getLocalImages().size()));

            if(album.getLocalGroup().getId().equals("private")) {
                album_sync_photo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.not_synced));
            }
            else {
                album_sync_photo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.synced));
            }

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.default_album);
            if(album.getLocalImages().size() > 0) {
                bitmap = BitmapFactory.decodeFile(album.getLocalImages().get(0).getPath());

                Matrix m = new Matrix();
                m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, 300, 300), Matrix.ScaleToFit.CENTER);

                bitmap =Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            }

            album_photo.setImageBitmap(bitmap);
            album_photo.setOnClickListener(new View.OnClickListener() {

                private LocalGroup local_group;

                public View.OnClickListener init(LocalGroup local_group) {
                    this.local_group = local_group;
                    return this;
                }

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AlbumsActivity.this, ImageGridActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("group_id", local_group.getId());
                    bundle.putString("group_name", local_group.getName());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }.init(album.getLocalGroup()));


            return convertView;
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.ALBUMS);

        GridView albums_grid = (GridView) findViewById(R.id.albums_grid);
        ArrayList<Album> albums = new ArrayList<Album>();
        AlbumAdapter album_adapter = new AlbumAdapter(this, albums);
        albums_grid.setAdapter(album_adapter);


        new AsyncTask<List<Album>, Integer, Long>() {

            private String user_id;
            private AlbumAdapter adapter;
            public AsyncTask<List<Album>, Integer, Long> init(String user_id, AlbumAdapter adapter) {
                this.user_id = user_id;
                this.adapter = adapter;
                return this;
            }

            @Override
            protected Long doInBackground(List<Album>... album_lists) {

                AppDatabase local_db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "schizodb").fallbackToDestructiveMigration().build();

                ArrayList<Album> albums = new ArrayList<Album>();

                for(LocalGroup local_group : local_db.localGroupDao().getAllByUser(user_id)) {

                    List<LocalImage> local_images = local_db.localImageDao().getAllByGroup(local_group.getId());

                    Album album = new Album(local_group, local_images);
                    albums.add(album);
                }

                album_lists[0].clear();
                album_lists[0].addAll(albums);
                adapter.notifyDataSetChanged();

                return null;
            }
        }.init(mFirebaseUser.getUid(), album_adapter).execute(albums);
    }

}
