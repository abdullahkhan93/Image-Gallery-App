package com.example.abdullah.fireapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //addPreferencesFromResource(R.layout.content_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!authenticated()) {
            return;
        }

        DrawerUtil.getDrawer(this,toolbar, mFirebaseUser, mFirebaseAuth, DrawerUtil.ITEM.SETTINGS);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new PrefsFragment()).commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences (this);

    }
    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.content_settings);

        }
    }
}
