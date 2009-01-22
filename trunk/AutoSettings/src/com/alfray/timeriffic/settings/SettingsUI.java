/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.settings;

import com.alfray.timeriffic.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class SettingsUI extends Activity {

    private LinearLayout mProfilesLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.settings_screen);
        
        mProfilesLinear = (LinearLayout) findViewById(R.id.profilesLinear);
        addDummy();
        addDummy();
    }

    private void addDummy() {
        Profile p = new Profile();
        p.addTo(getLayoutInflater(), mProfilesLinear);
    }
    
}
