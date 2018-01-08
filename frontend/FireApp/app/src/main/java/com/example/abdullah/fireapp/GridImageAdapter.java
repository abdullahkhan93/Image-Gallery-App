package com.example.abdullah.fireapp;

/**
 * Created by mpohjalainen on 9.12.2017.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class GridImageAdapter extends RecyclerView.Adapter<GridImageAdapter.ViewHolder> {
    private ArrayList<LocalImage> galleryList;
    private Context context;

    public GridImageAdapter(Context context, ArrayList<LocalImage> galleryList) {
        this.galleryList = galleryList;
        this.context = context;
    }

    @Override
    public GridImageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GridImageAdapter.ViewHolder viewHolder, final int i) {

        //viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Bitmap bitmap = BitmapFactory.decodeFile(galleryList.get(i).getPath());

        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, 300, 300), Matrix.ScaleToFit.CENTER);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        viewHolder.img.setImageBitmap(bitmap);
        viewHolder.img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra("path", galleryList.get(i).getPath());
        viewHolder.context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private Context context;
        public ViewHolder(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.img);
            context = view.getContext();
        }
    }

}