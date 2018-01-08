package com.example.abdullah.fireapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by abdullah on 4.12.2017.
 */

public class FullScreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_full_screen_image);


        Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getExtras().getString("path"));
        PhotoView photoView = (PhotoView) findViewById(R.id.fullImageView);
        photoView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true));


    }
}