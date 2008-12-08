package com.alfray.autosettings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AutoUI extends Activity {

    private TextView mStatus;
    private PrefsValues mPrefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final SettingsHelper settings = new SettingsHelper(this);

        Button b = (Button) findViewById(R.id.show_settings);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startActivity(new Intent(AutoUI.this, PrefsActivity.class));
                forceSettingsCheck();
            }
        });
        
        b = (Button) findViewById(R.id.start);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.applyStartSettings();
            }
        });
        
        b = (Button) findViewById(R.id.stop);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.applyStopSettings();
            }
        });

        mPrefs = new PrefsValues(this);
        mStatus = (TextView) findViewById(R.id.status);
        
        mPrefs.getPrefs().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateStatus();
            }
        });

        updateStatus();
    }
    
    private void updateStatus() {
        String msg = mPrefs.getLog();
        mStatus.setText(msg);
    }
    
    private void forceSettingsCheck() {
        sendBroadcast(new Intent(AutoReceiver.ACTION_AUTO_CHECK_STATE));
    }
}