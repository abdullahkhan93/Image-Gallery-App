package com.example.abdullah.fireapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class JoinGroupActivity extends BaseActivity implements ZXingScannerView.ResultHandler, ApiCallResultHandler{


    private ZXingScannerView scanner_view;

    private Handler mHandler;

    private final int JOIN_GROUP_FAILED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!authenticated()) {
            return;
        }

        scanner_view = new ZXingScannerView(this);
        setContentView(scanner_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if(message.what == JOIN_GROUP_FAILED) {
                    scanner_view.resumeCameraPreview(JoinGroupActivity.this);
                    Toast.makeText(getApplicationContext(), "Failed to join to the group!", Toast.LENGTH_SHORT).show();
                }

            }
        };



    }

    @Override
    public void onResume() {
        super.onResume();
        scanner_view.setResultHandler(this);
        scanner_view.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        scanner_view.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner_view.stopCamera();// Stop camera on pause
    }

    @Override
    public void handleResult(Result result) {
       this.callJoinGroupApi(result.getText(), this);
    }

    @Override
    public void apiCallSuccess() {
        Intent intent = new Intent(this , GroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent,0);
        finish();
    }

    @Override
    public void apiCallFailure() {
        Message message = mHandler.obtainMessage(JOIN_GROUP_FAILED, null);
        message.sendToTarget();
    }
}

