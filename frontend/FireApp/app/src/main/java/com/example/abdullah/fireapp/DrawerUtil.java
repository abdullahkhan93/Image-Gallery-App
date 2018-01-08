package com.example.abdullah.fireapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

/**
 * Created by joonas on 19.11.2017.
 */

public class DrawerUtil {

    static int GROUP_ITEM = 0;

    public enum ITEM {
        GROUP(0), SETTINGS(1), GALLERY(2), CAMERA(3), LOG_OUT(4), ALBUMS(5);

        private int identifier;

        ITEM(int identifier) {
            this.identifier = identifier;
        }

        public int getValue() {
            return this.identifier;
        }
    }

    static boolean initialized = false;

    public static void initializeDrawer() {

        if (!initialized) {
            DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                @Override
                public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                    Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
                }

                @Override
                public void cancel(ImageView imageView) {
                    Picasso.with(imageView.getContext()).cancelRequest(imageView);
                }
            });
            initialized = true;
        }
    }

    private class DrawerClickListener implements Drawer.OnDrawerItemClickListener {

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            return false;
        }
    }

    public static void getDrawer(final Activity activity, Toolbar toolbar, final FirebaseUser user, final FirebaseAuth firebase_auth, final ITEM selected_item) {

        initializeDrawer();

        PrimaryDrawerItem item_group = new PrimaryDrawerItem().withIdentifier(ITEM.GROUP.getValue())
                .withName(R.string.group).withIcon(R.mipmap.group);

        PrimaryDrawerItem item_settings = new PrimaryDrawerItem().withIdentifier(ITEM.SETTINGS.getValue())
                .withName(R.string.settings).withIcon(R.mipmap.settings);

        PrimaryDrawerItem item_gallery = new PrimaryDrawerItem().withIdentifier(ITEM.ALBUMS.getValue())
                .withName(R.string.gallery).withIcon(R.mipmap.gallery);

        PrimaryDrawerItem item_camera = new PrimaryDrawerItem().withIdentifier(ITEM.CAMERA.getValue())
                .withName(R.string.camera).withIcon(R.mipmap.camera);

        PrimaryDrawerItem item_log_out= new PrimaryDrawerItem().withIdentifier(ITEM.LOG_OUT.getValue())
                .withName(R.string.log_out).withIcon(R.drawable.log_out);

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl())
                )
                .build();

        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(selected_item.getValue())
                .addDrawerItems(
                        item_gallery,
                        item_camera,
                        item_group,
                        item_settings,
                        new DividerDrawerItem(),
                        item_log_out

                ).
                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent intent = null;
                       if(drawerItem.getIdentifier() == ITEM.GROUP.getValue()) {
                           intent = new Intent(activity,
                                   GroupActivity.class);
                       }
                       else if(drawerItem.getIdentifier() == ITEM.SETTINGS.getValue()) {
                            intent = new Intent(activity,
                                    SettingsActivity.class);
                       }
                       else if(drawerItem.getIdentifier() == ITEM.CAMERA.getValue()) {
                           intent = new Intent(activity,
                                   TakePicsActivity.class);
                       }
                       else if(drawerItem.getIdentifier() == ITEM.ALBUMS.getValue()) {
                           intent = new Intent(activity,
                                   AlbumsActivity.class);
                       }
                       else if(drawerItem.getIdentifier() == ITEM.LOG_OUT.getValue()) {
                           firebase_auth.signOut();
                           //Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                           Toast.makeText(activity, "Signed out", Toast.LENGTH_SHORT).show();
                           intent = new Intent(activity, SignInActivity.class);
                       }
                       if(intent != null) {
                           activity.startActivity(intent);
                           activity.finish();
                           return true;
                       }

                       return false;
                    }
                })
                .build();
    }
}
