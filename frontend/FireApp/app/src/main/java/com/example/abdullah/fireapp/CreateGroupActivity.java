package com.example.abdullah.fireapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;

public class CreateGroupActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, ApiCallResultHandler, View.OnClickListener{


    Calendar group_expires_cal;
    private Handler mHandler;
    ProgressDialog create_group_progress;

    private final int CREATE_GROUP_FAILED = 1;
    private final int CREATE_GROUP_SUCCEEDED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.GROUP);

        getSupportActionBar().setTitle(R.string.create_group);


        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {

                create_group_progress.dismiss();

                if(message.what == CREATE_GROUP_FAILED) {
                    Toast.makeText(getApplicationContext(), "Failed to create a group!", Toast.LENGTH_SHORT).show();
                }

            }
        };

        group_expires_cal = Calendar.getInstance();

        EditText date_expires = (EditText) findViewById(R.id.expires_date);
        EditText time_expires = (EditText) findViewById(R.id.expires_time);

        date_expires.setOnClickListener(new View.OnClickListener() {

            private CreateGroupActivity activity;

            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        activity,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }

            public View.OnClickListener init(CreateGroupActivity activity) {
                this.activity = activity;
                return this;
            }

        }.init(this));

        time_expires.setOnClickListener(new View.OnClickListener() {

            private CreateGroupActivity activity;

            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog dpd = TimePickerDialog.newInstance(
                        activity,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                dpd.show(getFragmentManager(), "Timepickerdialog");
            }

            public View.OnClickListener init(CreateGroupActivity activity) {
                this.activity = activity;
                return this;
            }

        }.init(this));

        FloatingActionButton create_group_fab = (FloatingActionButton) findViewById(R.id.create_group_fab);
        create_group_fab.setOnClickListener(this);
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {

        group_expires_cal.set(Calendar.YEAR, year);
        group_expires_cal.set(Calendar.MONTH, monthOfYear);
        group_expires_cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        EditText date_expires = (EditText) findViewById(R.id.expires_date);
        date_expires.setText(dateFormat.format(group_expires_cal.getTime()));
    }


    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        group_expires_cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        group_expires_cal.set(Calendar.MINUTE, minute);
        EditText time_expires = (EditText) findViewById(R.id.expires_time);
        time_expires.setText(hourOfDay+":"+minute);
    }

    @Override
    public void apiCallSuccess() {

        Message message = mHandler.obtainMessage(CREATE_GROUP_SUCCEEDED, null);
        message.sendToTarget();

        Intent intent = new Intent(this , GroupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(intent,0);
        finish();
    }

    @Override
    public void apiCallFailure() {
        Message message = mHandler.obtainMessage(CREATE_GROUP_FAILED, null);
        message.sendToTarget();
    }

    @Override
    public void onClick(View v) {

        EditText time_expires = (EditText) findViewById(R.id.expires_time);
        EditText date_expires = (EditText) findViewById(R.id.expires_date);
        EditText name = (EditText) findViewById(R.id.create_group_name);

        if(!name.getText().toString().isEmpty() && !time_expires.getText().toString().isEmpty() && !date_expires.getText().toString().isEmpty()) {


            create_group_progress = new ProgressDialog(this);
            create_group_progress.setMessage("Creating group...");
            create_group_progress.setIndeterminate(true);
            create_group_progress.setCancelable(true);
            create_group_progress.show();

            this.callCreateGroupApi(name.getText().toString(), group_expires_cal.getTimeInMillis(),this);
        }


    }
}
