/*
 * (c) ralfoide gmail com, 2009
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import com.alfray.timeriffic.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ProfilesUI extends Activity {

    private LinearLayout mProfilesLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profiles_screen);
        
        mProfilesLinear = (LinearLayout) findViewById(R.id.profilesLinear);
        addDummy();
        addDummy();
    }

    private void addDummy() {
        Profile p = new Profile();
        p.addTo(getLayoutInflater(), mProfilesLinear);
    }
    
}
