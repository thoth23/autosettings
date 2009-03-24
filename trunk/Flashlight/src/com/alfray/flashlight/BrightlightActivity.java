package com.alfray.flashlight;

import android.widget.TextView;

public class BrightlightActivity extends FlashlightActivity {

    public BrightlightActivity() {
        super("Bright");
    }
    
    @Override
    protected void initializeOnCreate(TextView tv) {
        tv.setText("Bright");
        setBrightness(1.0f);
    }
    
}
