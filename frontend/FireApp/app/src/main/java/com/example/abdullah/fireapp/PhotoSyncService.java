package com.example.abdullah.fireapp;


import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.UUID;

/**
 * Created by jharjuma on 12/8/17.
 */

public class PhotoSyncService extends Service implements ValueEventListener, ChildEventListener{

    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseUser mFirebaseUser;
    protected DatabaseReference mDatabase;
    protected StorageReference mStorage;
    protected AppDatabase local_db;
    private User user;

    @Override
    public void onCreate() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(5000);

        local_db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "schizodb").fallbackToDestructiveMigration().build();

        mDatabase.child("users/"+mFirebaseUser.getUid()).addValueEventListener(this);

        new AsyncTask<String,Long,Long>() {
            @Override
            protected Long doInBackground(String... user_ids) {
                if(local_db.localGroupDao().getByIdAndUser("private", user_ids[0]) == null) {
                    local_db.localGroupDao().insert(new LocalGroup("private", user_ids[0], "Private"));
                }
                return null;
            }
        }.execute(mFirebaseUser.getUid());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {


    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User new_user = dataSnapshot.getValue(User.class);
        if(new_user == null || new_user.getCurrent_Group() == null ) {
            if(user != null && user.getCurrent_Group() != null) {
                mDatabase.child("groups/" + user.getCurrent_Group() + "/images").removeEventListener((ChildEventListener) this);
            }
        }
        else if (new_user.getCurrent_Group() != null) {
            mDatabase.child("groups/"+new_user.getCurrent_Group()+"/images").addChildEventListener(this);

            mDatabase.child("groups/"+ new_user.getCurrent_Group()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Group group = dataSnapshot.getValue(Group.class);

                    if(group != null) {

                        LocalGroup local_group = new LocalGroup(dataSnapshot.getKey(), mFirebaseUser.getUid(), group.getGroupname());

                        new AsyncTask<LocalGroup, Integer, Long>() {
                            @Override
                            protected Long doInBackground(LocalGroup... local_groups) {

                                if(local_db.localGroupDao().getById(local_groups[0].getId()) == null) {
                                    local_db.localGroupDao().insert(local_groups[0]);
                                }

                                return null;
                            }
                        }.execute(local_group);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        user = new_user;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        final Image image = dataSnapshot.getValue(Image.class);
        image.setFirebaseId(dataSnapshot.getKey());

        final String group_id = dataSnapshot.getRef().getParent().getParent().getKey();

        final LocalImage new_local_image = new LocalImage(image.getFirebaseId(), mFirebaseUser.getUid(), group_id, image.getUrl(), new File(getApplicationContext().getFilesDir().getAbsolutePath()+"/"+UUID.randomUUID().toString()+".jpg").getAbsolutePath(), image.getAuthor());

        new AsyncTask<LocalImage, Integer, Long>() {
            @Override
            protected Long doInBackground(LocalImage... newLocalImages) {

                if(local_db.localImageDao().getById(newLocalImages[0].getId()) == null) {
                    // image doesn't exist locally, lets download it

                    FirebaseStorage.getInstance().getReferenceFromUrl(newLocalImages[0].getUrl()).getFile(new File(newLocalImages[0].getPath())).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {

                        private LocalImage push_this;

                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            new AsyncTask<LocalImage, Integer, Long>() {

                                @Override
                                protected Long doInBackground(LocalImage... pushLocalImages) {
                                    local_db.localImageDao().insert(pushLocalImages[0]);
                                    Log.d("push to db", pushLocalImages[0].getUserId()+" "+pushLocalImages[0].getGroupId()+" "+pushLocalImages[0].getPath()+" "+pushLocalImages[0].getUrl());
                                    return null;
                                }
                            }.execute(this.push_this);

                        }

                        public OnSuccessListener<FileDownloadTask.TaskSnapshot> init(LocalImage push_this) {
                            this.push_this = push_this;
                            return this;
                        }

                    }.init(newLocalImages[0])).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("push to db", "failed to get file from storage");
                        }
                    });

                }

                return null;
            }
        }.execute(new_local_image);



    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        final Image image = dataSnapshot.getValue(Image.class);
        image.setFirebaseId(dataSnapshot.getKey());
        final String group_id = dataSnapshot.getRef().getParent().getParent().getKey();
        final LocalImage update_local_image = new LocalImage(image.getFirebaseId(), mFirebaseUser.getUid(), group_id, image.getUrl(), "", image.getAuthor());

        new AsyncTask<LocalImage, Long, Long>() {

            @Override
            protected Long doInBackground(LocalImage... localImages) {

                LocalImage local_image = local_db.localImageDao().getById(localImages[0].getId());
                if(local_image != null) {
                    localImages[0].setPath(local_image.getPath());
                    local_db.localImageDao().update(localImages[0]);
                }

                Log.d("push local image", localImages[0].getUserId()+" "+localImages[0].getGroupId()+" "+localImages[0].getPath()+" "+localImages[0].getUrl()+" "+localImages[0].getAuthor());


                return null;
            }
        }.execute(update_local_image);

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }


}
