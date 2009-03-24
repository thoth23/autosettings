package com.alfray.flashlight;

import android.widget.TextView;

public class DarklightActivity extends FlashlightActivity {

    public DarklightActivity() {
        super("Dark");
    }

    @Override
    protected void initializeOnCreate(TextView tv) {
        tv.setText("Dark");
        setBrightness(0.1f);
    }
    
}
