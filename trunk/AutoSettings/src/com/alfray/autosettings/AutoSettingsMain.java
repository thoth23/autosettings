package com.alfray.autosettings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AutoSettingsMain extends Activity {
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
                startActivity(new Intent(AutoSettingsMain.this, PrefsActivity.class));
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
    }
}