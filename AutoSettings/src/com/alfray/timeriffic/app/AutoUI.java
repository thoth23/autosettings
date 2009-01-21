/*
 * Copyright 2008 (c) ralfoide gmail com, 2008
 * Project: auto settings
 * License: GPL version 3 or any later version
 */


package com.alfray.timeriffic.app;

import java.util.Calendar;

import com.alfray.timeriffic.R;
import com.alfray.timeriffic.R.drawable;
import com.alfray.timeriffic.R.id;
import com.alfray.timeriffic.R.layout;
import com.alfray.timeriffic.R.string;
import com.alfray.timeriffic.prefs.PrefsActivity;
import com.alfray.timeriffic.prefs.PrefsValues;
import com.alfray.timeriffic.ui.IntroDialogActivity;
import com.alfray.timeriffic.utils.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class AutoUI extends Activity {

    protected static final int SETTINGS_UPDATED = 42;
    private TextView mStatus;
    private PrefsValues mPrefs;
    private Runnable mStatusUpdater;
    private ScrollView mScroller;
    
    private Calendar mCalendar = Calendar.getInstance();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button b = (Button) findViewById(R.id.show_settings);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrefs();
            }
        });
        
        b = (Button) findViewById(R.id.check_now);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSettingsCheck();
            }
        });

        mPrefs = new PrefsValues(this);
        mScroller = (ScrollView) findViewById(R.id.scroller);
        mStatus = (TextView) findViewById(R.id.status);
        mStatus.setFocusable(true);
        mStatus.setFocusableInTouchMode(true);
        mStatus.requestFocus();
        
        mStatusUpdater = new Runnable() {
            @Override
            public void run() {
                updateStatus();
            }
        };
        
        mPrefs.getPrefs().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                // we want to make sure this runs in this thread
                mStatus.post(mStatusUpdater);
            }
        });

        mStatusUpdater.run();
        
        showIntro(false /*force*/);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch(requestCode) {
        case SETTINGS_UPDATED:
            requestSettingsCheck();
        }
    }

    private void updateStatus() {
        long now = System.currentTimeMillis();
        
        String[] lines = mPrefs.getLog().split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            int pos = line.indexOf(':');
            if (pos < 0) continue;
            String ts = line.substring(0, pos);
            long timeMs = Long.parseLong(ts);
            
            ts = Utils.formatTime(mCalendar, timeMs, now);
            sb.append(ts).append(": ").append(line.substring(pos + 1)).append('\n');
        }
        
        mStatus.setText(sb.toString());
        mScroller.scrollTo(0, mStatus.getHeight());
    }

    private void requestSettingsCheck() {
        sendBroadcast(new Intent(AutoReceiver.ACTION_AUTO_CHECK_STATE));
    }

    private void showIntro(boolean force) {
        if (!force) {
            Application app = getApplication();
            if (app instanceof TimerifficApp) {
                TimerifficApp tapp = (TimerifficApp) app;
                if (!tapp.isIntroDisplayed() && !mPrefs.dismissIntro()) {
                    tapp.setIntroDisplayed(true);
                    force = true;
                }
            }
        }
        
        if (force) {
            startActivity(new Intent(AutoUI.this, IntroDialogActivity.class));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, R.string.settings,  0, R.string.settings).setIcon(R.drawable.ic_menu_preferences);
        menu.add(0, R.string.check_now,  0, R.string.check_now).setIcon(R.drawable.ic_menu_rotate);
        menu.add(0, R.string.about,  0, R.string.about).setIcon(R.drawable.ic_menu_help);

        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.string.settings:
            showPrefs();
            break;
        case R.string.check_now:
            requestSettingsCheck();
            break;
        case R.string.about:
            showIntro(true /*force*/);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPrefs() {
        startActivityForResult(new Intent(AutoUI.this, PrefsActivity.class),
                SETTINGS_UPDATED);
    }
}
