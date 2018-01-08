package com.example.abdullah.fireapp;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MessagingIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseInstance";
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;
    /*
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        Log.d(TAG, "onTokenRefresh: called");
        //Wait for the user to login before proceeding to call updateInstanceId method
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in and defined so we can call updateInstanceId method.
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    updateInstanceId();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }


    private void sendInstanceIdToServer(String idToken, String notifyId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://mcc-fall-2017-g03.appspot.com/api/updatenotifyid")
                .put(RequestBody.create(null, "") ) //PUT
                .addHeader("Authorization", idToken)
                .addHeader("notify_id", notifyId)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            //Response not received
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "Get request failed");
            }
            //Response received
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Response was successful
                if (response.isSuccessful()) {
                    Log.d(TAG, "Response successfull");
                } else {
                    //Response unsuccessful
                    Log.d(TAG, "Response failed");
                }
            }
        });
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
    }

    private void updateInstanceId() {
        //Get the current firebase user
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        //If user is null return
        if(mUser == null) {
            return;
        }
        //Get the current users id_token
        mUser.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String notifyId = FirebaseInstanceId.getInstance().getToken();
                            String idToken = task.getResult().getToken();
                            Log.d(TAG,"idToken: " + idToken);
                            Log.d(TAG,"notifyId: " + notifyId);
                            sendInstanceIdToServer(idToken, notifyId);
                        } else {
                            Log.d(TAG, "token not found");
                        }
                    }
                });
    }
}