package com.example.abdullah.fireapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by joonas on 18.11.2017.
 */

public abstract class BaseActivity extends AppCompatActivity  {

    protected FirebaseAuth mFirebaseAuth;
    protected FirebaseUser mFirebaseUser;
    protected DatabaseReference mDatabase;
    protected StorageReference mStorage;

    private final String api_url = "http://mcc-fall-2017-g03.appspot.com/api";

    protected boolean authenticated() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity

            Intent intent = new Intent(this, SignInActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("redirect_to", this.getClass().getCanonicalName());
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
            return false;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mStorage = FirebaseStorage.getInstance().getReference();
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(5000);

        // start photo sync
        startService(new Intent(this, PhotoSyncService.class));

        return true;
    }

    protected void callJoinGroupApi(final String token, final ApiCallResultHandler result_handler) {
        Request request = new Request.Builder()
                .url(api_url+"/joingroup")
                .header("token", token)
                .method("PUT", RequestBody.create(null, new byte[0]))
                .build();

        this.callApi(request, result_handler);

    }

    protected void callLeaveOrDeleteGroupApi(final String group_id,  final ApiCallResultHandler result_handler) {
        Request request = new Request.Builder()
                .url(api_url+"/deletegroup")
                .header("group_id", group_id)
                .method("DELETE", RequestBody.create(null, new byte[0]))
                .build();

        this.callApi(request, result_handler);
    }

    private void callApi(final Request request, final ApiCallResultHandler result_handler ) {
        mFirebaseUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {

                            OkHttpClient client = new OkHttpClient();

                            Request authorized_request = request.newBuilder()
                                    .addHeader("Authorization", task.getResult().getToken())
                                    .build();

                            client.newCall(authorized_request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    result_handler.apiCallFailure();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if(!response.isSuccessful()) {
                                        result_handler.apiCallFailure();
                                    }
                                    else {
                                        result_handler.apiCallSuccess();
                                    }
                                }

                            });


                        } else {
                            result_handler.apiCallFailure();
                        }
                    }
                });

    }

    protected void callCreateGroupApi(final String group_name, final long duration, final ApiCallResultHandler result_handler) {


        mFirebaseUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {

                            OkHttpClient client = new OkHttpClient();
                            String encodedGroupName;
                            try {
                                encodedGroupName = URLEncoder.encode(group_name, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                throw new AssertionError("UTF-8 not supported");
                            }

                            Request request = new Request.Builder()
                                    .url(api_url+"/creategroup")
                                    .header("Authorization", task.getResult().getToken())
                                    .header("group_name", encodedGroupName)
                                    .header("duration", Long.toString(duration))
                                    .method("POST", RequestBody.create(null, new byte[0]))
                                    .build();


                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    result_handler.apiCallFailure();
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if(!response.isSuccessful()) {
                                        result_handler.apiCallFailure();
                                    }
                                    else {
                                        result_handler.apiCallSuccess();
                                    }
                                }

                            });


                        } else {
                            result_handler.apiCallFailure();
                        }
                    }
                });



    }



}
