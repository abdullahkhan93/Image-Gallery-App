package com.example.abdullah.fireapp;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

/**
 * Created by LAT on 7.12.2017.
 */

public class StorageUtil {

    static String TAG = "StorageUTIL0";

    public interface Command{
        void execute(String url);
    }

    public static void sendToFirebaseStorage(StorageReference baseReference, Bitmap image, String Current_Group, String filename, int file_size_preference,
                                             final Command onFailCallback, final Command onSuccessCallback, Matrix rotationMatrix) {

        int new_w;
        int new_h;

        //Matrix m = new Matrix();
        if (file_size_preference == 0) {

            if(image.getWidth() > image.getHeight()) {
                new_w = 640;
                new_h = 480;
            }
            else {
                new_w = 480;
                new_h = 640;
            }

        } else if (file_size_preference == 1) {

            if(image.getWidth() > image.getHeight()) {
                new_w = 1280;
                new_h = 960;
            }
            else {
                new_w = 960;
                new_h = 1280;
            }

        } else {
            new_w = image.getWidth();
            new_h = image.getHeight();
        }
        //m.setRectToRect(new RectF(0, 0, image.getWidth(), image.getHeight()), new RectF(0, 0, new_w, new_h), Matrix.ScaleToFit.CENTER);
        //Bitmap image_scaled =Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);

        Bitmap image_scaled = Bitmap.createScaledBitmap(image, new_w, new_h, true);
        Bitmap rotated = Bitmap.createBitmap(image_scaled, 0, 0, image_scaled.getWidth(), image_scaled.getHeight(), rotationMatrix, true);
        final StorageReference sendFileRef = baseReference.child(Current_Group).child(filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rotated.compress(Bitmap.CompressFormat.JPEG, 100, baos);



        byte[] data = baos.toByteArray();

        UploadTask uploadTask = sendFileRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Send to Firebase storage failed.");
                onFailCallback.execute(null);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.e(TAG, "Send to Firebase storage succeeded.");
                onSuccessCallback.execute(sendFileRef.toString());
            }
        });
    }
}
