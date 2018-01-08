package com.example.abdullah.fireapp;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class TakePicsActivity extends BaseActivity  implements ValueEventListener {

    private static final String TAG = "TakePicsActivity";
    private static final int REQUEST_CAMERA = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView mImageReview;
    Button acceptButton;
    Button keepPrivateButton;
    TextView userWarning;
    TextView groupTextView;
    Bitmap mCurrentImage;
    BarcodeDetector detector;

    Bitmap small_image;
    StorageReference mImagesRef;
    private User mUser;

    SharedPreferences preferences;

    private File mImageFile;
    private Uri mImageUri;
    private Matrix rotationMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        mImageReview = findViewById(R.id.imageReview);
        acceptButton = findViewById(R.id.acceptButton);
        keepPrivateButton = findViewById(R.id.buttonKeepPrivate);
        hideButtons();
        userWarning = findViewById(R.id.textViewUserWarning);
        groupTextView = findViewById(R.id.textViewGroup);
        userWarning.setText("");
        groupTextView.setText("");
        setSupportActionBar(toolbar);


        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!checkCameraHardware(getApplicationContext())) {
            Log.e(TAG, "Device has no camera.");
            Toast.makeText(this, "Device has no camera", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.CAMERA);


        detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats( Barcode.QR_CODE | Barcode.DATA_MATRIX | Barcode.PDF417)
                        .build();
        if(!detector.isOperational()){
            Log.e(TAG, "Failed to set up a barcode detector.");
            return;
        }

        mImagesRef = mStorage.child("images");
        mUser = null;

        mDatabase.child("users/"+mFirebaseUser.getUid()).addValueEventListener(this);

    }

    //camera API instructions
    //https://developer.android.com/guide/topics/media/camera.html#camera-preview

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public void onClickSnap(View view) {
        //Check for camera permission (Runtime permission check for "Dangerous" permissions required for API 23 and higher)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            requestCameraPermission();
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck == 0) {
            dispatchTakePictureIntent();
        }
    }

    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }

    //When allowing camera permission for the first time launch join group activity in this callback funbction if permission was granted.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: called");
        if(permissions[0].equals("android.permission.CAMERA") && grantResults[0] == 0) {
            dispatchTakePictureIntent();
        }
    }

    public void onClickKeepPrivate (View view) {
        saveCurrentImageToFile();
    }

    public void saveCurrentImageToFile() {
        FileOutputStream out = null;
        try {
            String filename = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".jpg";
            out = new FileOutputStream(filename);

            int new_w;
            int new_h;
            if(mCurrentImage.getWidth() > mCurrentImage.getHeight()) {
                new_w = 1280;
                new_h = 960;
            }
            else {
                new_w = 960;
                new_h = 1280;
            }
            Bitmap image_scaled = Bitmap.createScaledBitmap(mCurrentImage, new_w, new_h, true);

            image_scaled.compress(Bitmap.CompressFormat.JPEG, 100, out);
            userWarning.setText("Saved locally!");

            LocalImage local_image = new LocalImage(UUID.randomUUID().toString(), mFirebaseUser.getUid(), "private", "", filename, mFirebaseUser.getDisplayName());

            new AsyncTask<LocalImage, Long, Long>() {

                @Override
                protected Long doInBackground(LocalImage... localImages) {

                    AppDatabase local_db = Room.databaseBuilder(getApplicationContext(),
                            AppDatabase.class, "schizodb").fallbackToDestructiveMigration().build();

                    local_db.localImageDao().insert(localImages[0]);

                    return null;
                }
            }.execute(local_image);

            hideButtons();
        } catch (Exception e) {
            e.printStackTrace();
            userWarning.setText("Failed to save");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClickAccept(View view) {
        sendToFirebase();
    }
    private void sendToFirebase() {

        hideButtons();

        String basename = Long.toString(System.currentTimeMillis());

        if ((mUser != null) && (mUser.getCurrent_Group() != null)) {

            userWarning.setText("Trying to connect . . .");
            StorageUtil.sendToFirebaseStorage(mImagesRef, mCurrentImage, mUser.getCurrent_Group(), basename+"-low.jpg",  0,
                    //on failure callback
                    new StorageUtil.Command(){
                        public void execute(String url){}},
                    //on success callback
                    new StorageUtil.Command(){
                        public void execute(String url){}}
            , rotationMatrix);

            StorageUtil.sendToFirebaseStorage(mImagesRef, mCurrentImage, mUser.getCurrent_Group(), basename+"-high.jpg", 1,
                    //on failure callback
                    new StorageUtil.Command(){
                        public void execute(String url){}},
                    //on success callback
                    new StorageUtil.Command(){
                        public void execute(String url){
                            final Image image = new Image(url, mFirebaseUser.getDisplayName());
                            mDatabase.child("/groups/"+mUser.getCurrent_Group()+"/images").push().setValue(image);
                        }}
                    , rotationMatrix);

            StorageUtil.sendToFirebaseStorage(mImagesRef, mCurrentImage, mUser.getCurrent_Group(), basename+"-full.jpg", 2,
                    //on failure callback
                    new StorageUtil.Command(){
                        public void execute(String url){
                            showAcceptButton();
                            userWarning.setText("Failed to sync");
                        }},
                    //on success callback
                    new StorageUtil.Command(){
                        public void execute(String url){
                            // save reference to realtime db

                            hideButtons();
                            userWarning.setText("Synced");
                        }}
                    , rotationMatrix);

        } else {
            userWarning.setText("Can't sync while you're not in a group.");
            showAcceptButton();
        }
    }
    private void hideButtons() {
        acceptButton.setEnabled(false);
        acceptButton.setVisibility(View.INVISIBLE);
        keepPrivateButton.setEnabled(false);
        keepPrivateButton.setVisibility(View.INVISIBLE);
    }
    private void showAcceptButton() {
        acceptButton.setEnabled(true);
        acceptButton.setVisibility(View.VISIBLE);
    }
    private void showKeepPrivateButton() {
        keepPrivateButton.setEnabled(true);
        keepPrivateButton.setVisibility(View.VISIBLE);
    }

    private void dispatchTakePictureIntent() {

        hideButtons();
        userWarning.setText("");

        File path = new File(this.getFilesDir(), "picture_capture");
        if (!path.exists()) path.mkdirs();
        mImageFile = new File(path, "image.jpg");

        if(mImageFile != null && mImageFile.exists()) {
            mImageFile.delete();
        }
        mImageUri = FileProvider.getUriForFile(this, "com.example.abdullah.fireapp.fileprovider", mImageFile);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    private boolean barcodesCheck() {

        if (!preferences.getBoolean("barcodePref",true))
            return false;

        Frame frame = new Frame.Builder().setBitmap(small_image).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        return barcodes.size() > 0;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCurrentImage = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            hideButtons();


            try {

                mCurrentImage = BitmapFactory.decodeFile(mImageFile.getAbsolutePath());

                small_image = Bitmap.createScaledBitmap(mCurrentImage, mCurrentImage.getWidth() / 4, mCurrentImage.getHeight() / 4, true);

                ExifInterface exif = new ExifInterface(mImageFile.getAbsolutePath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);

                rotationMatrix = new Matrix();
                if (rotation != 0f) {rotationMatrix.preRotate(rotationInDegrees);}

                mImageReview.setImageBitmap( Bitmap.createBitmap(small_image, 0, 0, small_image.getWidth(), small_image.getHeight(), rotationMatrix, true));
                if (barcodesCheck()) {
                    Toast.makeText(this, "Barcodes detected!", Toast.LENGTH_SHORT).show();
                    userWarning.setText("Are you sure you want to share your barcodes?");
                    showAcceptButton();
                    showKeepPrivateButton();
                } else {
                    userWarning.setText("");
                    sendToFirebase();
                }

            }catch (Exception e)
            {
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Failed to load", e);
            }
        }
    }

    //ValueEventListener for user/group

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Integer i=1;
    }

    @Override
    public void onDataChange(DataSnapshot data_snapshot) {

        if(data_snapshot.getKey().equals(mFirebaseUser.getUid())) {

            mUser = data_snapshot.getValue(User.class);
            if ((mUser != null) && (mUser.getCurrent_Group() != null)) {
                groupTextView.setText("Pictures you take will be synced to your current group.");
            } else {
                groupTextView.setText("You are not in any group right now.");
            }
        }
    }

}
