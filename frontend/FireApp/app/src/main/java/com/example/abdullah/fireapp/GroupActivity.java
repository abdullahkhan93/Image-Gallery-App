package com.example.abdullah.fireapp;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupActivity extends BaseActivity  implements ValueEventListener, ApiCallResultHandler {

    public static final String TAG = "GroupActivity";
    private static final int REQUEST_CAMERA = 0;
    private Toolbar toolbar;
    private User user;
    private Group group;
    private ConstraintLayout content_layout;

    private GroupListener current_group_listener;

    private Handler mHandler;

    private final int LEAVE_OR_DELETE_GROUP_FAILED = 1;

    @Override
    public void apiCallSuccess() {

    }

    @Override
    public void apiCallFailure() {
        Message message = mHandler.obtainMessage(LEAVE_OR_DELETE_GROUP_FAILED, null);
        message.sendToTarget();
    }

    class GroupListener implements ValueEventListener {

        private ConstraintLayout group_content_layout;
        private List<Member> members;
        private MembersAdapter members_adapter;
        private String user_id;
        private boolean effective = true;

        public GroupListener(String user_id, ConstraintLayout group_content_layout) {
            this.user_id = user_id;
            this.group_content_layout = group_content_layout;
            members = new ArrayList<Member>();
            members_adapter = new MembersAdapter(GroupActivity.this, members);

            ListView members_view = (ListView) group_content_layout.findViewById(R.id.members_view);
            members_view.setAdapter(members_adapter);
        }

        public ConstraintLayout getContentLayout() {
            return this.group_content_layout;
        }

        public void setEffective(boolean effective) {
            this.effective = effective;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Group snapshot_group = dataSnapshot.getValue(Group.class);
            if(snapshot_group == null) {
                // group is removed
                dataSnapshot.getRef().removeEventListener(this);
            }
            else { // update view

                members.clear();
                if (snapshot_group.getMembers() == null) return;
                Set entry_set = snapshot_group.getMembers().entrySet();
                Iterator it = entry_set.iterator();

                boolean user_is_member = false;
                while(it.hasNext()){
                    Map.Entry<String,Member> map_entry = (Map.Entry)it.next();
                    String member_id = map_entry.getKey();
                    members.add(map_entry.getValue());

                    if(member_id.equals(user_id)) {
                        user_is_member = true;
                    }
                }

                members_adapter.notifyDataSetChanged();

                if(!user_is_member) { // user is removed from group
                    dataSnapshot.getRef().removeEventListener(this);
                }

                if(effective) {
                    try {
                        toolbar.setTitle(URLDecoder.decode(snapshot_group.groupname, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError("UTF-8 not supported");
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(snapshot_group.duration);

                    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                    DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

                    toolbar.setSubtitle("Expires @ "+dateFormat.format(calendar.getTime())+" "+timeFormat.format(calendar.getTime()));
                    group = snapshot_group;
                    GroupActivity.this.invalidateOptionsMenu();

                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.GROUP);

        content_layout = (ConstraintLayout) findViewById(R.id.content_layout);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if(message.what == LEAVE_OR_DELETE_GROUP_FAILED) {
                    Toast.makeText(getApplicationContext(), "Failed to leave or delete group!", Toast.LENGTH_SHORT).show();
                }

            }
        };

        mDatabase.child("users/"+mFirebaseUser.getUid()).addValueEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFirebaseUser != null) {
            mDatabase.child("users/"+mFirebaseUser.getUid()).removeEventListener(this);
        }
    }

    @Override
    public void onDataChange(DataSnapshot data_snapshot) {

        User new_user = data_snapshot.getValue(User.class);
        if(new_user == null || new_user.getCurrent_Group() == null) {
            content_layout.removeAllViewsInLayout();
            getLayoutInflater().inflate(R.layout.content_no_group, content_layout);
            toolbar.setTitle("Group");
            toolbar.setSubtitle("");

            group = null;
            this.invalidateOptionsMenu();
        }
        else { // user has group

            if(user == null || (user != null && !new_user.getCurrent_Group().equals(user.getCurrent_Group()))) {
                // group has changed, old listener will be removed automatically

                if(current_group_listener != null) {
                    current_group_listener.setEffective(false);
                }

                content_layout.removeAllViewsInLayout();
                ConstraintLayout listener_layout  = (ConstraintLayout)getLayoutInflater().inflate(R.layout.content_group, content_layout);
                GroupListener group_listener = new GroupListener(mFirebaseUser.getUid(), listener_layout);
                current_group_listener = group_listener;
                mDatabase.child("groups/"+new_user.getCurrent_Group()).addValueEventListener(group_listener);
            }


        }

        user = new_user;
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.group_menu, menu);

        MenuItem add_member_item = menu.findItem(R.id.add_member);
        MenuItem join_group_item = menu.findItem(R.id.join_group);
        MenuItem create_group_item = menu.findItem(R.id.create_group);
        MenuItem leave_group_item = menu.findItem(R.id.leave_group);
        MenuItem delete_group_item = menu.findItem(R.id.delete_group);

        if(group == null) {
            add_member_item.setVisible(false);
            join_group_item.setVisible(true);
            create_group_item.setVisible(true);
            leave_group_item.setVisible(false);
            delete_group_item.setVisible(false);
        }
        else {
            add_member_item.setVisible(true);
            join_group_item.setVisible(false);
            create_group_item.setVisible(false);

            if(group.isAdmin(mFirebaseUser.getUid())) {
                leave_group_item.setVisible(false);
                delete_group_item.setVisible(true);
            }
            else {
                leave_group_item.setVisible(true);
                delete_group_item.setVisible(false);
            }

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.create_group) {
            startActivity(new Intent(this, CreateGroupActivity.class));
            return true;
        }
        else if (id == R.id.join_group) {
            //Check for camera permission (Runtime permission check for "Dangerous" permissions required for API 23 and higher)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Camera permission has not been granted.
                requestCameraPermission();
            }
            //Check if permission is granted for camera
            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if(permissionCheck != 0) {
                return false;
            }
            Intent intent = new Intent(this, JoinGroupActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.leave_group || id == R.id.delete_group) {
            this.callLeaveOrDeleteGroupApi(user.getCurrent_Group(), this);
        }
        else if (id == R.id.add_member) {
            Intent intent = new Intent(this, AddMemberActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("token", group.getToken());
            intent.putExtras(bundle);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //When allowing camera permission for the first time launch join group activity in this callback funbction if permission was granted.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult: called");
        if(permissions[0].equals("android.permission.CAMERA") && grantResults[0] == 0) {
            Intent intent = new Intent(this, JoinGroupActivity.class);
            startActivity(intent);
        }
    }

    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }
}


